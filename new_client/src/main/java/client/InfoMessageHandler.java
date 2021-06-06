package client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class InfoMessageHandler extends SimpleChannelInboundHandler<String> {
    CallbackCommand callback;

    /**
     * Хендлер получения сообщений с сервера
     * @param callback - сообщение
     */
    public InfoMessageHandler(CallbackCommand callback){
        this.callback = callback;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String o) throws Exception {
        if (callback != null)
            callback.call(o);
    }
}

