package org.tinygame.herostory.async;

/**
 * 异步操作接口
 *
 * 可以替换成 MQ ，将IO操作放入MQ执行会进入另一个进程中，减少服务器压力，
 * 虽然会增加 IO 时长，但是配合 redis 可以减少更多的消耗
 */
public interface IAsyncOperation {
    /**
     * 获取绑定 ID
     *
     * 前提；主线程与异步操作数据库线程分开，但是由于异步数据库线程是单线程，所以其实如果访问量多，异步线程也会卡住
     * 为了解决此问题，建立一个线程池 Executors.newFixedThreadPool() ,在线程池中设立一个数字，进行多线程处理
     *
     * 解决问题：在请求中多次点击按钮，可能会造成数据库写入重复数据的问题，在领奖等模块容易发生
     *
     * 设计构思：一种解决方法是直接给方法加锁，但是加锁后会影响性能，而且锁时间也很长
     *
     * 第二种思路（真实场景下）：不再使用线程池，建立一个单线程数组，然后在异步操作接口中新建一个方法，bindId()
     * 在异步操作处理器中 AsyncOperationProcessor ，构造器里将单线程循环添加进数组，处理异步消息的时候，首先获取传入的
     * 异步操作接口 asyncOp 的 bindId() 方法的返回值，这里使用的 asyncOp 在 LoginService 类中重写了 bindId() 方法，
     * 然后将新的绑定 ID 值对单线程数组长度取余，得到需要使用的那个单线程。这样就可以让一个用户在一条固定线程中执行
     * 不会影响到其他的操作，也就不会出现读数据时出现反复读取导致重复写入数据库的问题。
     *
     * @return 绑定 ID
     */
    default int bindId(){
        return 0;
    }
    /**
     * 执行异步操作
     */
    void doAsync();

    /**
     * 执行完成逻辑
     */
    default void doFinish(){
    }
}
