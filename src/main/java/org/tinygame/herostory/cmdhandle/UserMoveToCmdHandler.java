package org.tinygame.herostory.cmdhandle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.tinygame.herostory.Broadaster;
import org.tinygame.herostory.msg.GameMsgProtocol;

public class UserMoveToCmdHandler implements ICmdHandler<GameMsgProtocol.UserMoveToCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.UserMoveToCmd msg) {
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();

        if (null == userId) {
            return;
        }

        GameMsgProtocol.UserMoveToCmd cmd = msg;

        GameMsgProtocol.UserMoveToResult.Builder resultBuider = GameMsgProtocol.UserMoveToResult.newBuilder();
        resultBuider.setMoveUserId(userId);
        resultBuider.setMoveToPosX(cmd.getMoveToPosX());
        resultBuider.setMoveToPosY(cmd.getMoveToPosY());

        GameMsgProtocol.UserMoveToResult newResult = resultBuider.build();
        Broadaster.broadcast(newResult);
    }
}
