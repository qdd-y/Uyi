package com.example.uyi.mapper;

import com.example.uyi.entity.EquipmentOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: qdd
 * @Description: EquipmentMapper
 * @DateTime: 2025/10/25 14:13
 **/
@Mapper
public interface EquipmentMapper {

    EquipmentOrder queryByUserId(String userId);

    void deleteById(String orderNum );

    EquipmentOrder queryByUserIdAndDeviceInfId(String userId, String deviceInfId);
}
