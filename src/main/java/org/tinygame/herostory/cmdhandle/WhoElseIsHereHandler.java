package org.tinygame.herostory.cmdhandle;

import io.netty.channel.ChannelHandlerContext;
import org.tinygame.herostory.model.MoveState;
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

            //获取移动状态
            MoveState moveState = currUser.moveState;
            GameMsgProtocol.WhoElseIsHereResult.UserInfo.MoveState.Builder
                    mvStateBuilder = GameMsgProtocol.WhoElseIsHereResult.UserInfo.MoveState.newBuilder();
            mvStateBuilder.setFromPosX(moveState.fromPosX);
            mvStateBuilder.setFromPosY(moveState.fromPosY);
            mvStateBuilder.setToPosX(moveState.toPosX);
            mvStateBuilder.setToPosY(moveState.toPosY);
            mvStateBuilder.setStartTime(moveState.startTime);
            //将移动状态设置到用户信息
            userInfoBuider.setMoveState(mvStateBuilder);

            resultBuider.addUserInfo(userInfoBuider);
        }

        GameMsgProtocol.WhoElseIsHereResult newResult = resultBuider.build();
        ctx.writeAndFlush(newResult);
    }
}
