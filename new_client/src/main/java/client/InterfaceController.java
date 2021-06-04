package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.ResourceBundle;

public class InterfaceController implements Initializable {

    private NettyClient nettyClient;
    private String clientDirectory;
    private String serverDirectory;
    private PanelController clientPC;
    private PanelController serverPC;
    private String nickname;
    private Stage stage;
    private Utils utils;
    private int numberOfNewFiles = 1;
    private int numberOfNewDirectories = 1;
    private int operationId;

    @FXML
    public Button ok;
    @FXML
    public Button back;
    @FXML
    public VBox serverInfo;
    @FXML
    public VBox clientInfo;
    @FXML
    public TextField login;
    @FXML
    public PasswordField password;
    @FXML
    public TextField nicknameField;
    @FXML
    public TextArea info;
    @FXML
    public TextField filename;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clientPC = (PanelController) clientInfo.getProperties().get("ctrl");
        serverPC = (PanelController) serverInfo.getProperties().get("ctrl");
        utils = new Utils();
        connect();
    }

    public void connect() {
        nettyClient = new NettyClient((args) -> {
            String[] answer = args.split(" ");
            if (args.startsWith("auth")) {
                serverDirectory = answer[1];
                nickname = answer[2];
                setTitle(nickname);
                nettyClient.sendMessage("cd " + serverDirectory);
                System.out.println();
                clientPC.setFilePath(clientDirectory);
                serverPC.setFilePath(serverDirectory);
                updatePanel();
                cancelAll();
                info.setText("Authentication successful");
            } else if (args.startsWith("nick")) {
                nickname = answer[1];
                setTitle(nickname);
                info.clear();
            } else if (args.startsWith("Info")) {
                info.setText(args);
            }
        });
    }

    // создание файла на сервере works
    public void createNewFile(ActionEvent actionEvent) {
        String target = null;

        if (clientPC.tableInfo.isFocused()) {
            target = clientPC.getFilePath();
        }
        if (serverPC.tableInfo.isFocused()) {
            target = serverPC.getFilePath();
        }
        if (target == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Choose Folder", ButtonType.OK);
            alert.showAndWait();
        }
        try {
            Path newFile = Paths.get(target, filename.getText());
            if (!Files.exists(newFile))
                Files.createFile(newFile);
            if (filename.getText().equals("")) {
                newFile = Paths.get(target, "new file(" + numberOfNewFiles + ")");
                numberOfNewFiles++;
                Files.createFile(newFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        updatePanel();
    }


    // создание директории on server works
    public void createDirectory(ActionEvent actionEvent) {
        String target = null;

        if (clientPC.tableInfo.isFocused()) {
            target = clientPC.getFilePath();
        }
        if (serverPC.tableInfo.isFocused()) {
            target = serverPC.getFilePath();
        }
        if (target == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Choose Folder", ButtonType.OK);
            alert.showAndWait();
        }
        try {
            Path newDir = Paths.get(target, filename.getText());
            if (!Files.exists(newDir))
                Files.createDirectory(newDir);
            if (filename.getText().equals("")) {
                newDir = Paths.get(target, "New Folder(" + numberOfNewDirectories + ")");
                numberOfNewDirectories++;
                Files.createDirectory(newDir);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        updatePanel();
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
            utils.methodForCopy(sourcePath, destPath);
            utils.methodForDelete(sourcePath);
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
            utils.methodForCopy(sourcePath, destPath);
            utils.methodForDelete(sourcePath);
            updatePanel();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Download error", ButtonType.OK);
            alert.showAndWait();
        }
    }

    // копирование файла works
    public void copyFile(ActionEvent actionEvent) {
        utils.chooseFileAlert(serverPC, clientPC);

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
            utils.methodForCopy(sourcePath, destPath);
            updatePanel();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Copy error", ButtonType.OK);
            alert.showAndWait();
        }
    }

    // удаление файла или директории works
    public void deleteFile(ActionEvent actionEvent) {
        utils.chooseFileAlert(serverPC, clientPC);

        PanelController source = null;

        if (clientPC.getFileName() != null) {
            source = clientPC;
        }
        if (serverPC.getFileName() != null) {
            source = serverPC;
        }
        Path sourcePath = Paths.get(source.getFilePath(), source.getFileName());
        utils.methodForDelete(sourcePath);
        updatePanel();
    }

    // поиск файла
    public void search(ActionEvent actionEvent) {
        utils.methodForSearch(clientPC.getFilePath(), filename.getText(), info);
        utils.methodForSearch(serverPC.getFilePath(), filename.getText(), info);
    }

    public void registration() {
        if (nicknameField.getText().length() * login.getText().length() * password.getText().length() != 0) {
            clientDirectory = "client_" + login.getText();
            nettyClient.sendMessage("reg " + login.getText() + " " + password.getText() + " " + nicknameField.getText());
        }
        try {
            Path newDir = Paths.get(clientDirectory);
            Files.createDirectory(newDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // works
    public void authentication() {
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

    public void changeNickName() {
        if (nickname != null) {
            nettyClient.sendMessage("nick " + nickname + " " + nicknameField.getText());
        }
    }

    public void updatePanel() {
        clientPC.updateList(Paths.get(clientPC.getFilePath()));
        serverPC.updateList(Paths.get(serverPC.getFilePath()));
        nettyClient.sendMessage("cd " + serverPC.getFilePath());
        System.out.println(serverPC.getFilePath());
    }

    public void showFile(ActionEvent actionEvent) {
        utils.chooseFileAlert(serverPC, clientPC);

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
        if (Files.isDirectory(sourcePath)) {
            info.setText("Files in Folder " + source.getFileName() + "\r\n" + String.join("\r\n", new File(String.valueOf(sourcePath)).list()));
        } else {
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
    }

    private void setTitle(String nick) {
        Platform.runLater(() -> {
            stage = (Stage) nicknameField.getScene().getWindow();
            stage.setTitle("Cloud Storage " + nick);
        });
    }

    public void executeOperation(ActionEvent actionEvent) {
        switch (operationId){
            case 1:
                registration();
                break;
            case 2:
                authentication();
                break;
            case 3:
                changeNickName();
                break;
        }
    }

    public void operAuth(ActionEvent actionEvent) {
        operationId = 2;
        login.setVisible(true);
        password.setVisible(true);
        nicknameField.setVisible(false);
        ok.setVisible(true);
        back.setVisible(true);
        login.setManaged(true);
        password.setManaged(true);
        nicknameField.setManaged(false);
        ok.setManaged(true);
        back.setManaged(true);
    }

    public void operReg(ActionEvent actionEvent) {
        operationId = 1;
        login.setVisible(true);
        password.setVisible(true);
        nicknameField.setVisible(true);
        ok.setVisible(true);
        back.setVisible(true);
        login.setManaged(true);
        password.setManaged(true);
        nicknameField.setManaged(true);
        ok.setManaged(true);
        back.setManaged(true);
    }

    public void operNick(ActionEvent actionEvent) {
        operationId = 3;
        nicknameField.setVisible(true);
        login.setVisible(false);
        password.setVisible(false);
        ok.setVisible(true);
        back.setVisible(true);
        login.setManaged(false);
        password.setManaged(false);
        nicknameField.setManaged(true);
        ok.setManaged(true);
        back.setManaged(true);
    }

    public void back(ActionEvent actionEvent){
        cancelAll();
    }

    public void cancelAll(){
        nicknameField.setVisible(false);
        login.setVisible(false);
        password.setVisible(false);
        ok.setVisible(false);
        back.setVisible(false);
        login.setManaged(false);
        password.setManaged(false);
        nicknameField.setManaged(false);
        ok.setManaged(false);
        back.setManaged(false);
    }
}
