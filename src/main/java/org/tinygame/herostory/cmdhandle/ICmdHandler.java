package org.tinygame.herostory.cmdhandle;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandlerContext;

/**
 * 指令处理接口
 *
 * @param <TCmd>
 */
public interface ICmdHandler<TCmd extends GeneratedMessageV3> {
    /**
     * 处理指令
     *
     * @param ctx
     * @param tCmd
     */
    void handle(ChannelHandlerContext ctx,TCmd tCmd);
}
