package client;

import fileutils.MyFile;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PanelController implements Initializable {

    @FXML
    public TableView<MyFile> tableInfo;
    @FXML
    public TextField filePath;

    /**
     * построение двух таблиц, отображающих файлы и их свойства
     *
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            TableColumn<MyFile, String> fileType = new TableColumn<>("Type");
            fileType.setCellValueFactory(param ->
                    new SimpleStringProperty(param.getValue().getType().getName()));
            fileType.setPrefWidth(50);

            TableColumn<MyFile, String> fileName = new TableColumn<>("Name");
            fileName.setCellValueFactory(param ->
                    new SimpleStringProperty(param.getValue().getFilename()));
            fileName.setPrefWidth(120);

            TableColumn<MyFile, Long> fileSize = new TableColumn<>("Size");
            fileSize.setCellValueFactory(param ->
                    new SimpleObjectProperty(param.getValue().getSize()));
            fileSize.setPrefWidth(90);
            fileSize.setCellFactory(column -> new TableCell<MyFile, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", item);
                        if (item == -1L)
                            text = "dir";
                        setText(text);
                    }
                }
            }
            );

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
            TableColumn<MyFile, String> fileDate = new TableColumn<>("Date");
            fileDate.setCellValueFactory(param ->
                    new SimpleStringProperty(param.getValue().getModifiedTime().format(dateTimeFormatter)));
            fileDate.setPrefWidth(120);

            tableInfo.getColumns().addAll(fileType, fileName, fileSize, fileDate);
            tableInfo.getSortOrder().add(fileType);
            tableInfo.getSortOrder().add(fileName);

            tableInfo.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() == 2) {
                        Path path = Paths.get(filePath.getText())
                                .resolve(tableInfo.getSelectionModel()
                                        .getSelectedItem()
                                        .getFilename());
                        if (Files.isDirectory(path))
                            updateList(path);
                    }
                }
            });
        });
    }

    /**
     * обновление директорий после измененеий или перехода
     *
     * @param path
     */
    public void updateList(Path path) {
        try {
            filePath.setText(path.normalize().toAbsolutePath().toString());
            tableInfo.getItems().clear();
            tableInfo.getItems()
                    .addAll(Files.list(path)
                            .map(MyFile::new)
                            .collect(Collectors.toList()));
            tableInfo.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Error. Cannot update list of files", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void updateList(List<MyFile> list) {
        tableInfo.getItems().clear();
        tableInfo.getItems()
                .addAll(list);
        tableInfo.sort();
    }

    /**
     * возврат вверх по директории до папок сервера и клиента
     *
     * @param actionEvent - нажатие на кнопку Up
     */
    public void upToDirectory(ActionEvent actionEvent) {
        Path up = Paths.get(filePath.getText()).getParent();
        if (filePath.getText().endsWith("server") || filePath.getText().endsWith("client")) {
            return;
        } else {
            updateList(up);
        }
    }

    /**
     * метод возвращает имя выбранного файла
     *
     * @return - имя файла
     */
    public String getFileName() {
        if (!tableInfo.isFocused()) {
            return null;
        }
        return tableInfo.getSelectionModel().getSelectedItem().getFilename();
    }

    /**
     * метод возвращает путь выбранного файла
     *
     * @return - путь файла
     */
    public String getFilePath() {
        return filePath.getText();
    }

    /**
     * метод изменяет путь выбранного файла
     *
     * @param - новый путь файла
     */
    public void setFilePath(String filePath) {
        this.filePath.setText(filePath);
    }
}
