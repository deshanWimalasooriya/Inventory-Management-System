module com.supermarket.pos {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    // UPDATE THIS LINE to include javafx.base
    opens com.supermarket.pos to javafx.fxml, javafx.base;

    exports com.supermarket.pos;
}