package client;


import fileutils.SendFile;
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
    private SendFile sendFile;

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

    public InterfaceController() throws IOException {
    }

    /**
     * Инициализация окна, таблиц из файла Panel и дополнительных методов из класса Util
     *
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clientPC = (PanelController) clientInfo.getProperties().get("ctrl");
        serverPC = (PanelController) serverInfo.getProperties().get("ctrl");
        utils = new Utils();
        connect();
    }

    /**
     * метод запуска клиента и настройки на обработку сообщений с сервера,
     * может быть вызван на кнопку Restart Connection
     */
    public void connect() {
        Platform.runLater(() -> {
            nettyClient = new NettyClient((args) -> {
                if (args instanceof String) {
                    String msg = (String) args;
                    String[] answer = msg.split("--s-");
                    if (msg.startsWith("auth")) {
                        serverDirectory = answer[1];
                        nickname = answer[2];
                        setTitle(nickname);
                        clientPC.setFilePath(clientDirectory);
                        serverPC.setFilePath(serverDirectory);
                        updatePanel();
                        cancelAll();
                        info.setText("Authentication successful");
                    } else if (msg.startsWith("nick")) {
                        nickname = answer[1];
                        setTitle(nickname);
                        info.clear();
                    } else if (msg.startsWith("Info")) {
                        info.setText(msg);
                    } else if (msg.startsWith("new")) {
                        updatePanel();
                    }
                } else if (args instanceof SendFile) {
                    try {
                        args = new SendFile(((SendFile) args).getName(), ((SendFile) args).getBytes(), ((SendFile) args).getPath());
                        if (!Files.exists(Paths.get(clientPC.getFilePath(), ((SendFile) args).getName()))) {
                            Files.createFile(Paths.get(clientPC.getFilePath(), ((SendFile) args).getName()));
                            Files.write(Paths.get(clientPC.getFilePath(), ((SendFile) args).getName()), ((SendFile) args).getBytes());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    updatePanel();
                }
            });
        });
    }

    /**
     * метод создания файла на сервере или клиенте
     *
     * @param actionEvent - нажатие кнопки Create File
     */
    public void createNewFile(ActionEvent actionEvent) {
        String target = null;

        if (serverPC.tableInfo.isFocused()) {
            target = serverPC.getFilePath();
            nettyClient.sendMessage("touch--s-" + filename.getText());
        }

        if (clientPC.tableInfo.isFocused()) {
            target = clientPC.getFilePath();
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
        if (target == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Choose Folder", ButtonType.OK);
            alert.showAndWait();
        }
    }


    /**
     * метод создания директории на сервере или клиенте
     *
     * @param actionEvent - нажатие кнопки Create Directory
     */
    public void createDirectory(ActionEvent actionEvent) {
        String target = null;

        if (serverPC.tableInfo.isFocused()) {
            target = serverPC.getFilePath();
            nettyClient.sendMessage("mkdir--s-" + filename.getText());
        }
        if (clientPC.tableInfo.isFocused()) {
            target = clientPC.getFilePath();
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
        if (target == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Choose Folder", ButtonType.OK);
            alert.showAndWait();
        }

    }

    /**
     * метод отправляет файл на сервер
     *
     * @param actionEvent - нажатие кнопки Upload File
     */
    public void upload(ActionEvent actionEvent) {
        if (!Files.isDirectory(Paths.get(clientPC.getFilePath(), clientPC.getFileName()).toAbsolutePath())) {
            try {
                byte[] byteBuf = Files.readAllBytes(Paths.get(clientPC.getFilePath(), clientPC.getFileName()).toAbsolutePath());
                sendFile = new SendFile(clientPC.getFileName(), byteBuf, serverPC.getFilePath());
                nettyClient.sendMessage(sendFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Choose file, not directory", ButtonType.OK);
            alert.showAndWait();
        }
    }

    /**
     * метод отправляет сообщение на сервер о загрузке файла на клиент
     *
     * @param actionEvent - нажатие кнопки Download File
     */
    public void download(ActionEvent actionEvent) {
        if (!Files.isDirectory(Paths.get(serverPC.getFilePath(), serverPC.getFileName()))) {
            nettyClient.sendMessage("download--s-" + serverPC.getFilePath() + "--s-" + serverPC.getFileName());
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Choose file, not directory", ButtonType.OK);
            alert.showAndWait();
        }
    }

    /**
     * метод копирования файла внутри директории клиента или сервера
     *
     * @param actionEvent - нажатие кнопки Copy File
     */
    public void copyFile(ActionEvent actionEvent) {
        utils.chooseFileAlert(serverPC, clientPC);

        PanelController source = null;

        if (serverPC.getFileName() != null) {
            nettyClient.sendMessage("copy--s-" + serverPC.getFileName());
        }
        if (clientPC.getFileName() != null) {
            Path sourcePath = Paths.get(clientPC.getFilePath(), clientPC.getFileName());
            Path destPath = Paths.get(clientPC.getFilePath(), clientPC.getFileName() + "(copy)");

            try {
                utils.methodForCopy(sourcePath, destPath);
                updatePanel();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Copy error", ButtonType.OK);
                alert.showAndWait();
            }
        }
    }

    /**
     * метод удаляет выбранный файл или директорию
     *
     * @param actionEvent - нажатие кнопки Delete File
     */
    public void deleteFile(ActionEvent actionEvent) {
        utils.chooseFileAlert(serverPC, clientPC);

        PanelController source = null;

        if (serverPC.getFileName() != null) {
            source = serverPC;
            nettyClient.sendMessage("rm--s-" + source.getFileName());
        }

        if (clientPC.getFileName() != null) {
            source = clientPC;
            Path sourcePath = Paths.get(source.getFilePath(), source.getFileName());
            utils.methodForDelete(sourcePath);
            updatePanel();
        }

    }

    /**
     * метод для поиска файла во всех папках и выдача полного пути файла в поле info
     *
     * @param actionEvent - нажатие кнопки Search File
     */
    public void search(ActionEvent actionEvent) {
        info.clear();
        String target = null;

        if (serverPC.tableInfo.isFocused()) {
            target = serverPC.getFilePath();
            nettyClient.sendMessage("search--s-"+ filename.getText());
        }
        if (clientPC.tableInfo.isFocused()) {
            target = clientPC.getFilePath();
            utils.methodForSearch(clientPC.getFilePath(), filename.getText(), info);
        }
        if (target == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Choose Folder", ButtonType.OK);
            alert.showAndWait();
        }
    }

    /**
     * метод для регистрации в базе данных, отправляет запрос на сервер
     */
    public void registration() {
        if (nicknameField.getText().length() * login.getText().length() * password.getText().length() != 0) {
            clientDirectory = login.getText() + "_client";
            nettyClient.sendMessage("reg--s-" + login.getText() + "--s-" + password.getText() + "--s-" + nicknameField.getText());
        }
        try {
            Path newDir = Paths.get(clientDirectory);
            Files.createDirectory(newDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * метод для входа в свой профиль через базу данных, запрос отправляется на сервер
     */
    public void authentication() {
        if (login.getText().length() * password.getText().length() != 0) {
            clientDirectory = login.getText() +"_client";
            nettyClient.sendMessage("auth--s-" + login.getText() + "--s-" + password.getText());
        }
        try {
            Path newDir = Paths.get(clientDirectory);
            if (!Files.exists(newDir))
                Files.createDirectory(newDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * метод изменения ника через базу данных, отправляет запрос на сервер
     */
    public void changeNickName() {
        if (nickname != null) {
            nettyClient.sendMessage("nick--s-" + nickname + "--s-" + nicknameField.getText());
        }
    }

    /**
     * метод обновляет директории после изменений и отправляет информацию о новой директории на сервер
     */
    public void updatePanel() {
        Platform.runLater(() -> {
            clientPC.updateList(Paths.get(clientPC.getFilePath()));
            serverPC.updateList(Paths.get(serverPC.getFilePath()));
            nettyClient.sendMessage("cd--s-" + serverPC.getFilePath());
            System.out.println(serverPC.getFilePath());
        });
    }

    /**
     * метод отправки сообщения на сервер об отключении и очистке всех полей на клиенте
     *
     * @param actionEvent - нажатие кнопки Disconnect
     */
    public void disconnect(ActionEvent actionEvent) {
        nettyClient.sendMessage("dis");
        cancelAll();
        clientPC.filePath.clear();
        serverPC.filePath.clear();
        clientPC.tableInfo.getItems().clear();
        serverPC.tableInfo.getItems().clear();
        info.clear();
    }

    /**
     * метод для просмотра содержимого выбранного файла или список файлов в выбранной директории
     *
     * @param actionEvent - нажатие кнопки Show File
     */
    public void showFile(ActionEvent actionEvent) {
        utils.chooseFileAlert(serverPC, clientPC);

        PanelController source = null;

        if (serverPC.getFileName() != null) {
            source = serverPC;
            nettyClient.sendMessage("sw--s-" + source.getFileName());
        }

        if (clientPC.getFileName() != null) {
            source = clientPC;
            Path sourcePath = Paths.get(source.getFilePath(), source.getFileName());
            String readyLine;
            info.setText("Info:\r\n");
            if (Files.isDirectory(sourcePath)) {
                info.appendText("Files in Folder " + source.getFileName() + "\r\n" + String.join("\r\n", new File(String.valueOf(sourcePath)).list()));
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
    }

    /**
     * метод изменяет заголовок окна программы добавляя ник пользователя
     *
     * @param nick новый ник
     */
    private void setTitle(String nick) {
        Platform.runLater(() -> {
            stage = (Stage) nicknameField.getScene().getWindow();
            stage.setTitle("Cloud Storage " + nick);
        });
    }

    /**
     * метод для кнопки ОК, которая соответствует трём операциям: Регистрация, Вход, Смена ника
     *
     * @param actionEvent - нажатие кнопки ОК
     */
    public void executeOperation(ActionEvent actionEvent) {
        switch (operationId) {
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

    /**
     * метод выбора операции Входа пользователя и компонования интерфейса пользователя
     *
     * @param actionEvent - нажатие кнопки Authentication
     */
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

    /**
     * метод выбора операции Регистрации пользователя и компонования интерфейса пользователя
     *
     * @param actionEvent - нажатие кнопки Registration
     */
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

    /**
     * метод выбора операции Смены ника пользователя и компонования интерфейса пользователя
     *
     * @param actionEvent - нажатие кнопки Change Nickname
     */
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

    /**
     * вызов метода возврата
     *
     * @param actionEvent - нажатие кнопки Back
     */
    public void back(ActionEvent actionEvent) {
        cancelAll();
    }

    /**
     * метод возврата без регистрации смены ника и входа. Убирает все поля и кнопки для регистрации
     */
    public void cancelAll() {
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
