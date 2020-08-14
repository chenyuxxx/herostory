package org.tinygame.herostory.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.MainThreadProcessor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 异步操作处理器
 */
public final class AsyncOperationProcessor {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(AsyncOperationProcessor.class);
    /**
     * 单例对象
     */
    private static AsyncOperationProcessor _instance = new AsyncOperationProcessor();

    /**
     * 创建一个单线程
     *
     * 不能加 static 关键字，因为加了关键字，在加载时才会实例化
     * */
    private final ExecutorService[] _esArray = new ExecutorService[8];

    /**
     * 私有化类默认构造器
     */
    private AsyncOperationProcessor() {
        for (int i = 0;i < _esArray.length;i++){
            //线程名称
            final String threadName = "AsyncOperationProcessor" + i;
            //创建一个单线程
            _esArray[i] = Executors.newSingleThreadExecutor(r -> {
                Thread newThread = new Thread(r);
                newThread.setName(threadName);
                return newThread;
            });
        }
    }

    /**
     * 获取单例对象
     *
     * @return 异步操作处理器
     */
    public static AsyncOperationProcessor getInstance(){
        return _instance;
    }

    /**
     * 处理异步消息
     *
     * 方法解析
     * 1.在 AsyncOperationProcessor 这个处理异步操作的处理器中，process方法里传入一个 IAsyncOperation
     * 2.IAsyncOperation 作用就是消息的处理与发送
     * 3.在 AsyncOperationProcessor 中本来就有一个单一线程的 ExecutorService
     * 4._es.submit 传入一个 Runnable ，里面首先执行 asyncOp 的 doAsync 方法，进行一次异步的操作
     * 5.当异步操作执行完之后，调用主线程 MainThreadProcessor 的实例对象，里面的 process 方法
     * 6.主线程的 process 方法里面的 Runnable 去执行 asyncOp 的 doFinish 方法，把消息传递回主线程
     * 7.这样操作，就可以让数据读取的步骤在异步线程中执行，执行完获取的消息重新回到主线程，串行化执行
     *
     * @param asyncOp
     */
    public void process(IAsyncOperation asyncOp){
        if (null == asyncOp) {
            return;
        }

        //根据 bindId 获取线程索引
        int bindId = Math.abs(asyncOp.bindId());
        int esIndex = bindId % _esArray.length;

        // submit 中 doAsync 和 doFinish 方法是同步执行的
        _esArray[esIndex].submit(()->{
            try {
                //执行异步操作
                asyncOp.doAsync();

                //返回主线程完成逻辑
                MainThreadProcessor.getInstance().process(asyncOp::doFinish);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(),e);
            }
        });
    }

}
