package client;

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
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PanelController implements Initializable {

    @FXML
    public TableView<MyFile> tableInfo;
    @FXML
    public TextField filePath;



    @FXML
    public TextField fileActive;

    // @FXML
    //public ComboBox<String> side;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
        fileSize.setCellFactory(column -> {
                    return new TableCell<MyFile, Long>() {
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
                    };
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

        //side.getItems().clear();
        ///side.getItems().add(serverDirectory);
        //side.getItems().add(clientDirectory);


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

    }

    public void updateList(Path path) {
        try {
            filePath.setText(path.normalize().toString());
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

    public void upToDirectory(ActionEvent actionEvent) {
        Path up = Paths.get(filePath.getText()).getParent();
        if (!up.startsWith("server") || !up.startsWith("client")) {
            updateList(up);
        }
    }

    /*public void selectDirectory(ActionEvent actionEvent) {
        ComboBox<String> folder = (ComboBox<String>) actionEvent.getSource();
        updateList(Paths.get(folder.getSelectionModel().getSelectedItem()));
    }*/

    public String getFileName (){
        if (!tableInfo.isFocused()){
            return null;
        }
        return tableInfo.getSelectionModel().getSelectedItem().getFilename();
    }

    public String getFilePath(){
        return filePath.getText();
    }

    public void setFilePath(String filePath) {
        this.filePath.setText(filePath);
    }
}