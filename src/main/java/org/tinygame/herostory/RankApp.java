package org.tinygame.herostory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.mq.MQConsumer;
import org.tinygame.herostory.util.RedisUtil;

/**
 * 排行榜应用程序
 */
public class RankApp {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(RankApp.class);

    /**
     * 应用入口函数
     *
     * @param args args命令行参数数组
     */
    public static void main(String[] args) {
        //初始化Redis连接
        RedisUtil.init();
        //初始化消费者MQ
        MQConsumer.init();

        LOGGER.info("排行榜应用程序启动！");
    }
}
