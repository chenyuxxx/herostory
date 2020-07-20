package org.tinygame.herostory.cmdhandle;

import com.google.protobuf.GeneratedMessageV3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.util.PackageUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 指令处理器工厂
 */
public final class CmdHandlerFactory {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(CmdHandlerFactory.class);
    /**
     * 处理器字典
     */
    private static final Map<Class<?>, ICmdHandler<? extends GeneratedMessageV3>> _handlerMap = new HashMap<>();

    /**
     * 私有化类默认构造器
     */
    private CmdHandlerFactory() {
    }

    public static void init() {
        LOGGER.info("==== 完成命令与处理器的关联 ====");

        // 获取包名称
        final String packageName = CmdHandlerFactory.class.getPackage().getName();

        //获取实现了ICmdHandler的子类的列表
        Set<Class<?>> clazzSet =  PackageUtil.listSubClazz(
                packageName,
                true,
                ICmdHandler.class
        );

        for (Class<?> clazz : clazzSet) {
            if ((clazz.getModifiers() & Modifier.ABSTRACT) != 0) {
                continue;
            }

            //获取方法数组
            Method[] methods = clazz.getDeclaredMethods();

            Class<?> msgType = null;

            for (Method currMethod : methods) {
                if (!currMethod.getName().equals("handle")) {
                    continue;
                }

                //获取函数参数类型
                Class<?>[] paramTypeArray = currMethod.getParameterTypes();

                if (paramTypeArray.length < 2 ||
                        !GeneratedMessageV3.class.isAssignableFrom(paramTypeArray[1])) {
                    continue;
                }

                msgType = paramTypeArray[1];
                break;
            }

            if (null == msgType) {
                continue;
            }

            try {
                ICmdHandler<?> newHandler = (ICmdHandler<?>)clazz.newInstance();

                LOGGER.info("{} <===> {}",msgType.getName(),clazz.getName());

                _handlerMap.put(msgType,newHandler);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(),e);
            }
        }
    }

    public static ICmdHandler<? extends GeneratedMessageV3> create(Class<?> msgClass) {
        if (null == msgClass) {
            return null;
        }

        return _handlerMap.get(msgClass);
    }


}
