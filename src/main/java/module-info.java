module com.cspinventory {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.apache.poi.ooxml;
    requires org.apache.poi.poi;
    requires org.xerial.sqlitejdbc;
    requires java.logging;

    opens com.cspinventory.controller to javafx.fxml;

    exports com.cspinventory.app;
    exports com.cspinventory.model;
}
