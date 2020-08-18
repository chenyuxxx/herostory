package org.tinygame.herostory.cmdhandle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.Broadcaster;
import org.tinygame.herostory.model.User;
import org.tinygame.herostory.model.UserManger;
import org.tinygame.herostory.mq.MQProducer;
import org.tinygame.herostory.mq.VictorMsg;
import org.tinygame.herostory.msg.GameMsgProtocol;

/**
 * 用户攻击指令处理器
 */
public class UserAttkCmdHandler implements ICmdHandler<GameMsgProtocol.UserAttkCmd> {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(UserAttkCmdHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.UserAttkCmd cmd) {
        if (null == ctx || null == cmd) {
            return;
        }

        //获取攻击者id
        Integer attkUserId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();

        if (null == attkUserId) {
            return;
        }

        //获取被攻击者id
        int targetUserId = cmd.getTargetUserId();
        //获取被攻击者用户
        User targetUser = UserManger.getUserById(targetUserId);
        if (null == targetUser) {
            broadcastAttkResult(attkUserId, -1);
            return;
        }

        LOGGER.info("当前线程 = {}", Thread.currentThread().getName());

        final int subtractHP = 10;
        targetUser.currHP = targetUser.currHP - subtractHP;
        // 广播攻击结果
        broadcastAttkResult(attkUserId, targetUserId);
        //广播减血消息
        broadcastSubtractHP(targetUserId,subtractHP);

        if (targetUser.currHP <= 0){
            //广播死亡消息
            broadcastDie(targetUserId);

            if (!targetUser.died) {
                targetUser.died = true;

                VictorMsg mqMsg = new VictorMsg();
                mqMsg.setWinId(attkUserId);
                mqMsg.setLoserId(targetUserId);
                MQProducer.sendMsg("Victor",mqMsg);
            }
        }
    }

    /**
     * 广播攻击结果
     *
     * @param attkUserId
     * @param targetUserId
     */
    static private void broadcastAttkResult(int attkUserId, int targetUserId) {
        if (attkUserId <= 0) {
            return;
        }

        GameMsgProtocol.UserAttkResult.Builder resultBuilder = GameMsgProtocol.UserAttkResult.newBuilder();
        resultBuilder.setAttkUserId(attkUserId);
        resultBuilder.setTargetUserId(targetUserId);

        GameMsgProtocol.UserAttkResult newResult = resultBuilder.build();
        Broadcaster.broadcast(newResult);
    }

    /**
     * 广播减血消息
     *
     * @param targetUserId
     * @param subtractHP
     */
    public static void broadcastSubtractHP(int targetUserId, int subtractHP) {
        if (targetUserId <= 0 || subtractHP <= 0) {
            return;
        }

        GameMsgProtocol.UserSubtractHpResult.Builder resultBuilder = GameMsgProtocol.UserSubtractHpResult.newBuilder();
        resultBuilder.setSubtractHp(subtractHP);
        resultBuilder.setTargetUserId(targetUserId);

        GameMsgProtocol.UserSubtractHpResult newResult = resultBuilder.build();
        Broadcaster.broadcast(newResult);
    }

    /**
     * 广播死亡消息
     *
     * @param targetUserId
     */
    public static void broadcastDie(int targetUserId){
        if (targetUserId <= 0) {
            return;
        }

        GameMsgProtocol.UserDieResult.Builder resultBuilder = GameMsgProtocol.UserDieResult.newBuilder();
        resultBuilder.setTargetUserId(targetUserId);

        GameMsgProtocol.UserDieResult newResult = resultBuilder.build();
        Broadcaster.broadcast(newResult);
    }
}
