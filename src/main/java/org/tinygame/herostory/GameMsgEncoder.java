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
    static private final Logger LOGGER = LoggerFactory.getLogger(GameMsgEncoder.class);

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (null == msg || !(msg instanceof GeneratedMessageV3)) {
            super.write(ctx, msg, promise);
            return;
        }

        int msgCode = GameMsgRecognizer.getMsgCodeByMsgClass(msg.getClass());
        if (msgCode <= -1){
            LOGGER.error("无法识别消息，msgClass = {}",msg.getClass().getName());
            return;
        }

        byte[] byteArray = ((GameMsgProtocol.UserEntryResult) msg).toByteArray();

        ByteBuf byteBuf = ctx.alloc().buffer();
        byteBuf.writeShort((short) msgCode);
        byteBuf.writeShort(msgCode);
        byteBuf.writeBytes(byteArray);

        BinaryWebSocketFrame frame = new BinaryWebSocketFrame(byteBuf);
        super.write(ctx, frame, promise);
    }
}
