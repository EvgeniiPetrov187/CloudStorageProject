package server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;


public class MessageHandler extends SimpleChannelInboundHandler<String> {

    public static final ConcurrentLinkedQueue<SocketChannel> channels = new ConcurrentLinkedQueue<>();
    int numberOfNewFiles = 0;
    int numberOfNewFolders = 0;
    String currentDirectory = "server/";

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected: " + ctx.channel());
        channels.add((SocketChannel) ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        String command = msg
                .replace("\n", "")
                .replace("\r", "");

        String[] commands = command.split(" ");
        try {
            if (command.startsWith("touch")) {
                createFileNew(commands);
            } else if (command.startsWith("cd")) {
                changeDirectory(commands, ctx);
            } else if (command.startsWith("mkdir")) {
                createDirectory(commands);
            } else if (command.startsWith("ls")) {
                ctx.writeAndFlush(getFileServerList() + "\r\n");
            } else if (command.startsWith("rm")) {
                deleteFile(commands, ctx);
            } else if (command.startsWith("copy")) {
                copyFile(commands, ctx);
            } else if (command.startsWith("cat")) {
                showFile(commands, ctx);
            } else if (command.startsWith("find")) {
                findFile(commands, ctx);
            } else if (command.startsWith("fs")) {
                fileSize(commands, ctx);
            } else if (command.startsWith("ft")) {
                fileTime(commands, ctx);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Message from client: " + msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client disconnected: " + ctx.channel());
    }

    // создание файла
    public void createFileNew(String[] com) throws IOException {
        try {
            Path newFile = Paths.get(currentDirectory + com[1]);
            if (!Files.exists(newFile))
                Files.createFile(newFile);
        } catch (IndexOutOfBoundsException e) {
            Path newFile = Paths.get(currentDirectory + "new file(" + numberOfNewFiles + ")");
            numberOfNewFiles++;
            Files.createFile(newFile);
        }
    }

    // перемещение по каталогу (.. | ~ )
    public void changeDirectory(String[] com, ChannelHandlerContext ctx) throws IOException {
        try {
            if ("~".equals(com[1])) {
                currentDirectory = "server/";
            } else if ("..".equals(com[1]) && !currentDirectory.equals("server/")) {
                StringBuilder sb = new StringBuilder();
                String[] comma = currentDirectory.split("/");
                for (int i = 0; i < comma.length - 1; i++) {
                    sb.append(comma[i] + "/");
                }
                currentDirectory = sb.toString();
            } else if (Files.exists(Paths.get(currentDirectory + com[1])) && !"..".equals(com[1])) {
                currentDirectory = currentDirectory + com[1] + "/";
            }
        } catch (IndexOutOfBoundsException e) {
            ctx.writeAndFlush("Directory's name is empty\r\n");
        }
    }

    // создание папки
    public void createDirectory(String[] com) throws IOException {
        try {
            Path newDirectory = Paths.get(currentDirectory + com[1]);
            if (!Files.exists(newDirectory))
                Files.createDirectory(newDirectory);
        } catch (IndexOutOfBoundsException e) {
            Path newDirectory = Paths.get(currentDirectory + "New folder (" + numberOfNewFolders + ")");
            numberOfNewFolders++;
            Files.createDirectory(newDirectory);
        }
    }

    // список файлов в директории
    public String getFileServerList() {
        return String.join("\r\n", new File(currentDirectory).list());
    }


    // удаление файла или папки
    public void deleteFile(String[] com, ChannelHandlerContext ctx) throws IOException {
        try {
            Path pathToFile = Paths.get(currentDirectory + com[1]);
            if (Files.exists(pathToFile)) {
                Files.walkFileTree(pathToFile, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (IndexOutOfBoundsException e) {
            ctx.writeAndFlush("Filename or directory's name is empty\r\n");
        } catch (DirectoryNotEmptyException e) {
            ctx.writeAndFlush("Directory is not empty\r\n");
        }
    }

    // копирование файла или папки
    public void copyFile(String[] com, ChannelHandlerContext ctx) throws IOException {
        try {
            Path pathToFile = Paths.get(currentDirectory + com[1]);
            Path output = Paths.get(com[2] + "/" + com[1]);
            if (!Files.exists(output))
                Files.copy(pathToFile, output, StandardCopyOption.COPY_ATTRIBUTES);
        } catch (IndexOutOfBoundsException e) {
            ctx.writeAndFlush("Filename or directory's name cannot be empty\r\n");
        }
    }

    // просмотр содержимого файла
    public void showFile(String[] com, ChannelHandlerContext ctx) throws IOException {
        String readyLine;
        try {
            Path pathToFile = Paths.get(currentDirectory + com[1]);
            for (String line : Files.readAllLines(pathToFile)) {
                readyLine = line + "\r\n";
                ctx.writeAndFlush(readyLine);
            }
        } catch (IndexOutOfBoundsException e) {
            ctx.writeAndFlush("Filename is empty\r\n");
        }
    }

    // найти файл
    public void findFile(String[] com, ChannelHandlerContext ctx) throws IOException {
        Path pathToFile = Paths.get("server/");
        Files.walkFileTree(pathToFile, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (com[1].equals(file.getFileName().toString())) {
                    ctx.writeAndFlush(file.getFileName() + " is founded.\r\nPath: " + file.toAbsolutePath());
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    // время файла или папки
    public void fileTime(String[] com, ChannelHandlerContext ctx) throws IOException {
        try {
            Path pathToFile = Paths.get(currentDirectory + com[1]);
            BasicFileAttributes attributes = Files.readAttributes(pathToFile, BasicFileAttributes.class);
            ctx.writeAndFlush(
                    com[1] + " - time of creation " + attributes.creationTime().toString() + "\r\n" +
                    com[1] + " - time of last modification " + attributes.lastModifiedTime().toString() + "\r\n");
        } catch (IndexOutOfBoundsException e) {
            ctx.writeAndFlush("Filename or directory's name cannot be empty\r\n");
        }
    }
    // размер файла или папки
    public void fileSize(String[] com, ChannelHandlerContext ctx) throws IOException {
        AtomicLong bytesOfFile = new AtomicLong(0);
        try {
            Path pathToFile = Paths.get(currentDirectory + com[1]);
            if (Files.isDirectory(pathToFile)) {
                Files.walkFileTree(pathToFile, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        bytesOfFile.addAndGet(attrs.size());
                        return FileVisitResult.CONTINUE;
                    }
                });
                ctx.writeAndFlush(bytesOfFile + "\r\n");
            } else if (Files.exists(pathToFile)) {
                ctx.writeAndFlush(Files.readAllBytes(pathToFile).length + "\r\n");
            }
        } catch (IndexOutOfBoundsException e) {
            ctx.writeAndFlush("Filename or directory's name cannot be empty\r\n");
        }
    }
}
