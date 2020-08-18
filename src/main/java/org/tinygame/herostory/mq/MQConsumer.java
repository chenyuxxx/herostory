package org.tinygame.herostory.mq;

import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.rank.RankService;

import java.util.List;

/**
 * 消息队列消费者
 */
public class MQConsumer {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(MQConsumer.class);

    /**
     * 私有化类默认构造器
     */
    private MQConsumer() {
    }

    /**
     * 初始化
     */
    public static void init() {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("herostory");
        consumer.setNamesrvAddr("127.0.0.1:9876");

        try {
            consumer.subscribe("Victor", "*");
            consumer.registerMessageListener((MessageListenerConcurrently) (msgExtList, ctx) -> {
                for (MessageExt msgExt : msgExtList) {
                    //解析战斗结果消息
                    VictorMsg mqMsg = JSONObject.parseObject(
                            msgExt.getBody(),
                            VictorMsg.class
                    );

                    LOGGER.info("从消息队列收到战斗结果，winId = {}，loserId = {}",
                            mqMsg.getWinId(),
                            mqMsg.getLoserId()
                    );

                    RankService.getInstance().refreshRank(mqMsg.getWinId(),
                            mqMsg.getLoserId());
                }

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
