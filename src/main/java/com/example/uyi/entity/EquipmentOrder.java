// EquipmentOrder.java
package com.example.uyi.entity;

import lombok.Data;

import java.util.Date;


@Data
public class EquipmentOrder {
    private Integer id;
    private String orderNum;
    private String tableName;
    private String queryId;
    private String userId;
    private String token;
    private String deviceInfId;
    private Date createdAt;
    private String tel;
}
