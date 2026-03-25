// AddressServiceImpl.java
package com.pet.service.impl;

import com.pet.entity.Address;
import com.pet.mapper.AddressMapper;
import com.pet.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public List<Address> getAddressList(Integer userId) {
        return addressMapper.getAddressList(userId);
    }

    @Override
    public Address getDefaultAddress(Integer userId) {
        return addressMapper.getDefaultAddress(userId);
    }

    @Override
    public Address getById(Integer id) {
        return addressMapper.getById(id);
    }

    @Override
    @Transactional
    public boolean addAddress(Address address, Integer userId) {
        address.setUserId(userId);
        address.setIsDefault(address.getIsDefault() == null ? 0 : address.getIsDefault());

        if (address.getIsDefault() == 1) {
            // 清除其他默认地址
            addressMapper.clearDefault(userId);
        }

        return addressMapper.insert(address) > 0;
    }

    @Override
    @Transactional
    public boolean updateAddress(Address address) {
        if (address.getIsDefault() == 1) {
            // 清除其他默认地址
            addressMapper.clearDefault(address.getUserId());
        }
        return addressMapper.update(address) > 0;
    }

    @Override
    public boolean deleteAddress(Integer id, Integer userId) {
        Address address = addressMapper.getById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            return false;
        }
        return addressMapper.delete(id) > 0;
    }

    @Override
    @Transactional
    public boolean setDefaultAddress(Integer id, Integer userId) {
        addressMapper.clearDefault(userId);
        return addressMapper.updateDefault(id) > 0;
    }
}