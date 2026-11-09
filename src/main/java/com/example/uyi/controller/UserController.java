package com.example.uyi.controller;

import com.example.uyi.entity.Result;
import com.example.uyi.service.youYiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: qdd
 * @Description: UserController
 * @DateTime: 2025/10/25 12:47
 **/
@RestController
@Slf4j
@CrossOrigin
public class UserController {

    @Autowired
    private youYiService youYiService;

    @PostMapping("/verifyCode")
    public Result<String> verifyCode(@RequestParam String tel){
        long currentTime = System.currentTimeMillis();
        System.out.println("=== 测试验证码发送 ===");
        Map<String, String> params = new HashMap<>();

        // 添加请求参数
        params.put("tel", tel);
        params.put("AndroidVersionName", "V2.8.1");
        params.put("AndroidVersionCode", "81");
        params.put("dateTime", String.valueOf(currentTime));

        // 调用验证码发送方法
        int result = youYiService.verify_Code(params);
        if (result == -1){
            return Result.error("验证码发送失败");
        }
        return Result.success("验证码发送结果: " + result);
    }

    @RequestMapping("/login")
    public Result< String> login(String tel, String code){
        long currentTime = System.currentTimeMillis();
        System.out.println("=== 测试登录 ===");
        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        params.put("tel", tel);
        params.put("deviceModel", "V2283A");
        params.put("type", "5");
        params.put("deviceName", "a7e0ac27472693a2");
        params.put("AndroidVersionName", "V2.8.1");
        params.put("AndroidVersionCode", "81");
        params.put("dateTime", String.valueOf(currentTime));
        String result = youYiService.login(params);
        return Result.success("登录结果: " + result);
    }
}