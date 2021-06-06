package client;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class Utils {
    /**
     * вспомогательный метод вызова предупреждения если не выбрана ни одна из директорий
     * @param serverPC - директория сервера
     * @param clientPC - директория клиента
     */
    public void chooseFileAlert(PanelController serverPC, PanelController clientPC) {
        if (serverPC.getFileName() == null && clientPC.getFileName() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Choose file", ButtonType.OK);
            alert.showAndWait();
        }
    }

    /**
     * основной метода для поиска файлов в директориях
     * @param pathName директория поиска
     * @param filename имя файла
     * @param info область для вывода информации
     */
    public void methodForSearch(String pathName, String filename, TextArea info) {
        try {
            Path pathToFile = Paths.get(pathName);
            Files.walkFileTree(pathToFile, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (filename.equals(file.getFileName().toString())) {
                        info.clear();
                        info.appendText(file.getFileName() + " is founded.\r\nPath: " + file.toAbsolutePath() + "\n\r");
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (filename.equals(dir.getFileName().toString())) {
                        info.clear();
                        info.appendText(dir.getFileName() + " is founded.\r\nPath: " + dir.toAbsolutePath() + "\n\r");
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * основной метод для копирования файлов или директорий
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
     * @param sourcePath путь файла или директории
     */
    public void methodForDelete(Path sourcePath) {
        try {
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
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Delete error", ButtonType.OK);
            alert.showAndWait();
        }
    }
}
