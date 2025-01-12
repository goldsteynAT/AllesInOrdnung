package com.example.allesinordnungfx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.yaml.snakeyaml.Yaml;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.geometry.Insets;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginScreen extends Application {

    private final String usersFilePath = "users.yaml"; // Datei zur Speicherung von Nutzerdaten

    @Override
    public void start(Stage primaryStage) {
        // Überschrift
        Label titleLabel = new Label("Alles In Ordnung");
        titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

        /* Untertitel
        Label subtitle = new Label("Projekt für Programmieren 1, WS 2024/25\n"
                + "Autor*Innen: Jochen Schmidtberger, Corinna Jäger, Christina Seidl und Daniel Essl");
        subtitle.setStyle("-fx-font-size: 14;");
*/
        // Zwei weitere Zeilen
        Label infoLine1 = new Label("Projekt für Programmieren 1, WS 2024/25");
        Label infoLine2 = new Label("Autor*Innen: Jochen Schmidtberger, Corinna Jäger, Christina Seidl und Daniel Essl");
        infoLine1.setStyle("-fx-font-size: 16px;");
        infoLine2.setStyle("-fx-font-size: 16px;");

        // Layout-Container für die Texte
        VBox textContainer = new VBox(10, titleLabel, infoLine1, infoLine2);
        textContainer.setAlignment(Pos.TOP_CENTER);

        // Login-Formular (GridPane)
        Label userLabel = new Label("Username:");
        TextField userField = new TextField();

        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

        GridPane loginGrid = new GridPane();
        loginGrid.setHgap(10);
        loginGrid.setVgap(10);
        loginGrid.add(userLabel, 0, 0);
        loginGrid.add(userField, 1, 0);
        loginGrid.add(passLabel, 0, 1);
        loginGrid.add(passField, 1, 1);
        loginGrid.add(loginButton, 0, 2);
        loginGrid.add(registerButton, 1, 2);

        // Kombinierte Layouts (Text oben, Login unten)
        VBox layout = new VBox(20, textContainer, loginGrid);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        // Rahmen und Hintergrund
        BorderStroke borderStroke = new BorderStroke(
                Color.DARKBLUE,  // Rahmenfarbe
                BorderStrokeStyle.SOLID, // Rahmenstil
                new CornerRadii(5),      // Eckenradius
                new BorderWidths(20)     // Rahmenbreite
        );

        VBox wrappedLayout = new VBox(layout); // Layout in einen neuen Container einbinden
        wrappedLayout.setBorder(new Border(borderStroke));
        wrappedLayout.setBackground(new Background(
                new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, null)
        ));





        /* Login-Formular (GridPane)
        Label userLabel = new Label("Username:");
        TextField userField = new TextField();

        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

        GridPane loginGrid = new GridPane();
        loginGrid.setHgap(10);
        loginGrid.setVgap(10);
        loginGrid.add(userLabel, 0, 0);
        loginGrid.add(userField, 1, 0);
        loginGrid.add(passLabel, 0, 1);
        loginGrid.add(passField, 1, 1);
        loginGrid.add(loginButton, 0, 2);
        loginGrid.add(registerButton, 1, 2);

        // Layout zusammenstellen
        VBox layout = new VBox(10, titleLabel, infoLine1, infoLine2, loginGrid);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
*/
        // Login-Logik
        loginButton.setOnAction(e -> {
            String username = userField.getText().trim();
            String password = passField.getText().trim();

            if (validateLogin(username, password)) {
                String userDirectoryPath = "users/" + username;
                BookManagerApp app = new BookManagerApp();
                app.startWithUser(primaryStage, userDirectoryPath); // Start mit Benutzerverzeichnis
            } else {
                showAlert("Login Error", "Invalid username or password.");
            }
        });

        // Registrierung-Logik
        registerButton.setOnAction(e -> {
            String username = userField.getText().trim();
            String password = passField.getText().trim();

            if (registerUser(username, password)) {
                showInfo("Registration Successful", "You can now log in with your credentials.");
            } else {
                showAlert("Registration Error", "Username already exists or invalid input.");
            }
        });

        // Szene erstellen und anzeigen
        Scene scene = new Scene(wrappedLayout, 500, 400); // Breite x Höhe
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login-Screen");
        primaryStage.show();
    }

    // Validiert Login-Daten
    private boolean validateLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) return false;

        File file = new File(usersFilePath);
        if (!file.exists()) return false;

        Yaml yaml = new Yaml();
        try (FileReader reader = new FileReader(file)) {
            Map<String, String> users = yaml.load(reader);
            if (users != null && password.equals(users.get(username))) {
                // Benutzerverzeichnis prüfen
                File userDir = new File("users/" + username);
                if (userDir.exists() && userDir.isDirectory()) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }



    // Registriert einen neuen Benutzer
    private boolean registerUser(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) return false;

        File file = new File(usersFilePath);
        Map<String, String> users = new HashMap<>();

        Yaml yaml = new Yaml();
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                users = yaml.load(reader);
                if (users == null) users = new HashMap<>();
                if (users.containsKey(username)) return false; // Benutzer existiert bereits
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        // 1. Benutzerdaten speichern
        users.put(username, password);
        try (FileWriter writer = new FileWriter(file)) {
            yaml.dump(users, writer);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // 2. Benutzerverzeichnis erstellen
        String userDirectoryPath = "users/" + username;
        File userDirectory = new File(userDirectoryPath);
        if (!userDirectory.exists()) {
            if (!userDirectory.mkdirs()) {
                System.err.println("Failed to create user directory: " + userDirectoryPath);
                return false;
            }
        }

        // 3. Default-Collection für den Benutzer erstellen
        String defaultCollectionPath = userDirectoryPath + "/default.yaml";
        CollectionManager collectionManager = new CollectionManager();
        collectionManager.addNewCollection("default", userDirectoryPath);

        System.out.println("User registered successfully with directory: " + userDirectoryPath);
        return true;
    }

    // Hilfsmethoden für Dialoge
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}