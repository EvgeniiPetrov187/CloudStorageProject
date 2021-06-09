package server;

import fileutils.SendFile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;


public class CommandHandler extends SimpleChannelInboundHandler<String> {
    private DataBaseService dataBaseService = new DataBaseService();
    private String currentDirectory;
    private String abs;
    private int numberOfNewFiles = 0;
    private int numberOfNewDirectories = 0;
    private ServerUtils serverUtils;

    /**
     * метод соединяется с базой данных, если клиент подключен
     *
     * @param ctx - клиент
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected: " + ctx.channel());
        serverUtils = new ServerUtils();
        try {
            dataBaseService.connect();
        } catch (SQLException | ClassNotFoundException e) {
            ctx.writeAndFlush("Information: Database is not connected");
            e.printStackTrace();
        }
    }

    /**
     * метод чтения команд от клиента
     *
     * @param ctx - клиент
     * @param msg - команда
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        String command = msg
                .replace("\n", "")
                .replace("\r", "");

        String[] commands = command.split("--s-");
        try {
            if (command.startsWith("cd")) {
                currentDirectory = commands[1];
            } else if (command.startsWith("reg")) {
                registrationDB(commands, ctx);
            } else if (command.startsWith("auth")) {
                authentication(commands, ctx);
            } else if (command.startsWith("nick")) {
                changeNick(commands, ctx);
            } else if (command.startsWith("dis")) {
                channelInactive(ctx);
            } else if (command.startsWith("touch")) {
                createNewFile(commands, ctx);
            } else if (command.startsWith("mkdir")) {
                createNewDirectory(commands, ctx);
            } else if (command.startsWith("rm")) {
                deleteFile(commands, ctx);
            } else if (command.startsWith("download")) {
                download(commands, ctx);
            } else if (command.startsWith("sw")) {
                showFile(commands[1], ctx);
            } else if (command.startsWith("copy")) {
                copyFile(commands[1], ctx);
            } else if (command.startsWith("search")) {
                searchFile(commands[1], ctx);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * отключение от базы данных, когда клиент не активен
     *
     * @param ctx клиент
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush("dis");
        System.out.println("Client disconnected: " + ctx.channel());
        dataBaseService.disconnect();
        ctx.close();
    }

    /**
     * поиск файлов на сервере
     * @param com - команда от клиента - имя файла
     * @param ctx - клиент
     * @throws IOException
     */
    private void searchFile(String com, ChannelHandlerContext ctx) throws IOException {
        serverUtils.methodForSearch(currentDirectory, com, ctx);
    }


    /**
     * копирование файлов внутри папки на сервере
     * @param com - команда от клиента - имя файла
     * @param ctx - клиент
     * @throws IOException
     */
    private void copyFile(String com, ChannelHandlerContext ctx) throws IOException {
        Path sourcePath = Paths.get(currentDirectory, com);
        Path destPath = Paths.get(currentDirectory, com + "(copy)");
        serverUtils.methodForCopy(sourcePath, destPath);
        ctx.writeAndFlush("new");
    }



    /**
     * метод отправляет список файлов директории или содержание файла клиенту
     * после принятия команды от клиента
     *
     * @param com - команда от клиента
     * @param ctx - клиент
     * @throws IOException
     */
    public void showFile(String com, ChannelHandlerContext ctx) throws IOException {
        Path sourcePath = Paths.get(currentDirectory, com);
        String readyLine = "";
        if (Files.isDirectory(sourcePath)) {
            readyLine = "Files in Folder " + com + "\r\n" + String.join("\r\n", new File(String.valueOf(sourcePath)).list());
            ctx.writeAndFlush("Info:\r\n" + readyLine);
        } else {
            StringBuilder sb = new StringBuilder();
            for (String line : Files.readAllLines(sourcePath)) {
                readyLine = line + "\r\n";
                sb.append(readyLine);
            }
            ctx.writeAndFlush("Info:\r\n" + sb.toString());
        }

    }

    /**
     * отправка файла на клиент после принятия команды от клиента
     *
     * @param com - коианда от клиента
     * @param ctx - клиент
     * @throws IOException
     */
    public void download(String com[], ChannelHandlerContext ctx) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(com[1], com[2]));
        SendFile sendFile = new SendFile(com[2], bytes, com[1]);
        ctx.writeAndFlush(sendFile);
    }


    /**
     * удаление файла или директории на сервере после принятие команды от клиента
     *
     * @param com - команда от клиента
     * @param ctx - клиент
     * @throws IOException
     */
    private void deleteFile(String com[], ChannelHandlerContext ctx) throws IOException {
        serverUtils.methodForDelete(Paths.get(currentDirectory, com[1]));
        ctx.writeAndFlush("new");
    }

    /**
     * создание директории на сервере после принятие команды от клиента
     *
     * @param com - команда от клиента
     * @param ctx - клиент
     * @throws IOException
     */
    private void createNewDirectory(String[] com, ChannelHandlerContext ctx) throws IOException {
        try {
            Path newFile = Paths.get(currentDirectory, com[1]);
            if (!Files.exists(newFile)) {
                Files.createDirectory(newFile);
                ctx.writeAndFlush("new");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Path newFile = Paths.get(currentDirectory, "New Folder(" + numberOfNewDirectories + ")");
            numberOfNewDirectories++;
            Files.createDirectory(newFile);
            ctx.writeAndFlush("new");
        }
    }

    /**
     * создание файла на сервере после принятие команды от клиента
     *
     * @param com - команда от клиента
     * @param ctx - клиент
     * @throws IOException
     */
    public void createNewFile(String[] com, ChannelHandlerContext ctx) throws IOException {
        try {
            Path newFile = Paths.get(currentDirectory, com[1]);
            if (!Files.exists(newFile)) {
                Files.createFile(newFile);
                ctx.writeAndFlush("new");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Path newFile = Paths.get(currentDirectory, "new file(" + numberOfNewFiles + ")");
            numberOfNewFiles++;
            Files.createFile(newFile);
            ctx.writeAndFlush("new");
        }
    }


    /**
     * отпрака запроса в базу данных для входа, создание директории пользователя на сервере, если она ещё не создана
     * и отправка новой директории сервера клиенту в случае успеха
     *
     * @param commands информация от клинета логин и пароль
     * @param ctx      клиент
     */
    private void authentication(String[] commands, ChannelHandlerContext ctx) {
        try {
            String nickname = dataBaseService.getNicknameByLoginAndPassword(commands[1], commands[2]);
            currentDirectory = commands[1] + "_server";
            try {
                Path newDir = Paths.get(currentDirectory);
                abs = newDir.toAbsolutePath().toString();
                if (!Files.exists(newDir))
                    Files.createDirectory(newDir);
            } catch (IOException e) {
                ctx.writeAndFlush("Info: Folder creation error");
            }
            ctx.writeAndFlush("auth--s-" + abs + "--s-" + nickname);
        } catch (SQLException e) {
            ctx.writeAndFlush("Info: Authentication failed");
            e.printStackTrace();
        }
    }

    /**
     * регистрация нового пользователя в базе данных, создание директории пользоваетля на сервере
     *
     * @param commands информация от клиента логин, пароль и ник
     * @param ctx      клиент
     */
    private void registrationDB(String[] commands, ChannelHandlerContext ctx) {
        try {
            dataBaseService.registration(commands[1], commands[2], commands[3]);
            currentDirectory = commands[1] + "_server";
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
     *
     * @param commands информация от клиента старый ник и новый ник
     * @param ctx      клиент
     */
    private void changeNick(String[] commands, ChannelHandlerContext ctx) {
        try {
            dataBaseService.changeNick(commands[1], commands[2]);
            ctx.writeAndFlush("nick--s-" + commands[2]);
        } catch (SQLException e) {
            ctx.writeAndFlush("Info: Change nickname failed");
            e.printStackTrace();
        }
    }
}
