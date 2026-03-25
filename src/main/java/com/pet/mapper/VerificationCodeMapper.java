package com.pet.mapper;

import com.pet.entity.VerificationCode;
import org.apache.ibatis.annotations.Param;

public interface VerificationCodeMapper {

    /**
     * 插入验证码
     */
    int insert(VerificationCode code);

    /**
     * 查询最新的未使用验证码
     */
    VerificationCode findLatestValidCode(@Param("email") String email,
                                         @Param("type") Integer type);

    /**
     * 将验证码标记为已使用
     */
    int markAsUsed(@Param("id") Integer id);

    /**
     * 删除过期验证码
     */
    int deleteExpired();
}