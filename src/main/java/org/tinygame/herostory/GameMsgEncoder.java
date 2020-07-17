package org.tinygame.herostory;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.msg.GameMsgProtocol;

public class GameMsgEncoder extends ChannelOutboundHandlerAdapter {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(GameMsgEncoder.class);

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (null == ctx ||
                null == msg) {
            return;
        }

        try {
            if (!(msg instanceof GeneratedMessageV3)) {
                super.write(ctx, msg, promise);
                return;
            }

            int msgCode = GameMsgRecognizer.getMsgCodeByClazz(msg.getClass());
            if (msgCode <= -1) {
                LOGGER.error("无法识别消息，msgClass = {}", msg.getClass().getSimpleName());
                super.write(ctx, msg, promise);
                return;
            }

            //异同点
            byte[] msgBody = ((GeneratedMessageV3) msg).toByteArray();
            //byte[] byteArray = ((GameMsgProtocol.UserEntryResult) msg).toByteArray();

            ByteBuf byteBuf = ctx.alloc().buffer();
            //异同点
            //byteBuf.writeShort((short) msgCode);
            byteBuf.writeShort((short) msgBody.length); // 消息的长度
            byteBuf.writeShort((short) msgCode);
            byteBuf.writeBytes(msgBody);

            BinaryWebSocketFrame frame = new BinaryWebSocketFrame(byteBuf);
            super.write(ctx, frame, promise);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
}
