package com.example.allesinordnungfx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
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

        // Zwei weitere Zeilen
        Label infoLine1 = new Label("Projekt für Programmieren 1, WS 2024/25");
        Label infoLine2 = new Label("Autor*Innen: Daniel Essl, Corinna Jäger, Jochen Schmidtberger und Christina Seidl");
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

        GridPane loginGrid = new GridPane();
        loginGrid.setHgap(10);
        loginGrid.setVgap(10);
        loginGrid.add(userLabel, 0, 0);
        loginGrid.add(userField, 1, 0);
        loginGrid.add(passLabel, 0, 1);
        loginGrid.add(passField, 1, 1);
        loginGrid.setAlignment(Pos.CENTER);

        // Login- und Register-Buttons in eine eigene HBox
        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

        HBox buttonBox = new HBox(20, loginButton, registerButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Komplette Login-Formular (Eingabefelder + Buttons)
        VBox loginLayout = new VBox(15, loginGrid, buttonBox);
        loginLayout.setAlignment(Pos.CENTER);

        // Kombinierte Layouts (Text oben, Login unten)
        VBox layout = new VBox(20, textContainer, loginLayout);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        // Rahmen und Hintergrund
        BorderStroke borderStroke = new BorderStroke(
                Color.web("#6c2a5a"),  // Rahmenfarbe
                BorderStrokeStyle.SOLID, // Rahmenstil
                new CornerRadii(5),      // Eckenradius
                new BorderWidths(20)     // Rahmenbreite
        );

        VBox wrappedLayout = new VBox(layout); // Layout in einen neuen Container einbinden
        wrappedLayout.setBorder(new Border(borderStroke));
        wrappedLayout.setBackground(new Background(
                new BackgroundFill(Color.web("#66a3a4"), new CornerRadii(15), null)
        ));
/*
        // Bildpfad (relativ zum Ressourcen-Ordner)
        String imagePath = "/images/1594205764210.png";

        // Hintergrundbild definieren
        BackgroundImage backgroundImage = new BackgroundImage(
                new Image(getClass().getResource(imagePath).toExternalForm()), // Bild aus Ressourcen laden
                BackgroundRepeat.NO_REPEAT,  // Keine Wiederholung horizontal
                BackgroundRepeat.NO_REPEAT,  // Keine Wiederholung vertikal
                BackgroundPosition.DEFAULT,  // Standard-Position (zentriert)
                BackgroundSize.DEFAULT       // Standard-Größe (entsprechend Bildgröße)
        );

        // Hintergrund auf das Layout setzen
        wrappedLayout.setBackground(new Background(backgroundImage));

*/


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
        /* alte Login Logik
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
*/
        // Login-Logik ausführen
        Runnable loginAction = () -> {
            String username = userField.getText().trim();
            String password = passField.getText().trim();

            if (validateLogin(username, password)) {
                String userDirectoryPath = "users/" + username;
                BookManagerApp app = new BookManagerApp();
                app.startWithUser(primaryStage, userDirectoryPath, username);
            } else {
                showAlert("Login Error", "Invalid username or password.");
            }
        };

        // Hinzufügen von Enter-Taste für die Felder
        userField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                passField.requestFocus(); // Fokus auf das Passwort-Feld legen
            }
        });

        passField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                loginAction.run(); // Login ausführen
            }
        });

        // Login-Button
        loginButton.setOnAction(e -> loginAction.run());

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
        Scene scene = new Scene(wrappedLayout, BookManagerApp.WINDOW_WIDTH, BookManagerApp.WINDOW_HEIGHT); // Breite x Höhe
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login-Screen");
        setBookIcon(primaryStage);
        primaryStage.show();
    }

    public static void setBookIcon(Stage stage) {
        Image bookIcon = new Image(LoginScreen.class.getResource("/icons/book.png").toExternalForm());
        stage.getIcons().add(bookIcon);
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
        Stage dialogueStage = (Stage) alert.getDialogPane().getScene().getWindow();
        setBookIcon(dialogueStage);
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