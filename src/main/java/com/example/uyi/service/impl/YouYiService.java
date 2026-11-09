package com.example.uyi.service.impl;

import com.example.uyi.entity.DeviceInfo;
import com.example.uyi.entity.EquipmentOrder;
import com.example.uyi.entity.UseEquipmentResponse;
import com.example.uyi.entity.UserInfo;
import com.example.uyi.exception.CustomException;
import com.example.uyi.mapper.UserMapper;
import com.example.uyi.service.youYiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;

/**
 * @Author: qdd
 * @Description: YouYiService
 * @DateTime: 2025/10/24 14:56
 **/
@Service
public class YouYiService implements youYiService {
    private static final String SECRET = "e275f1af-1dda-4b47-9d87-afcbe1f96dca";


    @Autowired
    private UserMapper userMapper;

    @Override
    public String generate_signature_from_params(Map<String, String> params) {
        try {
            // 过滤空值参数
            List<String> keysToSign = new ArrayList<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (value != null && !value.trim().isEmpty() && !"sign".equals(key)) {
                    keysToSign.add(key);
                }
            }
            // 排序keys：case-insensitive, tie-break by original key
            Collections.sort(keysToSign, new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    int lowerCompare = s1.toLowerCase().compareTo(s2.toLowerCase());
                    if (lowerCompare != 0) {
                        return lowerCompare;
                    }
                    return s1.compareTo(s2);
                }
            });
            // 构建参数字符串
            StringBuilder parts = new StringBuilder();
            for (int i = 0; i < keysToSign.size(); i++) {
                if (i > 0) {
                    parts.append("&");
                }
                String key = keysToSign.get(i);
                parts.append(key).append("=").append(params.get(key));
            }
            // 选择值：优先使用token，否则使用dateTime
            String token = params.get("token") != null ? params.get("token") : "";
            String dateTime = params.get("dateTime") != null ? params.get("dateTime") : "";
            String choice = !token.isEmpty() ? token : dateTime;
            // 清理版本号：移除前缀V/v和点号
            String versionName = params.get("AndroidVersionName") != null ? params.get("AndroidVersionName") : "";
            String cleanedVersion = versionName.replaceAll("^[Vv]+", "").replace(".", "");

            // 构建最终签名字符串
            String canonicalString = parts.toString() + "&" + choice + "&" + SECRET + "&" + cleanedVersion;

            // 计算SHA256签名
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(canonicalString.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("无法生成签名", e);
        }
    }

    @Override
    public UseEquipmentResponse request_Reshui(Map<String, String> params) {
        OkHttpClient client = new OkHttpClient();

        // 构建请求体
        StringBuilder requestBody = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                requestBody.append("&");
            }
            requestBody.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue());
            first = false;
        }

        // 创建请求
        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/x-www-form-urlencoded");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, requestBody.toString());

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://uyschool.uyxy.xin/ue/app/device/useEquipment")
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("User-Agent", "okhttp/4.9.0")
                .build();

        // 发送请求并处理响应
        try {
            okhttp3.Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                System.out.println("Request successful: " + responseBody);

                // 解析JSON响应
                ObjectMapper objectMapper = new ObjectMapper();
                UseEquipmentResponse result = objectMapper.readValue(responseBody, UseEquipmentResponse.class);
                if (result.getCode() == 0){
                    saveEquipmentOrderToDatabase(result, params);
                    return result;
                } else {
                    // 抛出自定义异常，正确传递错误码和错误信息
                    throw new CustomException(0, result.getMsg());
                }
            } else {
                // 处理失败 抛出异常
                throw new RuntimeException("请求失败，状态码：" + response.code());
            }
        } catch (CustomException e) {
            // 重新抛出自定义异常，让上层处理
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void saveEquipmentOrderToDatabase(UseEquipmentResponse result, Map<String, String> params) {
        // 创建设备订单对象
        EquipmentOrder equipmentOrder = new EquipmentOrder();
        equipmentOrder.setOrderNum(result.getData().getOrderNum());
        equipmentOrder.setTableName(result.getData().getTableName());
        equipmentOrder.setQueryId(result.getData().getQueryId());

        // 从参数中获取用户信息
        equipmentOrder.setUserId(params.get("userId"));
        equipmentOrder.setToken(params.get("token"));
        equipmentOrder.setDeviceInfId(params.get("deviceInfId"));

        // 设置createAt并添加日志
        equipmentOrder.setCreatedAt(new Date());

        // 保存到数据库
        userMapper.insertEquipmentOrder(equipmentOrder);
    }

    @Override
    public void request_Reshui_Async(Map<String, String> params) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // 构建请求体
        StringBuilder requestBody = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                requestBody.append("&");
            }
            requestBody.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue());
            first = false;
        }

        // 创建请求
        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/x-www-form-urlencoded");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, requestBody.toString());

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://uyschool.uyxy.xin/ue/app/device/useEquipment")
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("User-Agent", "okhttp/4.9.0")
                .build();

        // 发送请求并处理响应
        okhttp3.Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            String responseBody = response.body().string();
            System.out.println("Request successful: " + responseBody);
        }
    }

    @Override
    public String down_Reshui(Map<String, String> params) {
        OkHttpClient client = new OkHttpClient();

        // 构建请求体
        StringBuilder requestBody = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                requestBody.append("&");
            }
            requestBody.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue());
            first = false;
        }

        // 创建请求
        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/x-www-form-urlencoded");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, requestBody.toString());

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://uyschool.uyxy.xin/ue/app/device/endEquipment")
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("User-Agent", "okhttp/4.9.0")
                .build();

        // 发送请求并处理响应
        try {
            okhttp3.Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                System.out.println("End equipment request successful: " + responseBody);
                response.close();
                return responseBody; // 返回响应内容而不是null
            } else {
                System.err.println("End equipment request failed with code: " + response.code());
            }
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int verify_Code(Map<String, String> params) {
        try {
            OkHttpClient client = new OkHttpClient();

            // 生成签名
            String sign = generate_signature_from_params(params);
            params.put("sign", sign);

            // 构建请求体
            StringBuilder requestBody = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (!first) {
                    requestBody.append("&");
                }
                requestBody.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue());
                first = false;
            }

            // 创建请求
            okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/x-www-form-urlencoded");
            okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, requestBody.toString());

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url("https://uyschool.uyxy.xin/ue/app/user/sendRegisterMsg")
                    .post(body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("User-Agent", "okhttp/4.9.0")
                    .build();

            // 发送请求并处理响应
            try (okhttp3.Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    System.out.println("Verify code request successful");
                    return 1; // 成功
                } else {
                    System.err.println("Verify code request failed with code: " + response.code());
                    return -1; // 失败
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -2; // 异常
        }
    }

    @Override
    public String login(Map<String, String> params) {
        try {
            OkHttpClient client = new OkHttpClient();

            // 生成签名
            String sign = generate_signature_from_params(params);
            params.put("sign", sign);

            // 构建请求体
            StringBuilder requestBody = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (!first) {
                    requestBody.append("&");
                }
                requestBody.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue());
                first = false;
            }

            // 创建请求
            okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/x-www-form-urlencoded");
            okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, requestBody.toString());

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url("https://uyschool.uyxy.xin/ue/app/user/register")
                    .post(body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("User-Agent", "okhttp/4.9.0")
                    .build();

            // 发送请求并处理响应
            try (okhttp3.Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    System.out.println("Register request successful: " + responseBody);
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(responseBody);
                    // 提取用户信息
                    JsonNode dataNode = rootNode.get("data");
                    if (dataNode != null) {
                        //先查询是否有该用户
                        UserInfo userInfo = userMapper.queryByTel(params.get("tel"));
                        // 设置用户信息
                        UserInfo newUserInfo = new UserInfo();
                        newUserInfo.setUserId(dataNode.get("userId").asText());
                        newUserInfo.setToken(dataNode.get("token").asText());
                        newUserInfo.setTel(params.get("tel"));
                        
                        if (userInfo == null) {
                            // 插入用户信息
                            userMapper.insertUser(newUserInfo);
                        } else {
                            // 更新用户信息而不是插入新用户
                            userMapper.upDateUser(newUserInfo);
                        }
                    }
                    return responseBody; // 返回响应内容
                } else {
                    System.err.println("Register request failed with code: " + response.code());
                    return null; // 请求失败返回null
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 异常时返回null
        }
    }

    @Override
    public List<DeviceInfo> query_Equipment_List(Map<String, String> params) {
        try {
            OkHttpClient client = new OkHttpClient();

            // 生成签名
            String sign = generate_signature_from_params(params);
            params.put("sign", sign);

            // 构建请求体
            StringBuilder requestBody = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (!first) {
                    requestBody.append("&");
                }
                requestBody.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue());
                first = false;
            }

            // 创建请求
            okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/x-www-form-urlencoded");
            okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, requestBody.toString());

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url("https://uyschool.uyxy.xin/ue/app/device/findDeviceCommonlyByUserId")
                    .post(body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("User-Agent", "okhttp/4.9.0")
                    .build();

            // 发送请求并处理响应
            try (okhttp3.Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    System.out.println("Query equipment list request successful: " + responseBody);

                    // 解析JSON响应检查是否登录过期
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(responseBody);
                    int code = rootNode.get("code").asInt();
                    
                    // 如果返回码为107，表示登录过期
                    if (code == 107) {
                        // 直接抛出异常，将结果传递给调用方
                        throw new RuntimeException(responseBody);
                    }

                    JsonNode dataNode = rootNode.get("data");

                    if (dataNode != null) {
                        JsonNode listDeviceCommonly = dataNode.get("listDeviceCommonly");
                        if (listDeviceCommonly != null && listDeviceCommonly.isArray()) {
                            List<DeviceInfo> deviceList = new ArrayList<>();
                            for (JsonNode node : listDeviceCommonly) {
                                DeviceInfo deviceInfo = objectMapper.treeToValue(node, DeviceInfo.class);
                                deviceList.add(deviceInfo);
                            }
                            return deviceList;
                        }
                    }
                } else {
                    System.err.println("Query equipment list request failed with code: " + response.code());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        return List.of(); // 返回空列表如果请求失败或出现异常
    }




}