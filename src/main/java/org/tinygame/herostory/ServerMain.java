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
import org.tinygame.herostory.cmdHandle.CmdHandlerFactory;

/**
 *
 * @test http://cdn0001.afrxvk.cn/hero_story/demo/step010/index.html?serverAddr=192.168.0.137:12345&userId=1
 */
public class ServerMain {
    /**
     * 日志对象
     */
    static private final Logger LOGGER = LoggerFactory.getLogger(ServerMain.class);

    static public void main(String[] args){
        // 设置 log4j 属性文件
        PropertyConfigurator.configure(ServerMain.class.getClassLoader().getResourceAsStream("log4j.properties"));

        CmdHandlerFactory.init();
        GameMsgRecognizer.init();

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
                            new HttpServerCodec(),
                            new HttpObjectAggregator(65535),
                            new WebSocketServerProtocolHandler("/websocket"),
                            new GameMsgDecoder(), // 自定义消息解码器
                            new GameMsgEncoder(), // 自定义消息编码器
                            new GameMsgHandler()
                    );
                }
            });

            b.option(ChannelOption.SO_BACKLOG,128);
            b.childOption(ChannelOption.SO_KEEPALIVE,true);

            ChannelFuture f = b.bind(12345).sync();

            if (f.isSuccess()){
                LOGGER.info("游戏服务器启动成功！");
            }

            f.channel().closeFuture().sync();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
            e.printStackTrace();
        } finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
