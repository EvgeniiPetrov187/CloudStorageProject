package client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class NettyClient {
    private SocketChannel channel;
    private static final int PORT = 4000;
    private Callable callable;
    private String message;

    public NettyClient(Callable callable) {
        this.callable = callable;
        Thread t1 = new Thread(() -> {
            EventLoopGroup worker = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(worker)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                                     @Override
                                     protected void initChannel(SocketChannel socketChannel) throws Exception {
                                         channel = socketChannel;
                                         socketChannel.pipeline().addLast(
                                                 new StringEncoder(),
                                                 new StringDecoder(),
                                                 new SimpleChannelInboundHandler<String>() {
                                                     @Override
                                                     protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
                                                         if (callable != null) {
                                                             callable.call(s);
                                                             // получение сообщений от сервера
                                                         }

                                                     }

                                                 }
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

    //отсылка сообщений на сервер
    public void sendMessage(String msg) {
        channel.writeAndFlush(msg);
    }

    /*public String messageFromServer() {
        return message;
    }*/
}
