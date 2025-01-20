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

/*
 Die Klasse LoginScreen stellt einen grafischen Login- und  Registrierungsbildschirm für die Benutzerverwaltung
 bereit. Sie ermöglicht es Nutzern, sich mit einem bestehenden Konto einzuloggen oder ein neues Konto zu registrieren.

 Nutzerdaten (Benutzername und Passwort) werden in einer YAML-Datei ("users.yaml") gespeichert.
 Bei der Registrierung wird ein individuelles Benutzerverzeichnis sowie eine Standard-Collection-Datei
 für den Benutzer erstellt.

 Die GUI ist in klar strukturierte Layouts unterteilt und enthält Validierungsmechanismen.

 Die Klasse dient als Startpunkt der Anwendung und gibt bei einem erfolgreichen Login
 die Kontrolle an die Hauptanwendung (BookManagerApp) weiter.
 */

public class LoginScreen extends Application {

    private final String usersFilePath = "users.yaml"; // Dateipfad zur Speicherung von Nutzerdaten

    @Override
    public void start(Stage primaryStage) {
        // Erstellen der Benutzeroberfläche (UI) für den Anmeldebildschirm

        // Überschrift
        Label titleLabel = new Label("Alles In Ordnung");
        titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

        // Untertitel + Autor*innen
        Label infoLine1 = new Label("Projekt für Programmieren 1, WS 2024/25");
        Label infoLine2 = new Label("Autor*innen: Daniel Essl, Corinna Jäger, Jochen Schmidtberger und Christina Seidl");
        infoLine1.setStyle("-fx-font-size: 16px;");
        infoLine2.setStyle("-fx-font-size: 16px;");

        // Layout-Container für die Texte
        VBox textContainer = new VBox(10, titleLabel, infoLine1, infoLine2);
        textContainer.setAlignment(Pos.TOP_CENTER);

        // Labels und Eingabefelder für den Login
        Label userLabel = new Label("Username:");
        TextField userField = new TextField(); // Eingabefeld für den Benutzernamen

        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField(); // Eingabefeld für das Passwort (versteckt die Eingabe)

        // Layout für die Loginform (Grid mit 2x2 Feldern: Benutzername und Passwort)
        GridPane loginGrid = new GridPane();
        loginGrid.setHgap(10); // Horizontale Abstände zwischen den Elementen
        loginGrid.setVgap(10); // Vertikale Abstände zwischen den Elementen
        loginGrid.add(userLabel, 0, 0); // Label für Benutzername hinzugefügt
        loginGrid.add(userField, 1, 0); // Eingabefeld für Benutzername hinzugefügt
        loginGrid.add(passLabel, 0, 1); // Label für Passwort hinzugefügt
        loginGrid.add(passField, 1, 1); // Eingabefeld für Passwort hinzugefügt
        loginGrid.setAlignment(Pos.CENTER); // Zentrierung des GridPanes

        // Buttons für Login und Registrierung
        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

        // Container für die Buttons, mit Abstand zwischen den Buttons
        HBox buttonBox = new HBox(20, loginButton, registerButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Kombinieren der Eingabefelder und Buttons in einem vertikalen Layout
        VBox loginLayout = new VBox(15, loginGrid, buttonBox);
        loginLayout.setAlignment(Pos.CENTER);

        // Übergeordneter Container für Titel und Loginabschnitt
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

        // Wrapper für das Layout, der Rahmen und Hintergrund kombiniert
        VBox wrappedLayout = new VBox(layout); // Layout in einen neuen Container einbinden
        wrappedLayout.setBorder(new Border(borderStroke));
        wrappedLayout.setBackground(new Background(
                new BackgroundFill(Color.web("#66a3a4"), new CornerRadii(15), null) //Hintergrundfarbe
        ));

        // Funktion für die Login-Logik
        Runnable loginAction = () -> {
            String username = userField.getText().trim(); // Benutzername aus dem Textfeld abrufen
            String password = passField.getText().trim(); // Passwort aus dem Textfeld abrufen

            if (validateLogin(username, password)) { // Login überprüfen
                String userDirectoryPath = "users/" + username;
                BookManagerApp app = new BookManagerApp();
                app.startWithUser(primaryStage, userDirectoryPath, username); // Starten der Hauptanwendung
            } else {
                showAlert("Login Error", "Invalid username or password.");
            }
        };

        // Event für Enter-Taste im Benutzernamenfeld
        userField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                passField.requestFocus(); // Fokus auf das Passwort-Feld legen
            }
        });

        // Event für Enter-Taste im Passwortfeld
        passField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                loginAction.run(); // Login ausführen
            }
        });

        // Login-Button Event
        loginButton.setOnAction(e -> loginAction.run()); // Login-Logik ausführen

        // Registrierung-Button Event
        registerButton.setOnAction(e -> {
            String username = userField.getText().trim();
            String password = passField.getText().trim();

            if (registerUser(username, password)) { // Benutzer registrieren
                showInfo("Registration Successful", "You can now log in with your credentials.");
            } else {
                showAlert("Registration Error", "Username already exists or invalid input.");
            }
        });

        // Szene erstellen und die Szene auf die Stage setzen
        Scene scene = new Scene(wrappedLayout, BookManagerApp.WINDOW_WIDTH, BookManagerApp.WINDOW_HEIGHT); // Breite x Höhe
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login-Screen");
        setBookIcon(primaryStage);
        primaryStage.show();
    }

    // Setzt ein Buch-Icon für das Fenster
    public static void setBookIcon(Stage stage) {
        Image bookIcon = new Image(LoginScreen.class.getResource("/icons/book.png").toExternalForm());
        stage.getIcons().add(bookIcon);
    }

    /*
    Methode zur Überprüfung, ob die Login-Daten gültig sind
    Die Methode verwendet die SnakeYAML-Bibliothek, um die Benutzerdaten aus der Datei einzulesen.
    Falls die Datei erfolgreich eingelesen wird, wird ein Mapping von Benutzernamen und Passwörtern
    in ein Map-Objekt geladen. Es wird geprüft, ob der eingegebene Benutzername existiert und das
    angegebene Passwort mit dem gespeicherten übereinstimmt.

    Zusätzlich wird verifiziert, ob das Benutzerverzeichnis (`users/<username>`) existiert und
    ein gültiges Verzeichnis ist. Nur wenn beide Bedingungen erfüllt sind, wird `true` zurückgegeben
    und der Login ist erfolgreich.
    */

    private boolean validateLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) return false; // Leere Felder prüfen

        File file = new File(usersFilePath); // Datei mit Benutzerdaten öffnen
        if (!file.exists()) return false; // Datei existiert nicht

        Yaml yaml = new Yaml(); // Yaml-Objekt für SnakeYAML erstellen
        try (FileReader reader = new FileReader(file)) {
            Map<String, String> users = yaml.load(reader); // Benutzer und Passwörter aus Datei laden
            if (users != null && password.equals(users.get(username))) { // Prüfung des Passworts

                File userDir = new File("users/" + username); // Benutzerverzeichnis prüfen
                if (userDir.exists() && userDir.isDirectory()) {
                    return true; //Login erfolgreich
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false; //Login fehlgeschlagen
    }

    /*
    Methode zur Registrierung eines neuen Benutzers.
    Die Methode prüft, ob die Datei (users.yaml), die die Benutzerdaten speichert,
    bereits existiert. Falls die Datei vorhanden ist, werden die existierenden
    Benutzerdaten mithilfe der SnakeYAML-Bibliothek ausgelesen und in ein `Map<String, String>`-Objekt
    geladen. Es wird sichergestellt, dass der Benutzername nicht schon existiert.
    Falls der Benutzername vorhanden ist, wird `false` zurückgegeben.

    Anschließend wird der neue Benutzername und das Passwort zu der `Map` hinzugefügt
    und in die Datei zurückgeschrieben. Die Änderung wird mit einem `FileWriter`
    persistiert, wobei der `try-with-resources`-Block den Writer automatisch
    schließt.

    Abschließend wird ein Verzeichnis und eine Standard Buchsammlung für den Benutzer automatisch erstellt.

    */
    private boolean registerUser(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) return false; // Prüfung auf leere Felder

        File file = new File(usersFilePath); // Pfad zur Benutzerdatendatei
        Map<String, String> users = new HashMap<>(); // Map zum Speichern der Daten

        // Laden bestehender Benutzerdaten
        Yaml yaml = new Yaml(); // Yaml-Objekt für SnakeYAML erstellen
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                users = yaml.load(reader); // Benutzerdaten aus der YAML-Datei laden
                if (users == null) users = new HashMap<>(); // Leere Map initialisieren, falls Datei leer ist
                if (users.containsKey(username)) return false; // Benutzer existiert bereits
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        // Hinzufügen neuer Benutzerdaten (Benutzername und Passwort)
        users.put(username, password);
        try (FileWriter writer = new FileWriter(file)) {
            yaml.dump(users, writer); // Neue Liste der Benutzerdaten in die YAML-Datei schreiben
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // Erstellen des Benutzerverzeichnisses
        String userDirectoryPath = "users/" + username;
        File userDirectory = new File(userDirectoryPath);
        if (!userDirectory.exists()) {
            if (!userDirectory.mkdirs()) { // Verzeichnis erstellen, falls nicht vorhanden
                System.err.println("Failed to create user directory: " + userDirectoryPath);
                return false;
            }
        }

        // Erstellen einer Standard-Collection für den Benutzer
        String defaultCollectionPath = userDirectoryPath + "/default.yaml";
        CollectionManager collectionManager = new CollectionManager();
        collectionManager.addNewCollection("default", userDirectoryPath);

        System.out.println("User registered successfully with directory: " + userDirectoryPath);
        return true;
    }

    // Hilfsmethoden für Dialoge
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR); // Fehlerdialog erstellen
        Stage dialogueStage = (Stage) alert.getDialogPane().getScene().getWindow();
        setBookIcon(dialogueStage);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait(); // Dialog anzeigen
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); // Infodialog erstellen
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait(); // Dialog anzeigen
    }

    // Hauptmethode zum Starten der Anwendung
    public static void main(String[] args) {
        launch(args);
    }
}