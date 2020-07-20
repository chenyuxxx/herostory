package org.tinygame.herostory.cmdhandle;

import io.netty.channel.ChannelHandlerContext;
import org.tinygame.herostory.model.User;
import org.tinygame.herostory.model.UserManger;
import org.tinygame.herostory.msg.GameMsgProtocol;

public class WhoElseIsHereHandler implements ICmdHandler<GameMsgProtocol.WhoElseIsHereCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.WhoElseIsHereCmd msg) {
        GameMsgProtocol.WhoElseIsHereResult.Builder resultBuider = GameMsgProtocol.WhoElseIsHereResult.newBuilder();

        for (User currUser : UserManger.listUser()) {
            if (null == currUser) {
                continue;
            }
            GameMsgProtocol.WhoElseIsHereResult.UserInfo.Builder userInfoBuider = GameMsgProtocol.WhoElseIsHereResult.UserInfo.newBuilder();
            userInfoBuider.setUserId(currUser.userId);
            userInfoBuider.setHeroAvatar(currUser.heroAvatar);
            resultBuider.addUserInfo(userInfoBuider);
        }

        GameMsgProtocol.WhoElseIsHereResult newResult = resultBuider.build();
        ctx.writeAndFlush(newResult);
    }
}
