package client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class NettyClient {
    private SocketChannel channel;
    private static final int PORT = 36000;


    /**
     * клиент на Netty
     * @param callback - сообщение от сервера
     */
    public NettyClient(CallbackCommand callback) {
        Thread t1 = new Thread(() -> {
            EventLoopGroup worker = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(worker)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                                     @Override
                                     protected void initChannel(SocketChannel socketChannel) {
                                         channel = socketChannel;
                                         socketChannel.pipeline().addLast(
                                                 new ObjectEncoder(),
                                                 new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)),
                                                 new InfoMessageHandler(callback)
                                         );
                                     }
                                 }
                        );
                ChannelFuture future = bootstrap.connect("localhost", PORT).sync();
                System.out.println("Client started");
                future.channel().closeFuture().sync();
                System.out.println("Client closed");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                worker.shutdownGracefully();
            }
        });
        t1.setDaemon(true);
        t1.start();
    }
    /**
     * метод отправки комманд или объектов на сервер
     * @param msg - команда
     */
    public void sendMessage(Object msg) {
        channel.writeAndFlush(msg);
    }
}


