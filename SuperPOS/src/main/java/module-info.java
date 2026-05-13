module com.supermarket.pos {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.supermarket.pos to javafx.fxml, javafx.base;
    exports com.supermarket.pos;
}