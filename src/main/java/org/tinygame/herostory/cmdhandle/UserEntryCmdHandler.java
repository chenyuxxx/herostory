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
        if (null == ctx
                || null == msg) {
            return;
        }

        // 从指令对象中获取用户id跟英雄形象
        GameMsgProtocol.UserEntryCmd cmd = msg;
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        if (null == userId) {
            return;
        }

        //获取已存在用户
        User existUser = UserManger.getUserById(userId);
        if (null == existUser) {
            return;
        }

        //获取英雄形象
        String heroAvatar = existUser.heroAvatar;

        GameMsgProtocol.UserEntryResult.Builder resultBuider = GameMsgProtocol.UserEntryResult.newBuilder();
        resultBuider.setUserId(userId);
        resultBuider.setHeroAvatar(heroAvatar);

        //构建结果并发送
        GameMsgProtocol.UserEntryResult newResult = resultBuider.build();
        Broadcaster.broadcast(newResult);
    }
}
