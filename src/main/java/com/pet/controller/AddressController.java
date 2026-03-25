package com.pet.controller;

import com.pet.entity.Address;
import com.pet.service.AddressService;
import com.pet.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/address")
@CrossOrigin(origins = "*")
public class AddressController {

    @Autowired
    private AddressService addressService;

    private Integer getUserId(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        return userId;
    }

    /**
     * 获取地址列表
     */
    @GetMapping("/list")
    public Result<List<Address>> getAddressList(HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            List<Address> list = addressService.getAddressList(userId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取默认地址
     */
    @GetMapping("/default")
    public Result<Address> getDefaultAddress(HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            Address address = addressService.getDefaultAddress(userId);
            return Result.success(address);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加地址
     */
    @PostMapping("/add")
    public Result<?> addAddress(@RequestBody Address address, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            boolean success = addressService.addAddress(address, userId);
            if (success) {
                return Result.success("添加成功");
            } else {
                return Result.error("添加失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新地址
     */
    @PutMapping("/update")
    public Result<?> updateAddress(@RequestBody Address address) {
        try {
            boolean success = addressService.updateAddress(address);
            if (success) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteAddress(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            boolean success = addressService.deleteAddress(id, userId);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/default/{id}")
    public Result<?> setDefaultAddress(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer userId = getUserId(request);
            boolean success = addressService.setDefaultAddress(id, userId);
            if (success) {
                return Result.success("设置成功");
            } else {
                return Result.error("设置失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}