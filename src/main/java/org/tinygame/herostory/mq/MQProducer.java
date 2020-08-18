package org.tinygame.herostory.mq;

import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.GameMsgDecoder;

/**
 * 消息队列生产者
 */
public class MQProducer {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(MQProducer.class);

    /**
     * 生产者
     */
    private static DefaultMQProducer _producer = null;

    /**
     * 私有化类默认构造器
     */
    private MQProducer() {
    }

    /**
     * 初始化
     */
    public static void init() {
        try {
            DefaultMQProducer producer = new DefaultMQProducer("herostory");
            producer.setNamesrvAddr("127.0.0.1:9876");
            producer.start();
            //重连3次
            producer.setRetryTimesWhenSendAsyncFailed(3);

            _producer = producer;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 发送消息
     *
     * @param topic 主题
     * @param msg 消息对象
     */
    public static void sendMsg(String topic, Object msg) {
        if (null == topic ||
                null == msg) {
            return;
        }

        if (null == _producer) {
            throw new RuntimeException("_producer 初始化失败");
        }

        Message mqMsg = new Message();
        mqMsg.setTopic(topic);
        mqMsg.setBody(JSONObject.toJSONBytes(msg));

        try {
            _producer.send(mqMsg);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
        }
    }
}
