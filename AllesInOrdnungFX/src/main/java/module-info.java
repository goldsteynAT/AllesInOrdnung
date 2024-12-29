module com.example.allesinordnungfx {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.allesinordnungfx to javafx.fxml;
    exports com.example.allesinordnungfx;
}
