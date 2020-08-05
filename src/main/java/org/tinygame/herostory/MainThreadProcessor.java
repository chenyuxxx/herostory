package org.tinygame.herostory;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.cmdhandle.CmdHandlerFactory;
import org.tinygame.herostory.cmdhandle.ICmdHandler;

/**
 * 主线程处理器
 */
public final class MainThreadProcessor {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(MainThreadProcessor.class);

    /**
     * 单例对象
     */
    private static final MainThreadProcessor _instance = new MainThreadProcessor();

    /**
     * 私有化类默认构造器
     */
    private MainThreadProcessor() {
    }

    /**
     * 获取单例对象
     *
     * @return 主线程处理器
     */
    public static MainThreadProcessor getInstance(){
        return _instance;
    }

    /**
     * 处理消息
     * @param ctx 客户端信道上下文
     * @param msg 消息对象
     */
    public void process(ChannelHandlerContext ctx, GeneratedMessageV3 msg){
        if (null == ctx || null == msg){
            return;
        }

        //获取消息类
        Class<?> msgClazz = msg.getClass();

        ICmdHandler<? extends GeneratedMessageV3> cmdHandler = CmdHandlerFactory.create(msgClazz);

        if (null == cmdHandler) {
            LOGGER.error(
                    "未找到相应的指令处理器，msgclazz = {}",
                    msgClazz.getName()
            );
        }
        cmdHandler.handle(ctx, cast(msg));
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
