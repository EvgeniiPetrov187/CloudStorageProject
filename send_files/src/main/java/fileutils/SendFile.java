package fileutils;

import java.io.IOException;
import java.io.Serializable;

/**
 * класс для отправки и получения файлов
 */
public class SendFile implements Serializable {
    private String path;
    private String name;
    private byte [] bytes;

    public SendFile(String name, byte [] bytes, String path) throws IOException {
        this.name = name;
        this.bytes = bytes;
        this.path = path;
    }

     public String getName(){
        return name;
     }

     public byte[] getBytes() {
         return bytes;
     }

    public String getPath() {
        return path;
    }
}