module org.veteroch4k.laba1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;


    opens org.veteroch4k.laba1 to javafx.fxml;
    exports org.veteroch4k.laba1;
  exports org.veteroch4k.laba1.Lexicon;
  opens org.veteroch4k.laba1.Lexicon to javafx.fxml;
  exports org.veteroch4k.laba1.Regex;
  opens org.veteroch4k.laba1.Regex to javafx.fxml;
  exports org.veteroch4k.laba1.Syntax;
  opens org.veteroch4k.laba1.Syntax to javafx.fxml;
}