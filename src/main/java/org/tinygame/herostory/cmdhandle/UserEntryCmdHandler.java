package org.tinygame.herostory.cmdhandle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.tinygame.herostory.Broadcaster;
import org.tinygame.herostory.model.User;
import org.tinygame.herostory.model.UserManger;
import org.tinygame.herostory.msg.GameMsgProtocol;

public class UserEntryCmdHandler implements ICmdHandler<GameMsgProtocol.UserEntryCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.UserEntryCmd msg) {
        // 从指令对象中获取用户id跟英雄形象
        GameMsgProtocol.UserEntryCmd cmd = msg;
        int userId = cmd.getUserId();
        String heroAvatar = cmd.getHeroAvatar();

        GameMsgProtocol.UserEntryResult.Builder resultBuider = GameMsgProtocol.UserEntryResult.newBuilder();
        resultBuider.setUserId(userId);
        resultBuider.setHeroAvatar(heroAvatar);

        //将用户加入字典
        User newUser = new User();
        newUser.userId = userId;
        newUser.heroAvatar = heroAvatar;
        newUser.currHP = 100;
        UserManger.addUser(newUser);

        //将用户 id 附着到channnel
        ctx.channel().attr(AttributeKey.valueOf("userId")).set(userId);
        //构建结果并发送
        GameMsgProtocol.UserEntryResult newResult = resultBuider.build();
        Broadcaster.broadcast(newResult);
    }
}
