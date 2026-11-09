package com.example.uyi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @Author: qdd
 * @Description: UseEquipmentResponse
 * @DateTime: 2025/10/24 16:46
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UseEquipmentResponse {
    private String msg;
    private int code;
    private Data data;

    // getters and setters
    public String getMsg() {
        return msg;
    }

    public int getCode() {
        return code;
    }

    public Data getData() {
        return data;
    }

   @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        private String orderNum;
        private String tableName;
        private String queryId;
    }
}