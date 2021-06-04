package server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;


public class CommandHandler extends SimpleChannelInboundHandler<String> {
    private DataBaseService dataBaseService = new DataBaseService();
    String currentDirectory;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected: " + ctx.channel());
        try {
            dataBaseService.connect();
        } catch (SQLException | ClassNotFoundException e) {
            ctx.writeAndFlush("Information: Database is not connected");
            e.printStackTrace();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        String command = msg
                .replace("\n", "")
                .replace("\r", "");

        String[] commands = command.split(" ");
        if (command.startsWith("cd")) {
            currentDirectory = commands[1] + "/";
        } else if (command.startsWith("reg")) {
            registrationDB(commands, ctx);
        } else if (command.startsWith("auth")) {
            authentication(commands, ctx);
        } else if (command.startsWith("nick")) {
            changeNick(commands, ctx);
        }
        System.out.println("Message from client: " + msg);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client disconnected: " + ctx.channel());
        dataBaseService.disconnect();
    }

    private void authentication(String[] commands, ChannelHandlerContext ctx) {
        try {
            String nickname = dataBaseService.getNicknameByLoginAndPassword(commands[1], commands[2]);
            currentDirectory = "server_" + commands[1];
            try {
                Path newDir = Paths.get(currentDirectory);
                if (!Files.exists(newDir))
                    Files.createDirectory(newDir);
            } catch (IOException e) {
                ctx.writeAndFlush("Info: Folder creation error");
            }
            ctx.writeAndFlush("auth " + currentDirectory + " " + nickname);
        } catch (SQLException e) {
            ctx.writeAndFlush("Info: Authentication failed");
            e.printStackTrace();
        }
    }

    private void registrationDB(String[] commands, ChannelHandlerContext ctx) {
        try {
            dataBaseService.registration(commands[1], commands[2], commands[3]);
            currentDirectory = "server_" + commands[1];
            try {
                Path newDir = Paths.get(currentDirectory);
                if (!Files.exists(newDir))
                    Files.createDirectory(newDir);
            } catch (IOException e) {
                ctx.writeAndFlush("Info: Folder creation error");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            ctx.writeAndFlush("Info: Registration failed");
        }
        ctx.writeAndFlush("Info: Registration successful");
    }

    private void changeNick(String[] commands, ChannelHandlerContext ctx) {
        try {
            dataBaseService.changeNick(commands[1], commands[2]);
            ctx.writeAndFlush("nick " + commands[2]);
        } catch (SQLException e) {
            ctx.writeAndFlush("Info: Change nickname failed");
            e.printStackTrace();
        }
    }
}
