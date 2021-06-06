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

    /**
     * метод соединяется с базой данных, если клиент подключен
     * @param ctx - клиент
     * @throws Exception
     */
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

    /**
     * метод чтения комманд от клиента
     * @param ctx - клиент
     * @param msg - команда
     * @throws Exception
     */
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

    /**
     * отключение от базы данных, когда клиент отключился
     * @param ctx клиент
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client disconnected: " + ctx.channel());
        dataBaseService.disconnect();
    }

    /**
     * отпрака запроса в базу данных для входа, создание директории пользователя на сервере, если она ещё не создана
     * и отправка новой директории сервера клиенту в случае успеха
     * @param commands информация от клинета логин и пароль
     * @param ctx клиент
     */
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

    /**
     * регистрация нового пользователя в базе данных, создание директории пользоваетля на сервере
     * @param commands информация от клиента логин, пароль и ник
     * @param ctx клиент
     */
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

    /**
     * отправка запроса в базу данных для смены ника
     * @param commands информация от клиента старый ник и новый ник
     * @param ctx клиент
     */
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
