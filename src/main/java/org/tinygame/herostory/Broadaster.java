package org.tinygame.herostory;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * 广播管理器
 *
 */
public final class Broadaster {
    /**
     * 私有化类为默认构造器
     *
     */
    private Broadaster() {
    }


    /**
     * 客户端通信数组，一定要使用 static ，否则无法实现群发功能
     *
     */
    static private final ChannelGroup _channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 添加一个信道
     *
     * @param channel
     */
    static public void addChannel(Channel channel) {
        _channelGroup.add(channel);
    }

    /**
     * 移除信道
     *
     * @param channel
     */
    static public void removeChannel(Channel channel) {
        _channelGroup.remove(channel);
    }

    /**
     * 广播消息
     *
     * @param msg
     */
    static public void broadcast(Object msg) {
        if (null == msg){
            return;
        }

        _channelGroup.writeAndFlush(msg);
    }
}
