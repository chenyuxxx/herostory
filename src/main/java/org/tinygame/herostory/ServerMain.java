package org.tinygame.herostory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.cmdhandle.CmdHandlerFactory;
import org.tinygame.herostory.mq.MQProducer;
import org.tinygame.herostory.util.RedisUtil;

/**
 *
 * @test
 */
public class ServerMain {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(ServerMain.class);

    /**
     * 服务器端口号
     */
    static private final int SERVER_PORT = 12345;

    static public void main(String[] args){
        // 设置 log4j 属性文件
        PropertyConfigurator.configure(ServerMain.class.getClassLoader().getResourceAsStream("log4j.properties"));

        //初始化命令处理器工厂
        CmdHandlerFactory.init();
        //初始化消息识别器
        GameMsgRecognizer.init();
        //初始化Mysql会话
        MySqlSessionFactory.init();
        //初始化Redis连接
        RedisUtil.init();
        //初始化MQProducer
        MQProducer.init();

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            //操作Netty的一个界面
            ServerBootstrap b = new ServerBootstrap();
            //将group放进界面中
            b.group(bossGroup,workGroup);
            //非阻塞式管道
            b.channel(NioServerSocketChannel.class);
            //服务端获取到客户端的Channel，然后建立一个SocketChannel
            b.childHandler(new ChannelInitializer<SocketChannel>() {
                //接到客户端的SocketChannel之后建立一个管道
                @Override
                protected void initChannel(SocketChannel sc) throws Exception {
                    //编码解码之后走到自己的 GameMsgHandler方法里
                    sc.pipeline().addLast(
                            //HTTP服务器编解码器
                            new HttpServerCodec(),
                            //内容长度限制
                            new HttpObjectAggregator(65535),
                            //websocket协议处理器，在这里处理握手、ping、pong等消息
                            new WebSocketServerProtocolHandler("/websocket"),
                            new GameMsgDecoder(), // 自定义消息解码器
                            new GameMsgEncoder(), // 自定义消息编码器
                            new GameMsgHandler()  //自定义的消息处理器
                    );
                }
            });

            /**
             *
             * 异同点
             * b.option(ChannelOption.SO_BACKLOG,128);
             * b.childOption(ChannelOption.SO_KEEPALIVE,true);
             *
             */
            b.option(ChannelOption.SO_BACKLOG,128);
            b.childOption(ChannelOption.SO_KEEPALIVE,true);

            ChannelFuture f = b.bind(SERVER_PORT).sync();

            if (f.isSuccess()){
                LOGGER.info("游戏服务器启动成功！");
            }

            // 等待服务器信道关闭,
            // 也就是不要立即退出应用程序, 让应用程序可以一直提供服务
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            // 异常处理
            LOGGER.error(e.getMessage(),e);
            e.printStackTrace();
        } finally {
            // 关闭服务器
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
