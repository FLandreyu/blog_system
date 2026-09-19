package com.sunyongjie.blog.mapper;

import com.sunyongjie.blog.entity.User;
import org.apache.ibatis.annotations.Param;

/**
 * 用户表操作。SQL 全部写在 resources/mapper/UserMapper.xml。
 */
public interface UserMapper {

    User selectById(@Param("id") Long id);

    User selectByUsername(@Param("username") String username);

    /** 注册时的用户名唯一性校验 */
    int countByUsername(@Param("username") String username);

    int insert(User user);

    int updateProfile(User user);
}
