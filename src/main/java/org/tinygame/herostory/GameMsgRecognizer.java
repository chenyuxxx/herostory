package org.tinygame.herostory;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.msg.GameMsgProtocol;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息识别器
 */
public class GameMsgRecognizer {
    static private final Logger LOGGER = LoggerFactory.getLogger(GameMsgRecognizer.class);
    /**
     * 消息代码和消息体字典
     */
    private static final Map<Integer, GeneratedMessageV3> _msgCodeAndMsgBodyMap = new HashMap<>();

    /**
     * 消息类型和消息编号字典
     */
    private static final Map<Class<?>, Integer> _msgClassAndMsgCodeMap = new HashMap<>();

    /**
     * 私有化类默认构造器
     */
    private GameMsgRecognizer() {
    }

    public static void init() {
        //获取 GameMsgProtocol 下所有的内部类
        Class<?>[] innerClassArray = GameMsgProtocol.class.getDeclaredClasses();

        for (Class innerClass : innerClassArray) {
            if (!GeneratedMessageV3.class.isAssignableFrom(innerClass)) {
                continue;
            }

            String className = innerClass.getSimpleName();
            className.toLowerCase();

            for (GameMsgProtocol.MsgCode msgCode : GameMsgProtocol.MsgCode.values()) {
                String strMsgCode = msgCode.name();
                strMsgCode = strMsgCode.replaceAll("_", "");
                strMsgCode.toLowerCase();

                if (!strMsgCode.startsWith(className)) {
                    continue;
                }

                try {
                    //反射的方式调用 GameMsgProtocol.UserEntryCmd.getDefaultInstance() 函数
                    //返回一个Object类型
                    Object returnObj = innerClass.getDeclaredMethod("getDefaultInstance");

                    LOGGER.info("{} <==> {}",innerClass.getName(),msgCode.getNumber());

                    //将返回的 Object 强转成 GeneratedMessageV3 类型，put进Map中
                    _msgCodeAndMsgBodyMap.put(
                            msgCode.getNumber(),
                            (GeneratedMessageV3) returnObj
                    );

                    _msgClassAndMsgCodeMap.put(
                            innerClass,
                            msgCode.getNumber()
                    );
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    public static Message.Builder getBuilderByMsgCode(int msgCode) {
        if (msgCode < 0) {
            return null;
        }

        GeneratedMessageV3 msg = _msgCodeAndMsgBodyMap.get(msgCode);
        if (null == msg) {
            return null;
        }
        return msg.newBuilderForType();
    }

    public static int getMsgCodeByMsgClass(Class<?> msgClass) {
        if (null == msgClass) {
            return -1;
        }

        Integer msgCode = _msgClassAndMsgCodeMap.get(msgClass);
        if (null != msgCode) {
            return -1;
        }

        return msgCode;
    }

}
