package org.tinygame.herostory.cmdHandle;

import com.google.protobuf.GeneratedMessageV3;
import org.tinygame.herostory.msg.GameMsgProtocol;

import java.util.HashMap;
import java.util.Map;

/**
 * 指令处理器工厂
 *
 */
public final class CmdHandlerFactory {
    /**
     * 处理器字典
     *
     */
    private static Map<Class<?>,ICmdHandler<? extends GeneratedMessageV3>> _handlerMap = new HashMap<>();
    /**
     * 私有化类默认构造器
     *
     */
    private CmdHandlerFactory() {
    }

    public static void init(){
        _handlerMap.put(GameMsgProtocol.UserEntryCmd.class,new UserEntryCmdHandler());
        _handlerMap.put(GameMsgProtocol.WhoElseIsHereCmd.class,new WhoElseIsHereHandler());
        _handlerMap.put(GameMsgProtocol.UserMoveToCmd.class,new UserMoveToCmdHandler());
    }

    public static ICmdHandler<? extends GeneratedMessageV3> create(Class<?> msgClass){
        if (null == msgClass) {
            return null;
        }

        return _handlerMap.get(msgClass);
    }



}
