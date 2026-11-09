package com.example.uyi.mapper;

import com.example.uyi.entity.EquipmentOrder;
import com.example.uyi.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: qdd
 * @Description: UserMapper
 * @DateTime: 2025/10/25 13:30
 **/
@Mapper
public interface UserMapper {
    void insertUser(UserInfo userInfo);

    UserInfo queryByTel(String tel);

    void insertEquipmentOrder(EquipmentOrder equipmentOrder);
    
    void upDateUser(UserInfo userInfo);
}