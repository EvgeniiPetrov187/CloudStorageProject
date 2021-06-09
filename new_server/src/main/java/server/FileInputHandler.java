package server;

import fileutils.SendFile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * принятие файлов от клиента
 */
public class FileInputHandler extends SimpleChannelInboundHandler<SendFile> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SendFile sendFile) throws Exception {
        if (!Files.exists(Paths.get(sendFile.getPath(), sendFile.getName()))) {
            Files.createFile(Paths.get(sendFile.getPath(), sendFile.getName()));
            Files.write(Paths.get(sendFile.getPath(), sendFile.getName()), sendFile.getBytes());
            channelHandlerContext.writeAndFlush("new");
        }
    }
}
