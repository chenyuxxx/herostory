package org.tinygame.herostory;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.msg.GameMsgProtocol;

import java.nio.ByteBuffer;

/**
 * 自定义的解码器
 */
public class GameMsgDecoder extends ChannelInboundHandlerAdapter {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(GameMsgDecoder.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (null == ctx || null == msg) {
            return;
        }

        if (!(msg instanceof BinaryWebSocketFrame)){
            return;
        }

        // websocket 二进制消息会通过 HttpServerCodec 解码成 BinaryWebSocketFrame 类对象
        BinaryWebSocketFrame frame = (BinaryWebSocketFrame) msg;
        ByteBuf byteBuf = frame.content();

        byteBuf.readShort(); // 读取消息的长度
        int msgCode = byteBuf.readShort(); // 读取消息编号

        // 获取消息构建者
        // 不要写在拿到消息体步骤下面，写在底下会多执行一步的读字节操作，写在上面可以节省资源
        Message.Builder msgBuilder = GameMsgRecognizer.getBuilderByMsgCode(msgCode);
        if (null == msgBuilder) {
            LOGGER.error("无法识别的消息， msgCode = {}",msgCode);
            return;
        }

        // 拿到消息体
        byte[] msgBody = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(msgBody);

        msgBuilder.clear();
        msgBuilder.mergeFrom(msgBody);
        //构建消息
        Message newMessage  = msgBuilder.build();

        // 解码
        if (null != newMessage){
            ctx.fireChannelRead(newMessage);
        }

    }
}
