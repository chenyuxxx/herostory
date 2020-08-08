package org.tinygame.herostory.login.db;

import org.apache.ibatis.annotations.Param;

public interface IUserDao {

    /**
     * 根据用户名称获取用户信息
     *
     * @param userName
     * @return
     */
    UserEntity getUserById(@Param("userName")String userName);

    /**
     * 添加用户实体
     *
     * @param userEntity 用户实体
     */
    void insertInto(UserEntity userEntity);
}
