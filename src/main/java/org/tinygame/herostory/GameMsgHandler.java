package org.tinygame.herostory;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.cmdHandle.*;
import org.tinygame.herostory.model.UserManger;
import org.tinygame.herostory.msg.GameMsgProtocol;

/**
 * 自定义消息处理器
 */
public class GameMsgHandler extends SimpleChannelInboundHandler {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(GameMsgHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Broadaster.addChannel(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);

        Broadaster.removeChannel(ctx.channel());

        //先拿到用户id
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();

        if (null == userId) {
            return;
        }

        UserManger.removeUser(userId);

        GameMsgProtocol.UserQuitResult.Builder resultBuider = GameMsgProtocol.UserQuitResult.newBuilder();
        resultBuider.setQuitUserId(userId);

        GameMsgProtocol.UserQuitResult newResult = resultBuider.build();
        Broadaster.broadcast(newResult);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (null == ctx ||
                null == msg) {
            return;
        }

        LOGGER.info(
                "收到客户!!!!端消息, msgClazz = {}, msgBody = {}",
                msg.getClass().getSimpleName(),
                msg
        );

        ICmdHandler<? extends GeneratedMessageV3> cmdHandler = CmdHandlerFactory.create(msg.getClass());

        if (null != cmdHandler) {
            cmdHandler.handle(ctx, cast(msg));
        }
    }

    /**
     * 转型为命令对象
     *
     * @param msg
     * @param <TCmd>
     * @return
     */
    static private <TCmd extends GeneratedMessageV3> TCmd cast(Object msg) {
        if (null == msg) {
            return null;
        } else {
            return (TCmd) msg;
        }
    }

}
