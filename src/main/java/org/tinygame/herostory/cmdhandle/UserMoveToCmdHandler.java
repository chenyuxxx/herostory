package org.tinygame.herostory.cmdhandle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.tinygame.herostory.Broadcaster;
import org.tinygame.herostory.model.MoveState;
import org.tinygame.herostory.model.User;
import org.tinygame.herostory.model.UserManger;
import org.tinygame.herostory.msg.GameMsgProtocol;

public class UserMoveToCmdHandler implements ICmdHandler<GameMsgProtocol.UserMoveToCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.UserMoveToCmd msg) {
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();

        if (null == userId) {
            return;
        }

        //获取移动的用户
        User moveUser = UserManger.getUserById(userId);
        if (null == moveUser){
            return;
        }

        GameMsgProtocol.UserMoveToCmd cmd = msg;
        //设置位置和时间
        MoveState moveState = moveUser.moveState;
        moveState.fromPosX = cmd.getMoveFromPosX();
        moveState.fromPosY = cmd.getMoveFromPosY();
        moveState.toPosX = cmd.getMoveToPosX();
        moveState.toPosY = cmd.getMoveToPosY();
        moveState.startTime = System.currentTimeMillis();


        GameMsgProtocol.UserMoveToResult.Builder resultBuider = GameMsgProtocol.UserMoveToResult.newBuilder();
        resultBuider.setMoveUserId(userId);
        resultBuider.setMoveFromPosX(moveState.fromPosX);
        resultBuider.setMoveFromPosY(moveState.fromPosY);
        resultBuider.setMoveToPosX(moveState.toPosX);
        resultBuider.setMoveToPosY(moveState.toPosY);
        resultBuider.setMoveStartTime(moveState.startTime);

        GameMsgProtocol.UserMoveToResult newResult = resultBuider.build();
        Broadcaster.broadcast(newResult);
    }
}
