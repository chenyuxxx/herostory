package org.tinygame.herostory.cmdhandle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.Broadcaster;
import org.tinygame.herostory.async.AsyncOperationProcessor;
import org.tinygame.herostory.login.LoginService;
import org.tinygame.herostory.login.db.UserEntity;
import org.tinygame.herostory.model.User;
import org.tinygame.herostory.model.UserManger;
import org.tinygame.herostory.msg.GameMsgProtocol;

public class UserLoginHandler implements ICmdHandler<GameMsgProtocol.UserLoginCmd> {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(UserLoginHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.UserLoginCmd cmd) {
        if (null == ctx ||
                null == cmd) {
            return;
        }

        String userName = cmd.getUserName();
        String password = cmd.getPassword();

        LOGGER.info("userName = {} , password = {}",
                userName,
                password
        );

        LoginService.getInstance().userLogin(userName, password, (userEntity) -> {
            if (null == userEntity) {
                LOGGER.error("用户登录失败，登录用户名 = {}", cmd.getUserName());
                return null;
            }

            LOGGER.info("当前线程 = {}", Thread.currentThread().getName());
            LOGGER.info("用户登录成功，userId = {}，userName = {}",
                    userEntity.getUserId(),
                    userEntity.getUserName()
            );

            // 从指令对象中获取用户id跟英雄形象
            int userId = userEntity.getUserId();//cmd.getUserId();
            String heroAvatar = userEntity.getHeroAvatar();//cmd.getHeroAvatar();

            //将用户加入字典
            User newUser = new User();
            newUser.userId = userId;
            newUser.userName = userEntity.getUserName();
            newUser.heroAvatar = heroAvatar;
            newUser.currHP = 100;
            UserManger.addUser(newUser);

            //将用户 id 附着到channnel
            ctx.channel().attr(AttributeKey.valueOf("userId")).set(userId);

            //构建结果并发送
            GameMsgProtocol.UserLoginResult.Builder resultBuider = GameMsgProtocol.UserLoginResult.newBuilder();
            resultBuider.setUserId(newUser.userId);
            resultBuider.setUserName(newUser.userName);
            resultBuider.setHeroAvatar(newUser.heroAvatar);

            //构建结果并发送
            GameMsgProtocol.UserLoginResult newResult = resultBuider.build();
            ctx.writeAndFlush(newResult);

            Broadcaster.broadcast(newResult);

            return null;
        });
    }
}
