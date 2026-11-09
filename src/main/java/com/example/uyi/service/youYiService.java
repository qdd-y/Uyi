package com.example.uyi.service;

import com.example.uyi.entity.DeviceInfo;
import com.example.uyi.entity.UseEquipmentResponse;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * @Author: qdd
 * @Description: youYiService
 * @DateTime: 2025/10/24 14:40
 **/
public interface youYiService {

    // 生成签名
    String generate_signature_from_params(Map<String, String> params) throws NoSuchAlgorithmException;

    // 请求reshui
    UseEquipmentResponse request_Reshui(Map<String, String> params);

    // 异步请求reshui
    void request_Reshui_Async(Map<String, String> params) throws IOException;

    // 结束reshui
    String down_Reshui(Map<String, String> params) ;

    // 验证码
    int  verify_Code(Map<String, String>  params);

    //登录
    String login(Map<String, String> params);

    //查询设备列表
    List<DeviceInfo> query_Equipment_List(Map<String, String> params);

}