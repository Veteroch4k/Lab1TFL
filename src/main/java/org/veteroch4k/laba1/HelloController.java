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
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class HelloController {
    @FXML
    private TextArea codeEditorArea;

    private File currentFile = null;

    @FXML
    public void onNewClick(ActionEvent event) {
        codeEditorArea.clear();
        currentFile = null;
    }


    @FXML
    public void onOpenFileClick(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Открыть файл");

        Stage stage = (Stage) codeEditorArea.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                String content = Files.readString(file.toPath());
                codeEditorArea.setText(content);
                currentFile = file;
            } catch (IOException e) {
                System.err.println("Ошибка при чтении файла: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onSaveClick(ActionEvent event) {

        if (currentFile != null) {
            saveToFile(currentFile);
        } else {

            onSaveAsClick(event);
        }
    }

    @FXML
    public void onSaveAsClick(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить файл как...");

        Stage stage = (Stage) codeEditorArea.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            saveToFile(file);
            currentFile = file;
        }
    }

    private void saveToFile(File file) {
        try {
            Files.writeString(file.toPath(), codeEditorArea.getText());
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении файла: " + e.getMessage());
        }
    }

    @FXML
    public void onExitClick(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение выхода");
        alert.setHeaderText("Выход из программы");
        alert.setContentText("Сохранить текущий документ перед выходом?");

        ButtonType buttonTypeSave = new ButtonType("Сохранить и выйти");
        ButtonType buttonTypeExit = new ButtonType("Выйти без сохранения");
        ButtonType buttonTypeCancel = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeSave, buttonTypeExit, buttonTypeCancel);

        // Показываем окно и ждем, что нажмет пользователь
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == buttonTypeSave) {
                onSaveClick(new ActionEvent());
                Platform.exit();
                System.exit(0);
            } else if (result.get() == buttonTypeExit) {
                Platform.exit();
                System.exit(0);
            }
        }
    }

    /* Логики правки */

    @FXML
    public void onUndoClick(ActionEvent event) { codeEditorArea.undo(); }

    @FXML
    public void onRedoClick(ActionEvent event) { codeEditorArea.redo(); }

    @FXML
    public void onCutClick(ActionEvent event) { codeEditorArea.cut(); }

    @FXML
    public void onCopyClick(ActionEvent event) { codeEditorArea.copy(); }

    @FXML
    public void onPasteClick(ActionEvent event) { codeEditorArea.paste(); }

    @FXML
    public void onDeleteClick(ActionEvent event) {codeEditorArea.replaceSelection("");}

    @FXML
    public void onSelectAllClick(ActionEvent event) { codeEditorArea.selectAll(); }



}