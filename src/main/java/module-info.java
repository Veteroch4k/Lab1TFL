module org.veteroch4k.laba1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;


    opens org.veteroch4k.laba1 to javafx.fxml;
    exports org.veteroch4k.laba1;
}