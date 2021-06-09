package server;

import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class ServerUtils {

    /**
     * основной метода для поиска файлов в директориях
     *
     * @param pathName директория поиска
     * @param filename имя файла
     */
    public void methodForSearch(String pathName, String filename, ChannelHandlerContext ctx) throws IOException {
        Path pathToFile = Paths.get(pathName);
        Files.walkFileTree(pathToFile, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (filename.equals(file.getFileName().toString())) {
                    ctx.writeAndFlush("Info:\r\n" + file.getFileName() + " is founded.\r\nPath: " + file.toAbsolutePath() + "\n\r");
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                if (filename.equals(dir.getFileName().toString())) {
                    ctx.writeAndFlush("Info:\r\n" + dir.getFileName() + " is founded.\r\nPath: " + dir.toAbsolutePath() + "\n\r");
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }


    /**
     * основной метод для копирования файлов или директорий
     *
     * @param src исходная директория и имя файла
     * @param dst конечная директория
     * @throws IOException
     */
    public void methodForCopy(Path src, Path dst) throws IOException {
        Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetPath = dst.resolve(src.relativize(dir));
                if (!Files.exists(targetPath)) {
                    Files.createDirectory(targetPath);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, dst.resolve(src.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * основной метод удаления файла или директории
     *
     * @param sourcePath путь файла или директории
     */
    public void methodForDelete(Path sourcePath) throws IOException {
        Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
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
}
