package com.pet.mapper;

import com.pet.entity.Address;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AddressMapper {

    List<Address> getAddressList(@Param("userId") Integer userId);

    Address getDefaultAddress(@Param("userId") Integer userId);

    Address getById(@Param("id") Integer id);

    int insert(Address address);

    int update(Address address);

    int delete(@Param("id") Integer id);

    int clearDefault(@Param("userId") Integer userId);

    int updateDefault(@Param("id") Integer id);
}