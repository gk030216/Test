package com.pet.mapper;

import com.pet.entity.OrderItem;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface OrderItemMapper {
    int batchInsert(@Param("list") List<OrderItem> list);
    List<OrderItem> getByOrderNo(@Param("orderNo") String orderNo);
}