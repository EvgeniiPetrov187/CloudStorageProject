package client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * класс который отображает файлы в таблицах класса PanelController
 */
public class MyFile {
    public enum TypeOfFile {
        FILE("File"),
        DIRECTORY("Directory");

        private String name;

        public String getName(){
            return name;
        }

        TypeOfFile(String name){
            this.name = name;
        }
    }
    private String filename;
    private TypeOfFile type;
    private long size;
    private LocalDateTime modifiedTime;

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setType(TypeOfFile type) {
        this.type = type;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setModifiedTime(LocalDateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getFilename() {
        return filename;
    }

    public TypeOfFile getType() {
        return type;
    }

    public long getSize() {
        return size;
    }

    public LocalDateTime getModifiedTime() {
        return modifiedTime;
    }

    public MyFile(Path pathToFile)  {
        try {
            this.filename = pathToFile.getFileName().toString();
            this.size = Files.size(pathToFile);
            this.type = Files.isDirectory(pathToFile) ? TypeOfFile.DIRECTORY : TypeOfFile.FILE;
            if (this.type == TypeOfFile.DIRECTORY){
                this.size = -1L;
            }
            this.modifiedTime = LocalDateTime.ofInstant(Files
                    .getLastModifiedTime(pathToFile)
                    .toInstant(), ZoneOffset.ofHours(0));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
