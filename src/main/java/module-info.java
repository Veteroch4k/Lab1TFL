module org.veteroch4k.laba1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;


    opens org.veteroch4k.laba1 to javafx.fxml;
    exports org.veteroch4k.laba1;
}