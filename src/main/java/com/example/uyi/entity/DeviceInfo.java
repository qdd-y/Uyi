package com.example.uyi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @Author: qdd
 * @Description: Device
 * @DateTime: 2025/10/25 14:53
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceInfo {
    private String wayName;
    private Integer deviceInfId;
    private String deviceName;
    private String deviceTypeId; // 添加此字段
}