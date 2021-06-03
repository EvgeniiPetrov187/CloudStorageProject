package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ResourceBundle;

public class InterfaceController implements Initializable {

    private NettyClient nettyClient;
    private String clientDirectory;
    private String serverDirectory;
    private PanelController clientPC;
    private PanelController serverPC;

    @FXML
    public VBox serverInfo;
    @FXML
    public VBox clientInfo;
    @FXML
    public TextField login;
    @FXML
    public PasswordField password;
    @FXML
    public TextField nickname;
    @FXML
    public TextArea info;
    @FXML
    public TextField filename;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clientPC = (PanelController) clientInfo.getProperties().get("ctrl");
        serverPC = (PanelController) serverInfo.getProperties().get("ctrl");
        connect();
    }

    public void connect() {
        nettyClient = new NettyClient((args) -> {
            String[] answer = args.split(" ");
            info.setText(args);
            if (args.startsWith("auth")) {
                serverDirectory = answer[1];
                nettyClient.sendMessage("cd " + answer[1]);
                System.out.println();
                clientPC.setFilePath(clientDirectory);
                serverPC.setFilePath(serverDirectory);
                updatePanel();
            } else if (args.startsWith("new")) {
                updatePanel();
            }
        });
    }

    // создание файла на сервере works
    public void createNewFile(ActionEvent actionEvent) {
        nettyClient.sendMessage("touch " + serverPC.getFilePath() + " " + filename.getText());
        filename.clear();
    }

    // создание директории on server works
    public void createDirectory(ActionEvent actionEvent) {
        nettyClient.sendMessage("mkdir " + serverPC.getFilePath() + " " + filename.getText());
        filename.clear();
    }
//works
    public void upload(ActionEvent actionEvent) {
        if (clientPC.getFileName() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Choose file on client", ButtonType.OK);
            alert.showAndWait();
        }

        Path sourcePath = Paths.get(clientPC.getFilePath(), clientPC.getFileName());
        Path destPath = Paths.get(serverPC.getFilePath()).resolve(sourcePath.getFileName().toString());

        try {
            Files.copy(sourcePath, destPath);
            Files.delete(sourcePath);
            updatePanel();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Upload error", ButtonType.OK);
            alert.showAndWait();
        }
    }
//works
    public void download(ActionEvent actionEvent) {
        if (serverPC.getFileName() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Choose file on server", ButtonType.OK);
            alert.showAndWait();
        }

        Path sourcePath = Paths.get(serverPC.getFilePath(), serverPC.getFileName());
        Path destPath = Paths.get(clientPC.getFilePath()).resolve(sourcePath.getFileName().toString());

        try {
            Files.copy(sourcePath, destPath);
            Files.delete(sourcePath);
            updatePanel();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Download error", ButtonType.OK);
            alert.showAndWait();
        }
    }


    // копирование файла works
    public void copyFile(ActionEvent actionEvent) {
        chooseFileAlert();

        PanelController source = null;
        PanelController destination = null;

        if (clientPC.getFileName() != null) {
            source = clientPC;
            destination = serverPC;
        }
        if (serverPC.getFileName() != null) {
            source = serverPC;
            destination = clientPC;
        }

        Path sourcePath = Paths.get(source.getFilePath(), source.getFileName());
        Path destPath = Paths.get(destination.getFilePath()).resolve(sourcePath.getFileName().toString());

        try {
            Files.copy(sourcePath, destPath);
            updatePanel();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Copy error", ButtonType.OK);
            alert.showAndWait();
        }
    }

    // удаление файла или директории works
    public void deleteFile(ActionEvent actionEvent) {
        chooseFileAlert();

        PanelController source = null;

        if (clientPC.getFileName() != null) {
            source = clientPC;
        }
        if (serverPC.getFileName() != null) {
            source = serverPC;
        }

        Path sourcePath = Paths.get(source.getFilePath(), source.getFileName());

        try {
            Files.delete(sourcePath);
            updatePanel();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Delete error", ButtonType.OK);
            alert.showAndWait();
        }
    }

    // поиск файла
    public void search(ActionEvent actionEvent) {
        methodForSearch(clientPC.getFilePath());
        methodForSearch(serverPC.getFilePath());
    }


    public void registration(ActionEvent actionEvent) {
        if (nickname.getText().length() * login.getText().length() * password.getText().length() != 0) {
            clientDirectory = "client_" + login.getText();
            nettyClient.sendMessage("reg " + login.getText() + " " + password.getText() + " " + nickname.getText());
        }
        try {
            Path newDir = Paths.get(clientDirectory);
            Files.createDirectory(newDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // works
    public void authentication(ActionEvent actionEvent) {
        if (login.getText().length() * password.getText().length() != 0) {
            clientDirectory = "client_" + login.getText();
            nettyClient.sendMessage("auth " + login.getText() + " " + password.getText());
        }
        try {
            Path newDir = Paths.get(clientDirectory);
            if (!Files.exists(newDir))
                Files.createDirectory(newDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void updatePanel() {
        clientPC.updateList(Paths.get(clientPC.getFilePath()));
        serverPC.updateList(Paths.get(serverPC.getFilePath()));
        nettyClient.sendMessage("cd " + serverPC.getFilePath());
        System.out.println(serverPC.getFilePath());
    }

    public void showFile(ActionEvent actionEvent) {
        chooseFileAlert();

        PanelController source = null;

        if (clientPC.getFileName() != null) {
            source = clientPC;
        }
        if (serverPC.getFileName() != null) {
            source = serverPC;
        }

        Path sourcePath = Paths.get(source.getFilePath(), source.getFileName());
        String readyLine;
        info.clear();
        try {
            for (String line : Files.readAllLines(sourcePath)) {
                readyLine = line + "\r\n";
                info.appendText(readyLine);
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Show file error", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void chooseFileAlert() {
        if (serverPC.getFileName() == null && clientPC.getFileName() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Choose file", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void methodForSearch(String pathName){
        try {
            Path pathToFile = Paths.get(pathName);
            Files.walkFileTree(pathToFile, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (filename.getText().equals(file.getFileName().toString())) {
                        info.clear();
                        info.appendText(file.getFileName() + " is founded.\r\nPath: " + file.toAbsolutePath() + "\n\r");
                    }
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (filename.getText().equals(dir.getFileName().toString())) {
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
}
