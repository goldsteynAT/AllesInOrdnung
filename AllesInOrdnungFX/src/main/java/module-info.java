module com.example.allesinordnungfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;

    opens com.example.allesinordnungfx to javafx.fxml;
    exports com.example.allesinordnungfx;
}
