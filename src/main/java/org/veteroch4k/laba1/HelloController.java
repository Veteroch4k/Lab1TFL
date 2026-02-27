package org.veteroch4k.laba1;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class HelloController {


    @FXML private TabPane editorTabPane;
    @FXML private Label statusLabel;
    @FXML private Label positionLabel;

    private double currentFontSize = 14.0;

    @FXML
    public void initialize() {
        createNewTab("Новый документ", "");

        editorTabPane.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        editorTabPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                for (File file : db.getFiles()) {
                    openFileInNewTab(file);
                }
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });

        editorTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && newTab.getContent() instanceof TextArea) {
                updateStatusBar((TextArea) newTab.getContent());
            }
        });
    }


    private void createNewTab(String title, String content) {

        TextArea textArea = new TextArea(content);
        textArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: " + currentFontSize + ";");
        textArea.setWrapText(false);
        HBox.setHgrow(textArea, Priority.ALWAYS);

        TextArea lineNumbersArea = new TextArea("1\n");
        lineNumbersArea.setEditable(false);
        lineNumbersArea.setPrefWidth(50);
        lineNumbersArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: " + currentFontSize + "; -fx-control-inner-background: #f0f0f0; -fx-text-fill: #888888;");

        lineNumbersArea.setMouseTransparent(true);

        textArea.textProperty().addListener((obs, oldText, newText) -> {
            int lines = newText.split("\n", -1).length;
            StringBuilder numbers = new StringBuilder();
            for (int i = 1; i <= lines; i++) {
                numbers.append(i).append("\n");
            }
            lineNumbersArea.setText(numbers.toString());
        });

        Platform.runLater(() -> {
            ScrollBar codeScroll = (ScrollBar) textArea.lookup(".scroll-bar:vertical");
            ScrollBar lineScroll = (ScrollBar) lineNumbersArea.lookup(".scroll-bar:vertical");
            if (codeScroll != null && lineScroll != null) {
                lineScroll.valueProperty().bindBidirectional(codeScroll.valueProperty());
            }
        });

        textArea.setOnScroll(event -> {
            if (event.isControlDown()) {
                if (event.getDeltaY() > 0) currentFontSize += 1;
                else if (event.getDeltaY() < 0) currentFontSize = Math.max(8, currentFontSize - 1);

                // Применяем новый масштаб к обоим полям
                textArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: " + currentFontSize + ";");
                lineNumbersArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: " + currentFontSize + "; -fx-control-inner-background: #f0f0f0; -fx-text-fill: #888888;");
                event.consume();
            }
        });

        textArea.caretPositionProperty().addListener((obs, oldVal, newVal) -> updateStatusBar(textArea));

        HBox editorContainer = new HBox(lineNumbersArea, textArea);

        Tab tab = new Tab(title);
        tab.setContent(editorContainer);

        tab.getProperties().put("editor", textArea);

        editorTabPane.getTabs().add(tab);
        editorTabPane.getSelectionModel().select(tab);

        statusLabel.setText("Статус: Создан/Открыт " + title);
        updateStatusBar(textArea);
    }

    private void updateStatusBar(TextArea textArea) {
        int caretPos = textArea.getCaretPosition();
        String text = textArea.getText();
        int line = 1; int column = 1;

        for (int i = 0; i < caretPos; i++) {
            if (i < text.length() && text.charAt(i) == '\n') {
                line++; column = 1;
            } else {
                column++;
            }
        }
        positionLabel.setText("Строка: " + line + ", Столбец: " + column);
    }

    private TextArea getActiveTextArea() {
        Tab tab = editorTabPane.getSelectionModel().getSelectedItem();
        if (tab != null && tab.getProperties().containsKey("editor")) {
            return (TextArea) tab.getProperties().get("editor");
        }
        return null;
    }

    private File getActiveFile() {
        Tab tab = editorTabPane.getSelectionModel().getSelectedItem();
        if (tab != null && tab.getUserData() instanceof File) return (File) tab.getUserData();
        return null;
    }

    @FXML public void onNewClick(ActionEvent event) { createNewTab("Новый документ", ""); }

    @FXML
    public void onOpenFileClick(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(editorTabPane.getScene().getWindow());
        if (file != null) openFileInNewTab(file);
    }

    private void openFileInNewTab(File file) {
        try {
            String content = Files.readString(file.toPath());
            createNewTab(file.getName(), content);
            editorTabPane.getSelectionModel().getSelectedItem().setUserData(file); // Привязываем файл к вкладке
        } catch (IOException e) {
            System.err.println("Ошибка чтения: " + e.getMessage());
        }
    }

    @FXML
    public void onSaveClick(ActionEvent event) {
        File activeFile = getActiveFile();
        if (activeFile != null) saveToFile(activeFile);
        else onSaveAsClick(event);
    }

    @FXML
    public void onSaveAsClick(ActionEvent event) {
        TextArea activeTextArea = getActiveTextArea();
        if (activeTextArea == null) return;

        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showSaveDialog(editorTabPane.getScene().getWindow());

        if (file != null) {
            saveToFile(file);
            Tab activeTab = editorTabPane.getSelectionModel().getSelectedItem();
            activeTab.setText(file.getName());
            activeTab.setUserData(file); // Сохраняем привязку
        }
    }

    private void saveToFile(File file) {
        TextArea activeTextArea = getActiveTextArea();
        if (activeTextArea != null) {
            try {
                Files.writeString(file.toPath(), activeTextArea.getText());
                statusLabel.setText("Статус: Файл " + file.getName() + " сохранен");
            } catch (IOException e) {
                System.err.println("Ошибка сохранения: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onExitClick(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Выход"); alert.setHeaderText("Выход из программы");
        alert.setContentText("Уверены, что хотите выйти? Несохраненные данные будут потеряны.");

        ButtonType btnExit = new ButtonType("Выйти без сохранения");
        ButtonType btnCancel = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnExit, btnCancel);

        alert.showAndWait().ifPresent(type -> {
            if (type == btnExit) { Platform.exit(); System.exit(0); }
        });
    }


    @FXML public void onUndoClick(ActionEvent event) { if(getActiveTextArea() != null) getActiveTextArea().undo(); }
    @FXML public void onRedoClick(ActionEvent event) { if(getActiveTextArea() != null) getActiveTextArea().redo(); }
    @FXML public void onCutClick(ActionEvent event) { if(getActiveTextArea() != null) getActiveTextArea().cut(); }
    @FXML public void onCopyClick(ActionEvent event) { if(getActiveTextArea() != null) getActiveTextArea().copy(); }
    @FXML public void onPasteClick(ActionEvent event) { if(getActiveTextArea() != null) getActiveTextArea().paste(); }
    @FXML public void onDeleteClick(ActionEvent event) { if(getActiveTextArea() != null) getActiveTextArea().replaceSelection(""); }
    @FXML public void onSelectAllClick(ActionEvent event) { if(getActiveTextArea() != null) getActiveTextArea().selectAll(); }


}