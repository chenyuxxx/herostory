package org.tinygame.herostory.model;

/**
 * 用户
 */
public class User {
    /**
     * 用户id
     */
    public int userId;

    /**
     * 用户名称
     */
    public String userName;

    /**
     * 英雄形象
     */
    public String heroAvatar;

    /**
     * 当前血量
     */
    public int currHP;

    /**
     * 移动状态
     */
    public final MoveState moveState = new MoveState();

    /**
     * 死亡状态
     */
    public boolean died;
}
