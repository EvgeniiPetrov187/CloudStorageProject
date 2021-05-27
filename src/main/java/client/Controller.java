package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private NettyClient nettyClient;
    private String directory = "server/";

    @FXML
    public Text fileDirectory;
    @FXML
    public TextArea Info;
    @FXML
    public TextArea serverInfo;
    @FXML
    public TextField filename;
    @FXML
    public Button create;
    @FXML
    public TextArea clientInfo;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nettyClient = new NettyClient((args) -> {
            serverInfo.setText(args); // временно
        });
    }

    // после входа в профиль
    public void connect() {
        nettyClient = new NettyClient((args) -> {
            serverInfo.setText(args);
        });
    }


    // создание файла - работает
    public void createAction(ActionEvent actionEvent) {
        nettyClient.sendMessage("touch " + filename.getText());
        filename.clear();
    }

    // нужно доработать
    public void upload(ActionEvent actionEvent) {
        nettyClient.sendMessage("copy " + filename.getText() + " server");
        filename.clear();
    }

    // скачивание файла с сервера - работает
    public void download(ActionEvent actionEvent) {
        nettyClient.sendMessage("copy " + filename.getText() + " client");
        filename.clear();
    }

    // копирование файла - работает
    public void copyFile(ActionEvent actionEvent) {
        nettyClient.sendMessage("copy " + filename.getText());
        filename.clear();
    }

    // удаление файла или директории - работает
    public void deleteFile(ActionEvent actionEvent) {
        nettyClient.sendMessage("rm " + filename.getText());
        filename.clear();
    }

    // размер файла - работает
    public void viewFileSize(ActionEvent actionEvent) {
        nettyClient.sendMessage("fs " + filename.getText());
        filename.clear();
    }

    // время файла - работает
    public void viewFileTime(ActionEvent actionEvent) {
        nettyClient.sendMessage("ft " + filename.getText());
        filename.clear();
    }

    // поиск файла - работает
    public void search(ActionEvent actionEvent) {
        nettyClient.sendMessage("find " + filename.getText());
        filename.clear();
    }

    // не готов
    public void sortFiles(ActionEvent actionEvent) {

    }

    // изменение директории - нужно доработать
    public void changeDirectory(ActionEvent actionEvent) {
        nettyClient.sendMessage("cd " + filename.getText());
        StringBuilder sb = new StringBuilder();
        if ("~".equals(filename.getText())) {
            directory = "server/";
        } else if ("..".equals(filename.getText()) && !directory.equals("server/")) {
            String[] list = directory.split("/");
            for (int i = 0; i < list.length - 1; i++) {
                sb.append(list[i] + "/");
            }
            directory = sb.toString();
        } else {
            sb.append(directory).append(filename.getText() + "/");
            directory = sb.toString();
        }
        fileDirectory.setText(directory);
        filename.clear();
    }

    // создание директории - работает
    public void createDirectory(ActionEvent actionEvent) {
        nettyClient.sendMessage("mkdir " + filename.getText());
        filename.clear();
    }

    // список файлов - нужно доработать
    public void listFile(ActionEvent actionEvent) {
        nettyClient.sendMessage("ls " + filename.getText());
        filename.clear();
    }
}
