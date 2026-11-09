package com.example.uyi.controller;

import com.example.uyi.entity.DeviceInfo;
import com.example.uyi.entity.Result;
import com.example.uyi.entity.UserInfo;
import com.example.uyi.mapper.UserMapper;
import com.example.uyi.service.youYiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: qdd
 * @Description: DeviceController
 * @DateTime: 2025/10/25 15:00
 **/
@RestController
@Slf4j
@CrossOrigin
public class DeviceController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private youYiService youYiService;

    //获取设备信息
    @GetMapping("/get/device")
    public Result<Map<String, Object>>  getDeviceInfo(String tel) throws NoSuchAlgorithmException {
        log.info("=== 测试获取设备信息 ===");
        //查询用户信息
        UserInfo userInfo = userMapper.queryByTel(tel);
        String token = userInfo.getToken();
        String userId = userInfo.getUserId();

        //构建参数
        //构建参数
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("token", token);
        params.put("AndroidVersionName", "V2.8.1");
        params.put("AndroidVersionCode", "81");
        params.put("dateTime", String.valueOf(System.currentTimeMillis()));

        // 创建返回结果Map
        Map<String, Object> resultData = new HashMap<>();
        
        try {
            List<DeviceInfo> deviceInfo = youYiService.query_Equipment_List(params);
            resultData.put("devices", deviceInfo);
            resultData.put("message", "获取设备信息成功");
            return Result.success(resultData);
        } catch (Exception e) {
            log.error("获取设备信息失败", e);
            // 检查是否是登录过期的错误信息
            if (e.getMessage() != null && e.getMessage().contains("\"code\":107")) {
                // 直接将登录过期的完整响应返回给前端
                resultData.put("error", "登录过期");
                resultData.put("rawResponse", e.getMessage());
                return Result.error("登录过期，请重新登录");
            }
            resultData.put("message", "获取设备信息失败");
            resultData.put("error", e.getMessage());
            return Result.error("获取设备信息失败: " + e.getMessage());
        }
    }

}