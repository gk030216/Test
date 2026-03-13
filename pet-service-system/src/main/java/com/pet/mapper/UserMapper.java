package com.pet.mapper;

import com.pet.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {

    /**
     * 根据用户名查询
     */
    User findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询
     */
    User findByEmail(@Param("email") String email);

    /**
     * 根据用户名或邮箱查询
     */
    User findByUsernameOrEmail(@Param("account") String account);

    /**
     * 插入用户
     */
    int insert(User user);

    /**
     * 更新用户
     */
    int update(User user);

    /**
     * 更新最后登录时间
     */
    int updateLastLoginTime(@Param("id") Integer id);

    /**
     * 更新密码
     */
    int updatePassword(@Param("email") String email, @Param("password") String password);
}