package org.tinygame.herostory.model;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户管理器
 */
public final class UserManger {
    private UserManger() {
    }

    /**
     * 用户字典
     */
    static private final Map<Integer, User> _userMap = new ConcurrentHashMap<>();

    /**
     * 添加用户
     *
     * @param newUser
     */
    static public void addUser(User newUser){
        if (null == newUser) return;
        _userMap.put(newUser.userId,newUser);
    }

    /**
     * 根据用户id删除用户
     *
     * @param userId
     */
    static public void removeUser(int userId){
        _userMap.remove(userId);
    }

    /**
     * 用户列表
     *
     * @return
     */
    static public Collection<User> listUser(){
        return _userMap.values();
    }

    /**
     * 根据id获取用户
     *
     * @param userId
     * @return
     */
    static public User getUserById(int userId){
        return _userMap.get(userId);
    }
}
