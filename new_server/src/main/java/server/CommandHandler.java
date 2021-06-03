package server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class CommandHandler extends SimpleChannelInboundHandler<String> {

    private DataBaseService dataBaseService = new DataBaseService();

    int numberOfNewFiles = 0;
    int numberOfNewFolders = 0;
    String currentDirectory;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected: " + ctx.channel());
        dataBaseService.connect();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

        String command = msg
                .replace("\n", "")
                .replace("\r", "");

        String[] commands = command.split(" ");
        try {
            if (command.startsWith("cd")) {
                currentDirectory = commands[1] + "/";
            } else if (command.startsWith("reg")) {
                registrationDB(commands, ctx);
            } else if (command.startsWith("auth")) {
                authentication(commands, ctx);
            } else if (command.startsWith("touch")) {
                createFileNew(commands);
                ctx.writeAndFlush("new");
            } else if (command.startsWith("mkdir")) {
                createDirectory(commands);
                ctx.writeAndFlush("new");
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        System.out.println("Message from client: " + msg);
        System.out.println(currentDirectory);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client disconnected: " + ctx.channel());
        dataBaseService.disconnect();
    }

    // создание файла
    public void createFileNew(String[] com) throws IOException {
        currentDirectory = com[1]+"/";
        try {
            Path newFile = Paths.get(currentDirectory + com[2]);
            if (!Files.exists(newFile))
                Files.createFile(newFile);
        } catch (IndexOutOfBoundsException e) {
            Path newFile = Paths.get(currentDirectory + "new file(" + numberOfNewFiles + ")");
            numberOfNewFiles++;
            Files.createFile(newFile);
        }
    }

    // создание папки
    public void createDirectory(String[] com) throws IOException {
        currentDirectory = com[1]+"/";
        try {
            Path newDirectory = Paths.get(currentDirectory + com[2]);
            if (!Files.exists(newDirectory))
                Files.createDirectory(newDirectory);
        } catch (IndexOutOfBoundsException e) {
            Path newDirectory = Paths.get(currentDirectory + "New folder (" + numberOfNewFolders + ")");
            numberOfNewFolders++;
            Files.createDirectory(newDirectory);
        }
    }


    private void authentication(String[] commands, ChannelHandlerContext ctx) {
        dataBaseService.getNicknameByLoginAndPassword(commands[1], commands[2]);
        System.out.println("Auth client " + ctx.channel().toString());
        currentDirectory = "server_" + commands[1];
        try {
            Path newDir = Paths.get(currentDirectory);
            if (!Files.exists(newDir))
                Files.createDirectory(newDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ctx.writeAndFlush("auth " + currentDirectory);
    }

    private void registrationDB(String[] commands, ChannelHandlerContext ctx) {
        dataBaseService.registration(commands[1], commands[2], commands[3]);
        System.out.println("Reg client " + ctx.channel().toString());
        currentDirectory = "server_" + commands[1];
        try {
            Path newDir = Paths.get(currentDirectory);
            if (!Files.exists(newDir))
                Files.createDirectory(newDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ctx.writeAndFlush("reg_ok ");
    }

}
