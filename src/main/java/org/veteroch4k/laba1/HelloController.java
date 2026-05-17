package org.veteroch4k.laba1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.veteroch4k.laba1.Lexicon.LexicalAnalyzer;
import org.veteroch4k.laba1.Lexicon.Token;
import org.veteroch4k.laba1.Lexicon.TokenType;
import org.veteroch4k.laba1.Regex.RegexSearcher;
import org.veteroch4k.laba1.Regex.SearchResult;
import org.veteroch4k.laba1.Syntax.ErrorItem;
import org.veteroch4k.laba1.Syntax.SyntaxAnalyzer;

public class HelloController {


    @FXML private TabPane editorTabPane;
    @FXML private TabPane outputTabPane;
    @FXML private Label statusLabel;
    @FXML private Label positionLabel;
    @FXML private TableView<Token> tokenTable;
    @FXML private TableView<ErrorItem> errorTable;
    @FXML private ComboBox<String> regexTypeComboBox;
    @FXML private TableView<SearchResult> regexResultTable;

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

        if (tokenTable != null) {
            tokenTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    TextArea activeTextArea = getActiveTextArea();
                    if (activeTextArea != null) {
                        highlightToken(activeTextArea, newSelection);
                    }
                }
            });
        }

        if (tokenTable != null) {
            TableColumn<Token, String> typeCol = new TableColumn<>("Тип лексемы");
            typeCol.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().type().name()));
            typeCol.setPrefWidth(150);

            TableColumn<Token, String> valueCol = new TableColumn<>("Значение");
            valueCol.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().value()));
            valueCol.setPrefWidth(150);

            TableColumn<Token, Number> lineCol = new TableColumn<>("Строка");
            lineCol.setCellValueFactory(
                cellData -> new SimpleIntegerProperty(cellData.getValue().line()));
            lineCol.setPrefWidth(70);

            TableColumn<Token, Number> colCol = new TableColumn<>("Символ");
            colCol.setCellValueFactory(
                cellData -> new SimpleIntegerProperty(cellData.getValue().column()));
            colCol.setPrefWidth(70);

            tokenTable.getColumns().setAll(typeCol, valueCol, lineCol, colCol);
        }

        if (errorTable != null) {
            TableColumn<ErrorItem, String> fileCol = (TableColumn<ErrorItem, String>) errorTable.getColumns().get(0);
            fileCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().filePath()));

            TableColumn<ErrorItem, Number> errLineCol = (TableColumn<ErrorItem, Number>) errorTable.getColumns().get(1);
            errLineCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().line()));

            TableColumn<ErrorItem, Number> errColCol = (TableColumn<ErrorItem, Number>) errorTable.getColumns().get(2);
            errColCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().column()));

            TableColumn<ErrorItem, String> msgCol = (TableColumn<ErrorItem, String>) errorTable.getColumns().get(3);
            msgCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().message()));

            errorTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    TextArea activeTextArea = getActiveTextArea();
                    if (activeTextArea != null) {
                        highlightToken(activeTextArea, new Token(TokenType.ERROR, newSelection.errorValue(), newSelection.line(), newSelection.column()));
                    }
                }
            });
        }

        if (regexTypeComboBox != null) {
            regexTypeComboBox.getItems().addAll("Числа", "Пароли", "Email", "Email (Автомат)");
            regexTypeComboBox.setValue("Числа");
        }

        if (regexResultTable != null) {
            TableColumn<SearchResult, String> textCol = new TableColumn<>("Подстрока");
            textCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getText()));
            textCol.setPrefWidth(300);

            TableColumn<SearchResult, Number> lineCol2 = new TableColumn<>("Строка");
            lineCol2.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getLine()));

            TableColumn<SearchResult, Number> colCol2 = new TableColumn<>("Символ");
            colCol2.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getColumn()));

            TableColumn<SearchResult, Number> lenCol = new TableColumn<>("Длина");
            lenCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getLength()));

            regexResultTable.getColumns().setAll(textCol, lineCol2, colCol2, lenCol);

            regexResultTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    TextArea activeTextArea = getActiveTextArea();
                    if (activeTextArea != null) {
                        activeTextArea.requestFocus();
                        activeTextArea.selectRange(newSelection.getStartOffset(), newSelection.getEndOffset());
                    }
                }
            });
        }

    }

    @FXML
    public void onRegexSearchClick(ActionEvent event) {
        TextArea activeTextArea = getActiveTextArea();
        if (activeTextArea == null || regexResultTable == null || regexTypeComboBox == null) return;

        regexResultTable.getItems().clear();

        String text = activeTextArea.getText();
        String searchType = regexTypeComboBox.getValue();

        if (text.isEmpty()) {
            statusLabel.setText("Статус: Нет данных для поиска.");
            return;
        }

        List<SearchResult> results;

        if ("Email (Автомат)".equals(searchType)) {
            results = RegexSearcher.searchEmailWithAutomaton(text);
        } else {
            results = RegexSearcher.search(text, searchType);
        }

        regexResultTable.getItems().addAll(results);

        statusLabel.setText("Статус: Поиск завершен. Найдено совпадений (" + searchType + "): " + results.size());

        outputTabPane.getSelectionModel().select(2);
    }

    @FXML
    public void onStartAnalysisClick(ActionEvent event) {
        TextArea activeTextArea = getActiveTextArea();
        if (activeTextArea == null || tokenTable == null || errorTable == null) return;

        tokenTable.getItems().clear();
        errorTable.getItems().clear();

        File activeFile = getActiveFile();
        String fileName = (activeFile != null) ? activeFile.getName() : "Новый документ";

        LexicalAnalyzer lexer = new LexicalAnalyzer();
        List<Token> allTokens = lexer.analyze(activeTextArea.getText());

        List<Token> validTokensForParser = new ArrayList<>();
        int lexicalErrorsCount = 0;

        for (Token t : allTokens) {
            if (t.type() == TokenType.ERROR) {
                String msg = "Лексическая ошибка: Недопустимый символ";
                errorTable.getItems().add(new ErrorItem(fileName, t.line(), t.column(), msg, t.value()));
                lexicalErrorsCount++;
            } else if (t.type() != TokenType.WHITESPACE) {
                if (t.type() != TokenType.EOF) {
                    tokenTable.getItems().add(t);
                }
                validTokensForParser.add(t);
            }
        }

        SyntaxAnalyzer parser = new SyntaxAnalyzer(validTokensForParser);
        List<ErrorItem> syntaxErrors = parser.parse();

        for (ErrorItem err : syntaxErrors) {
            errorTable.getItems().add(new ErrorItem(fileName, err.line(), err.column(), err.message(), err.errorValue()));
        }

        int totalErrors = lexicalErrorsCount + syntaxErrors.size();
        if (totalErrors == 0) {
            statusLabel.setText("Статус: Анализ завершен успешно. Ошибок не найдено.");

            String astTree = parser.getAstTree();
            showInfoWindow("AST Дерево (Семантический анализ)", astTree);

        } else {
            statusLabel.setText("Статус: Анализ завершен. Найдено ошибок: " + totalErrors);
        }

        tokenTable.refresh();
        errorTable.refresh();
    }

    @FXML
    public void onShowAstClick(ActionEvent event) {
        TextArea activeTextArea = getActiveTextArea();
        if (activeTextArea == null || activeTextArea.getText().trim().isEmpty()) {
            showInfoWindow("Ошибка", "Нет текста для построения дерева.");
            return;
        }

        LexicalAnalyzer lexer = new LexicalAnalyzer();
        List<Token> allTokens = lexer.analyze(activeTextArea.getText());
        List<Token> validTokensForParser = new ArrayList<>();

        for (Token t : allTokens) {
            if (t.type() == TokenType.ERROR) {
                showInfoWindow("Ошибка", "В коде присутствуют лексические ошибки. Дерево не может быть построено.");
                return;
            } else if (t.type() != TokenType.WHITESPACE) {
                validTokensForParser.add(t);
            }
        }

        SyntaxAnalyzer parser = new SyntaxAnalyzer(validTokensForParser);
        List<ErrorItem> syntaxErrors = parser.parse();

        if (syntaxErrors.isEmpty()) {
            Object uiRoot = parser.getGraphicalAst();
            if (uiRoot != null) {
                AstVisualizer.showWindow(uiRoot);
            } else {
                showInfoWindow("Ошибка", "Не удалось сформировать структуру дерева.");
            }
        } else {
            showInfoWindow("Ошибка AST", "Невозможно отобразить дерево: в коде присутствуют синтаксические или семантические ошибки. Запустите обычный анализ, чтобы посмотреть таблицу ошибок.");
        }
    }

    private void highlightToken(TextArea textArea, Token token) {
        textArea.requestFocus();
        int caretPos = 0;
        String text = textArea.getText();
        int currentLine = 1;
        int currentCol = 1;

        for (int i = 0; i < text.length(); i++) {
            if (currentLine == token.line() && currentCol == token.column()) {
                caretPos = i;
                break;
            }
            if (text.charAt(i) == '\n') {
                currentLine++;
                currentCol = 1;
            } else {
                currentCol++;
            }
        }
        textArea.selectRange(caretPos, caretPos + token.value().length());
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
            editorTabPane.getSelectionModel().getSelectedItem().setUserData(file);
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
            activeTab.setUserData(file);
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

    @FXML
    private void onSpravkaClick() {
        String content = readTextFromResources("/kr/spravka.txt");

        showInfoWindow("Справка", content);
    }

    // --- ОБРАБОТЧИКИ НАЖАТИЙ КНОПОК МЕНЮ ---

    @FXML
    void showTaskAction(ActionEvent event) {
        String content = readTextFromResources("/kr/task.txt");
        showInfoWindow("Постановка задачи", content);
    }

    @FXML
    void showGrammarAction(ActionEvent event) {
        String content = readTextFromResources("/kr/grammar.txt");
        showInfoWindow("Грамматика", content);
    }

    @FXML
    void showClassificationAction(ActionEvent event) {
        String content = readTextFromResources("/kr/grammar_class.txt");
        showInfoWindow("Классификация грамматики", content);
    }

    @FXML
    void showAnalysisAction(ActionEvent event) {
        String content = readTextFromResources("/kr/analyze_method.txt");
        showInfoWindow("Метод анализа", content);
    }

    @FXML
    void showExamplesAction(ActionEvent event) {
        String content = readTextFromResources("/kr/test_examples.txt");
        showInfoWindow("Тестовые примеры", content);
    }

    @FXML
    void showReferencesAction(ActionEvent event) {
        String content = readTextFromResources("/kr/literature.txt");
        showInfoWindow("Список литературы", content);
    }

    @FXML
    void showCodeAction(ActionEvent event) {
        String content = readTextFromResources("/kr/code.txt");
        showInfoWindow("Исходный код программы", content);
    }

    @FXML
    void showInfoAction(ActionEvent event) {
        String content = readTextFromResources("/kr/info.txt");
        showInfoWindow("О программе", content);
    }


    private void showInfoWindow(String title, String content) {
        Stage infoStage = new Stage();
        infoStage.setTitle(title);

        infoStage.initModality(Modality.NONE);

        TextArea textArea = new TextArea(content);
        textArea.setEditable(false); // Запрещаем редактирование
        textArea.setWrapText(true);  // Включаем перенос строк

        VBox vbox = new VBox(textArea);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        Scene scene = new Scene(vbox, 600, 400);
        infoStage.setScene(scene);

        infoStage.show();
    }
    private String readTextFromResources(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {

            if (is == null) {
                return "Ошибка: Файл '" + resourcePath + "' не найден в ресурсах программы.";
            }

            return new String(is.readAllBytes(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            return "Критическая ошибка при чтении файла: " + e.getMessage();
        }
    }





    @FXML public void onUndoClick(ActionEvent event) { if(getActiveTextArea() != null) getActiveTextArea().undo(); }
    @FXML public void onRedoClick(ActionEvent event) { if(getActiveTextArea() != null) getActiveTextArea().redo(); }
    @FXML public void onCutClick(ActionEvent event) { if(getActiveTextArea() != null) getActiveTextArea().cut(); }
    @FXML public void onCopyClick(ActionEvent event) { if(getActiveTextArea() != null) getActiveTextArea().copy(); }
    @FXML public void onPasteClick(ActionEvent event) { if(getActiveTextArea() != null) getActiveTextArea().paste(); }
    @FXML public void onDeleteClick(ActionEvent event) { if(getActiveTextArea() != null) getActiveTextArea().replaceSelection(""); }
    @FXML public void onSelectAllClick(ActionEvent event) { if(getActiveTextArea() != null) getActiveTextArea().selectAll(); }


}