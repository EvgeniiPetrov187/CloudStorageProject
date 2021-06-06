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

    /**
     * Инициализация окна, таблиц из файла Panel и дополнительных методов из класса Util
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

    /**
     * метод создания файла на сервере или клиенте
     * @param actionEvent - нажатие кнопки Create File
     */
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


    /**
     * метод создания директории на сервере или клиенте
     * @param actionEvent - нажатие кнопки Create Directory
     */
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

    /**
     * метод загрузки файла на сервер, на клиенте файл удаляется
     * @param actionEvent - нажатие кнопки Upload File
     */
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

    /**
     * метод загрузки файла на клиент, на сервере файл удаляется
     * @param actionEvent - нажатие кнопки Download File
     */
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

    /**
     * метод копирования файла из выбранной директории сервера на клиент и наоборот
     * @param actionEvent - нажатие кнопки Copy File
     */
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

    /**
     * метод удаляет выбранный файл или директорию
     * @param actionEvent - нажатие кнопки Delete File
     */
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

    /**
     * метод для поиска файла во всех папках и выдача полного пути файла в поле info
     * @param actionEvent - нажатие кнопки Search File
     */
    public void search(ActionEvent actionEvent) {
        utils.methodForSearch(clientPC.getFilePath(), filename.getText(), info);
        utils.methodForSearch(serverPC.getFilePath(), filename.getText(), info);
    }

    /**
     * метод для регистрации в базе данных, отправляет запрос на сервер
     */
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

    /**
     * метод для входа в свой профиль через базу данных, запрос отправляется на сервер
     */
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

    /**
     * метод изменения ника через базу данных, отправляет запрос на сервер
     */
    public void changeNickName() {
        if (nickname != null) {
            nettyClient.sendMessage("nick " + nickname + " " + nicknameField.getText());
        }
    }

    /**
     * метод обновляет директории после изменений и отправляет информацию о новой директории на сервер
     */
    public void updatePanel() {
        clientPC.updateList(Paths.get(clientPC.getFilePath()));
        serverPC.updateList(Paths.get(serverPC.getFilePath()));
        nettyClient.sendMessage("cd " + serverPC.getFilePath());
        System.out.println(serverPC.getFilePath());
    }

    /**
     * метод для просмотра содержимого выбранного файла или список файлов в выбранной директории
     * @param actionEvent - нажатие кнопки Show File
     */
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

    /**
     * метод изменяет заголовок окна программы добавляя ник пользователя
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
     * @param actionEvent - нажатие кнопки ОК
     */
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

    /**
     * метод выбора операции Входа пользователя и компонования интерфейса пользователя
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
     * @param actionEvent - нажатие кнопки Back
     */
    public void back(ActionEvent actionEvent){
        cancelAll();
    }
    /**
     * метод возврата без регистрации смены ника и входа. Убирает все поля и кнопки для регистрации
     */
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
