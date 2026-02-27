package org.veteroch4k.laba1;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class HelloController {
    @FXML
    private TextArea codeEditorArea;

    @FXML
    public void onExitClick(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    // тут будет логика для файликов
    @FXML
    public void onOpenFileClick(ActionEvent event) {
        System.out.println("Когда-нибудь здесь откроется файл");
    }


}