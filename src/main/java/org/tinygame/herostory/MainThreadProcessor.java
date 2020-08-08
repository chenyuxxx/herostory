package org.tinygame.herostory;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.cmdhandle.CmdHandlerFactory;
import org.tinygame.herostory.cmdhandle.ICmdHandler;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

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
     * 创建一个单线程
     */
    private final ExecutorService _es = Executors.newSingleThreadExecutor(r -> {
        Thread newThread = new Thread(r);
        newThread.setName("MainThreadProcessor");
        return newThread;
    });

    /**
     * 处理消息
     * @param ctx 客户端信道上下文
     * @param msg 消息对象
     */
    public void process(ChannelHandlerContext ctx, GeneratedMessageV3 msg){
        if (null == ctx || null == msg){
            return;
        }

        //获取消息处理器代码写成函数存入 Lambda 表达式，包装成一个 Runnable 对象
        //相当于单线程处理消息，并提交到 ExecutorService 中
        //线程中上下文切换，效率是要比 synchronized 关键字要更高，因为加 synchronized 锁粒度不好控制
        //游戏服务器真实项目中，也是做单线程消息处理，多线程环境下，如果出错，查错难度太大
        this._es.submit(() -> {
            //获取消息类
            Class<?> msgClazz = msg.getClass();

            ICmdHandler<? extends GeneratedMessageV3> cmdHandler = CmdHandlerFactory.create(msgClazz);

            if (null == cmdHandler) {
                LOGGER.error(
                        "未找到相应的指令处理器，msgclazz = {}",
                        msgClazz.getName()
                );
            }
            try {
                cmdHandler.handle(ctx, cast(msg));
            } catch (Exception e) {
                LOGGER.error(e.getMessage(),e);
            }
        });
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
