package com.example.uyi.controller;

import com.example.uyi.entity.EquipmentOrder;
import com.example.uyi.entity.Result;
import com.example.uyi.entity.UserInfo;
import com.example.uyi.mapper.EquipmentMapper;
import com.example.uyi.mapper.UserMapper;
import com.example.uyi.service.youYiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: qdd
 * @Description: ActivityController
 * @DateTime: 2025/10/25 13:39
 **/
@RestController
@Slf4j
@CrossOrigin
public class ActivityController {
    @Autowired
    private youYiService youYiService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @GetMapping("/open/hot")
    public Result<String> openHot(String  tel,String deviceInfId,String billType) throws NoSuchAlgorithmException, IOException {
        log.info("=== 测试开启热水 ===");
        UserInfo userInfo = userMapper.queryByTel(tel);
        String token = userInfo.getToken();
        String userId = userInfo.getUserId();

        //构造参数1 billType 2是热水 1是直饮水水
        Map<String, String> params1 = new HashMap<>();
        params1.put("orderWay", "1");
        params1.put("theConnectionMethod", "2");
        params1.put("deviceInfId", deviceInfId);
        params1.put("billType", billType);
        params1.put("lackBalance", "lackBalance");
        params1.put("type", "1");
        params1.put("userId", userId);
        params1.put("token", token);
        params1.put("AndroidVersionName", "V2.8.1");
        params1.put("AndroidVersionCode", "81");
        params1.put("dateTime", String.valueOf(System.currentTimeMillis()));
        String signature1 = youYiService.generate_signature_from_params(params1);
        params1.put("sign", signature1);
        youYiService.request_Reshui_Async(params1);
        //构造参数2
        Map<String, String> params = new HashMap<>();
        params.put("orderWay", "1");
        params.put("theConnectionMethod", "2");
        params.put("deviceInfId", deviceInfId);
        params.put("billType", billType); //2
        params.put("lackBalance", "lackBalance");
        params.put("type", "0");
        params.put("userId", userId);
        params.put("token", token);
        params.put("AndroidVersionName", "V2.8.1");
        params.put("AndroidVersionCode", "81");
        params.put("dateTime", String.valueOf(System.currentTimeMillis()));
        //生成签名
        String signature = youYiService.generate_signature_from_params(params);
        params.put("sign", signature);
        youYiService.request_Reshui(params);
        return Result.success("开启热水成功");
    }

    //关闭热水
    @GetMapping("/close/hot")
    public Result<Map<String, Object>> closeHot(String  tel,String deviceInfId) throws NoSuchAlgorithmException {

        log.info("=== 测试关闭热水 ===");
        UserInfo userInfo = userMapper.queryByTel(tel);
        String token = userInfo.getToken();
        String userId = userInfo.getUserId();


        //查询订单号
        EquipmentOrder equipmentOrder = equipmentMapper.queryByUserIdAndDeviceInfId(userId, deviceInfId);
        Integer id = equipmentOrder.getId();
        String orderNum = equipmentOrder.getOrderNum();
        String queryId = equipmentOrder.getQueryId();
        String tableName = equipmentOrder.getTableName();

        // 构造参数
        Map<String, String> params = new HashMap<>();
        params.put("orderNum", orderNum); // 添加订单号
        params.put("userId", userId);
        params.put("mac", ""); // 添加 mac 参数
        params.put("token", token);
        params.put("tableName", tableName); // 添加表名
        params.put("AndroidVersionName", "V2.8.1");
        params.put("AndroidVersionCode", "81");
        params.put("dateTime", String.valueOf(System.currentTimeMillis()));
        String signature = youYiService.generate_signature_from_params(params);
        params.put("sign", signature);
        String response = youYiService.down_Reshui(params);

        equipmentMapper.deleteById(orderNum);
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("message", "关闭热水成功");
        
        // 添加空值检查
        if (response != null && !response.isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response);
                String expAmount = rootNode.path("data").path("billingDetail").path("expAmount").asText();
                resultData.put("expAmount", expAmount);
                resultData.put("response", response); // 临时返回完整响应供调试
            } catch (Exception e) {
                log.error("解析响应失败", e);
                resultData.put("error", "解析响应数据失败");
            }
        } else {
            log.warn("down_Reshui 返回空响应");
            resultData.put("expAmount", "0.00");
            resultData.put("error", "未收到有效响应数据");
        }
        
        return Result.success(resultData);
    }
}