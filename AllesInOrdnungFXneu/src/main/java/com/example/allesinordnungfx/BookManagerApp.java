package com.example.allesinordnungfx;

import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.LongStringConverter;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.time.Year;
import java.io.File;
import java.util.List;
import java.util.Optional;

public class BookManagerApp extends Application {

    @Override
    public void start(Stage primaryStage) {

        startWithUser(primaryStage, System.getProperty("user.dir"), "DefaultUser");
    }

    // Konstanten für die Fenstergröße
    public static final double WINDOW_WIDTH = 1000;
    public static final double WINDOW_HEIGHT = 600;

    // Instanz eines CollectionManagers (Verwaltung Sammlungen wie Bücher)
    private CollectionManager collectionManager = new CollectionManager();

    // Liste für die Anzeige der Bücher in einer Tabelle
    private final ObservableList<Book> bookListData = FXCollections.observableArrayList();

    private String collectionsFilePath = "collections.yaml"; // Pfad für Datei zur Speicherung von Sammlungen
    private final TableView<Book> bookTableView = new TableView<>(); // Tabelle zur Anzeige der Bücher
    private Collection currentCollection; // Aktuell ausgewählte Sammlung
    private ComboBox<String> collectionComboBox; // Klassenvariable für die ComboBox
    private ObservableList<String> collectionsObservableList; // ObservableList für Collections

    // Startet die JavaFX-Anwendung
    public static void main(String[] args) {
        launch(args);
    }

    // Startpunkt der Applikation mit einem vorgegebenen Benutzer und Verzeichnis
    // BookManagerApp ist auch als Stand-Alone konzipiert und kann ohne LoginScreen gestartet werden.
    public void startWithUser(Stage primaryStage, String userDirectoryPath, String username) {
        this.collectionManager = new CollectionManager(userDirectoryPath);
        this.collectionsFilePath = userDirectoryPath + "/collections.yaml"; // Angepasst für Benutzer

        collectionManager.loadCollectionNames("collections.yaml"); // Collection-Namen aus der Datei laden

        start(primaryStage, username); // Hauptfenster starten
    }

    // Initialisiert das Hauptfenster, die Benutzeroberfläche und die notwendigen Daten.
    public void start(Stage primaryStage, String username) {

        ensureCollectionsDirectoryExists(); // Überprüfung - Erstelt bei Bedarf ein neues Verzeichnis

        // Vorhandene Collection-Namen laden
        collectionManager.loadCollectionNames(collectionsFilePath);

        // Initialisiere das ObservableList mit den Collection-Namen
        collectionsObservableList = FXCollections.observableArrayList(collectionManager.getCollectionNames());

        // Wenn keine Collections vorhanden sind, eine Standard-Collection erstellen
        if (collectionsObservableList.isEmpty()) {
            String defaultName = "New Collection";
            collectionsObservableList.add(defaultName);
            collectionManager.addNewCollection(defaultName); // Erstellt und speichert eine Default-Collection
        }

        // Verbindung zwischen Collection und UI-Benachrichtigung herstellen
        if (currentCollection != null) { // Sicherstellen, dass currentCollection gesetzt ist
            // Zeigt einen Hinweis bei Duplikaten
            currentCollection.setNotificationCallback(message -> showAlert("Duplicate Book", message));
        } else {
            System.err.println("Error: currentCollection is null");
        }

        // Label zur Anzeige des eingeloggten Benutzers
        Label loggedInUserLabel = new Label("Logged in as: " + username); // "Benutzername" durch den echten Usernamen ersetzen
        loggedInUserLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;"); // Beispielstil

        // Container für die Anzeige des eingeloggten Benutzers (HBox für horizontale Anordnung)
        HBox userDisplayBox = new HBox(loggedInUserLabel);
        userDisplayBox.setStyle("-fx-padding: 5; -fx-background-color: #66a3a4; -fx-alignment: center-left;");
        userDisplayBox.setPadding(new Insets(5, 10, 5, 10));

        // Initialisierung einer ComboBox für die Auswahl der Sammlungen
        collectionComboBox = new ComboBox<>(collectionsObservableList);
        collectionComboBox.setPrefWidth(150);
        collectionComboBox.setPromptText("Select Collection");

        // Setzt die Auswahl auf die erste Collection (Standard)
        if (!collectionsObservableList.isEmpty()) {
            String firstCollection = collectionsObservableList.getFirst(); // Nimm die erste Collection
            collectionComboBox.getSelectionModel().select(firstCollection); // Setze die Auswahl in der ComboBox
            currentCollection = collectionManager.loadBooksForCollection(firstCollection); // Lade Bücher der ersten Collection
            bookListData.setAll(currentCollection.getBooks()); // Aktualisiere die Buch-Liste (Tabelle)
        }

        // Listener: Reagiert auf Änderungen in der Auswahlliste (ComboBox)
        collectionComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentCollection = collectionManager.loadBooksForCollection(newVal); // Lade Bücher der neuen Collection
                bookListData.setAll(currentCollection.getBooks()); // Zeige die Bücher der neuen Collection in der Tabelle
                System.out.println("Collection gewechselt zu: " + newVal);
            }
        });

        // Button zum Hinzufügen einer neuen Collection
        Button addCollectionButton = new Button("Add Collection");
        addCollectionButton.setOnAction(e -> addNewCollection());

        // Button zum Umbenennen einer Collection
        Button renameCollectionButton = new Button("Rename Collection");
        renameCollectionButton.setOnAction(e -> renameSelectedCollection());

        // Button zum Löschen einer Collection
        Button deleteCollectionButton = new Button("Delete Collection");
        deleteCollectionButton.setOnAction(e -> deleteSelectedCollection());

        // Logout-Button
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            LoginScreen loginScreen = new LoginScreen(); // Erstellt eine neue LoginScreen-Instanz
            try {
                loginScreen.start(primaryStage); // Wechsel zum Login-Screen
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Suchfeld für die Bücher-Ansicht
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");

        // Such-Button
        Button searchButton = new Button("🔍");

        // Reset-Button (Suchfeld löschen und Ansicht zurücksetzen)
        Button resetButton = new Button();
        try {
            Image resetImage = new Image(getClass().getResourceAsStream("/icons/reset.png"));
            ImageView resetImageView = new ImageView(resetImage);
            resetImageView.setFitWidth(12);
            resetImageView.setFitHeight(12);
            resetButton.setGraphic(resetImageView);
        } catch (NullPointerException e) {
            // Falls /icons/reset.png nicht gefunden wird
            resetButton.setText("Reset");
        }

        // Aktion: Suchfeld leeren + wieder alle Bücher anzeigen
        resetButton.setOnAction(e -> {
            searchField.clear(); // Suchfeld löschen
            loadBooksForCurrentCollection(); // Bücherliste der aktuellen Sammlung laden
        });

        // Import Button
        Button importButton = new Button("Import...");
        importButton.setDisable(true); // deaktiviert

        // Add-Book Button
        Button addButton = new Button("Add Book");

        // Refresh Button
        Button refreshButton = new Button("Refresh");
        refreshButton.setDisable(true); // deaktiviert
        refreshButton.setOnAction(e -> {
            loadBooksForCurrentCollection(); // Bücherliste der aktuellen Sammlung laden
            bookTableView.refresh(); // Optional, falls die Tabelle nicht automatisch aktualisiert wird
        });

        // Export-Button mit FileChooser
        Button exportButton = new Button("Export...");

        // Import-Button: Öffnet eine Dialog zum Importieren von Dateien - aktuell deaktiviert
        importButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Import Books");

            // Filter für erlaubte Dateitypen (z.B. YAML oder Excel)
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("YAML Files (*.yaml, *.yml)", "*.yaml", "*.yml"),
                    new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx")
            );

            // Datei auswählen
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                String filePath = file.getAbsolutePath();
                String fileName = file.getName().toLowerCase();

                // Verarbeitet den Dateipfad basierend auf seiner Dateiendung
                boolean success = false;
                if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
                    // Import von YAML
                    success = collectionManager.importFromYaml(filePath, currentCollection.getName());
                } else if (fileName.endsWith(".xlsx")) {
                    // Import von XLSX
                    success = collectionManager.importFromXlsx(filePath, currentCollection.getName());
                }

                if (success) {
                    loadBooksForCurrentCollection(); // GUI aktualisieren
                    showInfo("Import Erfolgreich", "Die Bücher wurden erfolgreich importiert!");
                } else {
                    showAlert("Import Fehler", "Der Import ist fehlgeschlagen.");
                }
            }
        });

        // Export-Button: Öffnet einen Dialog zum Exportieren von Sammlungen und Büchern
        exportButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Books");

            // Standardname für Exportdatei (basierend auf dem aktuellen Sammlungstitel)
            fileChooser.setInitialFileName(currentCollection.getName() + ".xlsx");

            // Filter: Export als XLSX oder YAML wählen
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx"),
                    new FileChooser.ExtensionFilter("YAML Files (*.yaml)", "*.yaml")
            );

            // Speicher-Pfad vom Nutzer wählen lassen
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                String filePath = file.getAbsolutePath();

                // Unterscheidet basierend auf Dateiendung zwischen XLSX und YAML
                boolean success = false;
                if (filePath.endsWith(".xlsx")) {
                    // Export nach XLSX
                    success = collectionManager.exportToXlsx(filePath);
                } else if (filePath.endsWith(".yaml")) {
                    // Export nach YAML
                    success = collectionManager.exportToYaml(filePath);
                }

                if (success) {
                    showInfo("Export Erfolgreich", "Die Sammlung wurde erfolgreich exportiert.");
                } else {
                    showAlert("Export Fehler", "Der Export ist fehlgeschlagen.");
                }
            }
        });

        // Suchfunktion - Sucht in der Buchliste basierend auf dem Text im Suchfeld
        searchButton.setOnAction(ev -> searchBooks(searchField));
        searchField.setOnKeyPressed(ev -> { // ENTER-Taste im Suchfeld: Startet die Suche
            if (ev.getCode() == KeyCode.ENTER) {
                searchBooks(searchField); // Ruft die Methode `searchBooks` auf
            }
        });

        // Neues Buch hinzufügen
        addButton.setOnAction(ev -> openAddBookWindow()); // Öffnet Dialogfenster

        // Spacer für die erste Zeile (macht Layout flexibler)
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, javafx.scene.layout.Priority.ALWAYS);

        // HBox: Legt die Layoutstruktur für die erste Zeile fest (Sammlungs-Auswahl und Buttons)
        HBox collectionBox = new HBox(10, collectionComboBox, addCollectionButton, renameCollectionButton, deleteCollectionButton, logoutButton, spacer1, importButton);
        collectionBox.setPadding(new Insets(5)); // Innenabstände setzen

        // Spacer für die zweite Zeile
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, javafx.scene.layout.Priority.ALWAYS);

        // HBox: Layout für die zweite Zeile (Suchfeld, Buttons, Exportfunktion)
        HBox actionBox = new HBox(10, searchField, searchButton, resetButton, addButton, refreshButton, spacer2, exportButton);
        actionBox.setPadding(new Insets(5)); // Innenabstände setzen

        // VBox: Enthält Benutzerinfo, CollectionBox und ActionBox (vertikale Anordnung)
        VBox topContainer = new VBox(10, userDisplayBox, collectionBox, actionBox);
        topContainer.setPadding(new Insets(10)); // Padding für die gesamte VBox setzen

        // TableView: Tabelle zur Anzeige und Bearbeitung der Bücher
        TableView<Book> bookTableView = new TableView<>();
        bookTableView.setEditable(true); // Tabelle als bearbeitbar setzen
        Label placeholderLabel = new Label("No content in table");
        bookTableView.setPlaceholder(placeholderLabel); // Platzhaltertext, wenn keine Inhalte vorhanden sind

        // ContextMenu: Kontextmenü für Tabellenzeilen (Rechtsklick)
        bookTableView.setRowFactory(tv -> {
            TableRow<Book> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            // Menüpunkt: "Show" (Details eines Buches anzeigen)
            MenuItem showItem = new MenuItem("Show");
            showItem.setOnAction(e -> { // Holt das Buch aus der Zeile
                Book rowData = row.getItem();
                if (rowData != null) {
                    openShowWindow(rowData); // Öffnet Detailfenster
                }
            });

            // Menüpunkt: "Edit" (Buch bearbeiten)
            MenuItem editItem = new MenuItem("Edit");
            editItem.setOnAction(e -> {
                Book rowData = row.getItem(); // Holt das Buch aus der Zeile
                if (rowData != null) {
                    openEditWindow(rowData); // Öffnet Bearbeitungsfenster
                }
            });

            // Menüpunkt: "Delete" (Buch löschen)
            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(e -> {
                Book rowData = row.getItem(); // Holt das Buch aus der Zeile
                if (rowData != null) {
                    currentCollection.removeBook(rowData.getTitle()); // Entfernt Buch aus der Sammlung
                    bookListData.remove(rowData); // Entfernt Buch aus der Tabellenansicht
                    // Sofort in YAML speichern
                    collectionManager.saveBooksForCollection(currentCollection);
                }
            });


            // Hinzufügen der Menüeinträge zum Kontextmenü
            contextMenu.getItems().addAll(showItem, editItem, deleteItem);

            // Kontextmenü nur anzeigen, wenn Zeile nicht leer ist
            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu)null)
                            .otherwise(contextMenu)
            );
            return row;
        });

        // -- Tabellen-Spalten definieren --

        // Spalte: Titel
        TableColumn<Book, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(cd -> // Zugriff auf den Titel des Buches
                new SimpleStringProperty(cd.getValue().getTitle()));
        titleColumn.setCellFactory(TextFieldTableCell.forTableColumn()); // Setzt Editierbarkeit
        titleColumn.setOnEditCommit(event -> { // Listener für Änderungen am Titel
            Book book = event.getRowValue();
            book.setTitle(event.getNewValue()); // Aktualisiert den Titel
            collectionManager.saveBooksForCollection(currentCollection); // Speichert Änderungen
        });
        titleColumn.setEditable(true); // Spalte editierbar setzen
        titleColumn.setPrefWidth(190); // Breite der Spalte

        // Spalte: Autor
        TableColumn<Book, String> authorColumn = new TableColumn<>("Author");
        authorColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(
                        cd.getValue().getFirstName() + " " + cd.getValue().getLastName()
                )); // Verknüpft Vor- und Nachnamen
        authorColumn.setCellFactory(TextFieldTableCell.forTableColumn()); // Setzt Editierbarkeit
        authorColumn.setOnEditCommit(event -> { // Listener für Änderungen am Autorennamen
            Book book = event.getRowValue();
            String newValue = event.getNewValue();

            // Aufteilen des eingegebenen Namens in Vor- und Nachnamen
            String[] parts = newValue.split("\\s+", 2);
            if (parts.length == 2) {
                book.setFirstName(parts[0]);
                book.setLastName(parts[1]);
            } else {
                book.setFirstName(parts[0]);
                book.setLastName("");
            }
            collectionManager.saveBooksForCollection(currentCollection); // Speichert Änderungen
        });
        authorColumn.setEditable(true);
        authorColumn.setPrefWidth(190);

        // Spalte: Genre - gleiche Funktionen wie Titel und Autor
        TableColumn<Book, String> genreColumn = new TableColumn<>("Genre");
        genreColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getGenre()));
        genreColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        genreColumn.setOnEditCommit(event -> {
            Book book = event.getRowValue();
            book.setGenre(event.getNewValue());
            collectionManager.saveBooksForCollection(currentCollection);
        });
        genreColumn.setEditable(true);
        genreColumn.setPrefWidth(70);

        // Spalte: Erscheinungsjahr
        TableColumn<Book, Integer> yearColumn = new TableColumn<>("Publication Year");
        yearColumn.setCellValueFactory(cd ->
                new SimpleIntegerProperty(cd.getValue().getPublicationYear()).asObject());
        // Textfeld-Konverter
        yearColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        yearColumn.setPrefWidth(70);

        yearColumn.setOnEditCommit(event -> {
            Book book = event.getRowValue();
            int newYear = event.getNewValue();

            // Validierung der Jahreszahl (falls Tabelle direkt bearbeitet wird)
            if (isValidYear(String.valueOf(newYear))) {
                book.setPublicationYear(newYear);
                collectionManager.saveBooksForCollection(currentCollection); // Speichern
            } else {
                showAlert("Invalid Year", "Please fill in a valid year!");
                loadBooksForCurrentCollection(); // Zurücksetzen, wenn ungültig
            }
        });

        // Spalte ISBN-Code
        TableColumn<Book, Long> isbnColumn = new TableColumn<>("ISBN");
        isbnColumn.setCellValueFactory(cd ->
                new SimpleLongProperty(cd.getValue().getIsbn()).asObject());
        isbnColumn.setCellFactory(TextFieldTableCell.forTableColumn(new LongStringConverter()));
        isbnColumn.setOnEditCommit(event -> {
            Book book = event.getRowValue();
            book.setIsbn(event.getNewValue());
            collectionManager.saveBooksForCollection(currentCollection);
        });
        isbnColumn.setEditable(true);
        isbnColumn.setPrefWidth(70);

        // Spalte "Read" (Lesestatus)
        TableColumn<Book, Boolean> readColumn = new TableColumn<>("Read");
        readColumn.setCellValueFactory(cd ->
                new SimpleBooleanProperty(cd.getValue().isRead()));
        // Stellt Checkboxen in der Spalte dar
        readColumn.setCellFactory(CheckBoxTableCell.forTableColumn(readColumn));
        readColumn.setEditable(true);
        readColumn.setMaxWidth(40);
        // Listener: Speichert Änderungen am Lesestatus
        readColumn.setOnEditCommit(event -> {
            Book book = event.getRowValue();
            book.setRead(event.getNewValue());
            collectionManager.saveBooksForCollection(currentCollection);
        });

        // Spalte für Bewertungen
        TableColumn<Book, String> ratingColumn = new TableColumn<>("Rating");
        ratingColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getRating()));

        // Setzt eine benutzerdefinierte CellFactory
        ratingColumn.setCellFactory(column -> new TableCell<>() {
            // Dropdown-Menü (ComboBox) für Bewertungen (1, 2, 3)
            private final ComboBox<String> comboBox = new ComboBox<>(
                    FXCollections.observableArrayList("1", "2", "3")
            );

            // Tooltip, wenn Bewertung nicht verfügbar ist
            private final Tooltip disabledTooltip = new Tooltip("Mark as read to enable rating");

            @Override
            protected void updateItem(String rating, boolean empty) {
                super.updateItem(rating, empty);

                // Wenn die Zelle leer ist oder keine Daten vorhanden sind
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                // Zugriff auf das zugehörige Buch
                Book book = (Book) getTableRow().getItem();

                if (!book.isRead()) {
                    // ComboBox deaktivieren und Tooltip hinzufügen
                    comboBox.setDisable(true);
                    Tooltip.install(comboBox, disabledTooltip); // Tooltip für Info
                } else {
                    // ComboBox aktivieren und Tooltip entfernen
                    comboBox.setDisable(false);
                    Tooltip.uninstall(comboBox, disabledTooltip);
                    comboBox.setValue(rating); // Setze aktuelles Rating
                }

                // Listener: Bei Änderung den Wert im Buch speichern
                comboBox.setOnAction(event -> {
                    String newValue = comboBox.getValue();
                    book.setRating(newValue); // Neues Rating speichern
                    collectionManager.saveBooksForCollection(currentCollection); // Sammlung speichern
                });

                setGraphic(comboBox);
            }
        });

        // Spalte für Kommentare
        TableColumn<Book, String> commentColumn = new TableColumn<>("Comment");
        commentColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getComment()));
        commentColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        commentColumn.setOnEditCommit(event -> {
            Book book = event.getRowValue();
            book.setComment(event.getNewValue());
            collectionManager.saveBooksForCollection(currentCollection);
        });
        commentColumn.setEditable(true);
        commentColumn.setMaxWidth(180);

        // Edit-Button (in Zelle)
        TableColumn<Book, Void> editColumn = new TableColumn<>("Edit");
        editColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button();
            {
                // Versuche Bild "edit.png" zu laden
                try {
                    Image editImage = new Image(getClass().getResourceAsStream("/icons/edit.png"));
                    ImageView iv = new ImageView(editImage);
                    iv.setFitWidth(16);
                    iv.setFitHeight(16);
                    editButton.setGraphic(iv);
                } catch (NullPointerException e) {
                    editButton.setText("Edit");
                }
                // Aktion: Öffnet ein Bearbeitungsfenster für das Buch
                editButton.setOnAction(evt -> {
                    // Holt das Buch der aktuellen Zeile
                    Book book = getTableView().getItems().get(getIndex());
                    openEditWindow(book); // Öffnet das Bearbeitungsfenster für das Buch
                });
            }

            // Aktualisiert die Darstellung der Zelle, basierend darauf, ob sie leer ist
            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editButton); // Zeigt den Edit-Button nur an, wenn die Zelle nicht leer ist
                setAlignment(Pos.CENTER); // Zentriert den Button in der Zelle
            }
        });

        // Delete-Button (in Zelle)
        // Erstellt die CellFactory, um in jeder Zeile einen Delete-Button hinzuzufügen
        TableColumn<Book, Void> deleteColumn = new TableColumn<>("Delete");
        deleteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button();
            {
                // Versuche Bild "delete.png" zu laden
                try {
                    Image deleteImage = new Image(getClass().getResourceAsStream("/icons/delete.png"));
                    ImageView iv = new ImageView(deleteImage);
                    iv.setFitWidth(16);
                    iv.setFitHeight(16);
                    deleteButton.setGraphic(iv);
                } catch (NullPointerException e) {
                    deleteButton.setText("Del");
                }
                // Aktion für Klick auf den Button
                deleteButton.setOnAction(evt -> {
                    Book book = getTableView().getItems().get(getIndex()); // Holt das Buch aus der aktuellen Zeile
                    currentCollection.removeBook(book.getTitle()); // Entfernt das Buch aus der aktuellen Collection
                    bookListData.remove(book); // Entfernt das Buch aus der Ansicht (Daten der TableView)
                    collectionManager.saveBooksForCollection(currentCollection); // Speichert die aktualisierte Collection
                });
            }
            // Aktualisiert die Darstellung der Zelle
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);  // Zeigt den Delete-Button nur, wenn die Zelle nicht leer ist
                setAlignment(Pos.CENTER);
            }
        });

        // Spalten zur Tabelle hinzufügen
        bookTableView.getColumns().addAll(
                titleColumn, authorColumn, genreColumn, yearColumn, isbnColumn,
                readColumn, ratingColumn, commentColumn, editColumn, deleteColumn
        );

        // Verhindert das Sortieren der Edit- und Delete-Spalten
        editColumn.setSortable(false);
        deleteColumn.setSortable(false);

        // Listener für die Auswahl der aktuellen Collection
        collectionComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) { // Überprüft, ob eine neue Collection ausgewählt wurde
                currentCollection = collectionManager.loadBooksForCollection(newVal); // Lädt die Bücher der neuen Collection
                bookListData.setAll(currentCollection.getBooks()); // Aktualisiert die Buchliste in der Tabelle
                System.out.println("Current Collection set to: " + currentCollection.getName()); // Gibt die neue Collection im Log aus
            }
        });

        // Initialisierung der aktuellen Collection durch Auswahl der ersten Collection
        collectionComboBox.getSelectionModel().selectFirst();

        // Daten an die Tabelle binden
        bookTableView.setItems(bookListData); // Verknüpft die ObservableList mit der Tabelle
        bookListData.setAll(currentCollection.getBooks()); // Lädt die Bücher der aktuellen Collection in die Anzeige

        // Initialisiert die Hauptszene
        Scene scene = new Scene(new VBox(20, topContainer, bookTableView), WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setScene(scene); // Setzt die Szene des Fensters
        primaryStage.setTitle("Book Collection Manager - Alles in Ordnung"); // Setzt den Titel des Fensters
        LoginScreen.setBookIcon(primaryStage);

        primaryStage.show(); // Zeigt das Fenster an

        // Beim Schließen -> Speichern aller Collections
        primaryStage.setOnCloseRequest(ev -> {
            // Speichern der Collection-Namen
            collectionManager.saveCollectionNames(collectionsFilePath);
            // Speichern aller Collections
            for (String name : collectionManager.getCollectionNames()) {
                Collection collection = collectionManager.loadBooksForCollection(name);
                collectionManager.saveBooksForCollection(collection);
            }
        });
        primaryStage.setOnCloseRequest(event -> {
            // Speichere die aktuelle Collection, wenn sie existiert
            if (currentCollection != null) {
                collectionManager.saveBooksForCollection(currentCollection); // Speichert die Bücher in der aktuellen Collection
            }

            // Speichere auch die Liste aller Collection-Namen
            collectionManager.saveCollectionNames(collectionsFilePath); // Speichert die Collection-Namen in der YAML-Datei
            System.out.println("Collection were saved while shutting down.");
        });
    }

    //Überprüft ob die Eingabe der Jahreszahl valide ist
    private boolean isValidYear(String yearString) {
        //if (year.isEmpty()) return false;
        try {
            int intYear = Integer.parseInt(yearString); // Konvertiert den String in eine Ganzzahl
            int currentYear = Year.now().getValue(); // Aktuelles Jahr ermitteln
            return intYear >= 1000 && intYear <= currentYear; // Sicherstellen, dass das Jahr zwischen 1000 und dem aktuellen Jahr liegt
        } catch (NumberFormatException e) {
            return false; // Keine gültige Zahl
        }
    }

    /**
     * Fügt eine neue Collection hinzu.
     */
    private void addNewCollection() {
        // Erzeugt ein Dialogfenster für die Eingabe des Collection-Namens
        TextInputDialog dialog = new TextInputDialog();
        Image bookIcon = new Image(getClass().getResource("/icons/book.png").toExternalForm());
        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(bookIcon);
        dialog.setTitle("Add New Collection");
        dialog.setHeaderText(null); // Kein Headertext
        dialog.setContentText("Collection Name:"); // Hinweistext für den Benutzer

        // Zeigt den Dialog an und wartet auf die Benutzereingabe
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> { // Falls ein Name eingegeben wurde
            String trimmedName = name.trim(); // Entfernt unnötige Leerzeichen
            // Überprüft, ob der Name gültig ist und keine ungültigen Zeichen enthält
            if (!trimmedName.isEmpty() && isValidFileName(trimmedName)) {
                collectionManager.addNewCollection(trimmedName); // Methode im CollectionManager aufrufen
                collectionsObservableList.add(trimmedName); // ObservableList aktualisieren
                collectionComboBox.getSelectionModel().select(trimmedName);
                showInfo("Collection Added", "Collection '" + trimmedName + "' was added successfully.");
            } else {
                showAlert("Invalid Name", "Collection name cannot be empty or contains invalid characters.");
            }
        });
    }

    /**
     * Handler zum Umbenennen der aktuell ausgewählten Collection.
     */
    private void renameSelectedCollection() {
        // Holt den aktuell ausgewählten Collection-Namen aus der ComboBox
        String selectedCollection = collectionComboBox.getSelectionModel().getSelectedItem();
        if (selectedCollection == null) {
            showAlert("No Collection Selected", "Please select a collection first.");
            return;
        }

        // Erstellt einen Dialog für die Eingabe des neuen Namens, mit dem aktuellen Namen als Platzhalter
        TextInputDialog dialog = new TextInputDialog(selectedCollection);
        Image bookIcon = new Image(getClass().getResource("/icons/book.png").toExternalForm());
        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(bookIcon);

        // Konfiguriert den Dialog
        dialog.setTitle("Rename Collection");
        dialog.setHeaderText(null);
        dialog.setContentText("New Collection Name:");

        // Zeigt den Dialog an und wartet auf das Ergebnis
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> { // Falls ein neuer Name eingegeben wurde
            String trimmedName = newName.trim(); // Leerzeichen entfernen

            // Überprüft, ob der Name gültig ist
            if (trimmedName.isEmpty() || !isValidFileName(trimmedName)) {
                showAlert("Invalid Name", "Invalid or empty name.");
                return;
            }

            // Methode zum Umbenennen der Collection aufrufen
            boolean success = collectionManager.renameSelectedCollection(selectedCollection, trimmedName); // Neue Methode aufrufen
            if (success) { // Wenn das Umbenennen erfolgreich war
                // Aktualisiert die ObservableList
                collectionsObservableList.set(collectionsObservableList.indexOf(selectedCollection), trimmedName);
                collectionComboBox.getSelectionModel().select(trimmedName); // Wählt die neue Collection aus
                showInfo("Renamed Successfully", "Collection '" + selectedCollection + "' was renamed to '" + trimmedName + "'.");
            } else {
                showAlert("Rename Failed", "Collection '" + selectedCollection + "' could not be renamed.");
            }
        });
    }

    /**
     * Handler zum Löschen der aktuell ausgewählten Collection.
     */
    private void deleteSelectedCollection() {
        // Holt die aktuell ausgewählte Collection
        String selectedCollection = collectionComboBox.getSelectionModel().getSelectedItem();
        if (selectedCollection == null) {
            showAlert("No Collection Selected", "Please select a collection first.");
            return;
        }

        // Erstellt ein Bestätigungsdialogfenster
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        Image bookIcon = new Image(getClass().getResource("/icons/book.png").toExternalForm());
        Stage confirmationStage = (Stage) confirmation.getDialogPane().getScene().getWindow();
        confirmationStage.getIcons().add(bookIcon);

        // Dialog-Text konfigurieren
        confirmation.setTitle("Delete Collection");
        confirmation.setContentText("Are you sure you want to delete the collection '" + selectedCollection + "'? This action cannot be undone.");

        // Zeigt den Dialog an und wartet auf die Antwort des Benutzers
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) { // Falls der Benutzer die Löschung bestätigt
            boolean success = collectionManager.deleteSelectedCollection(selectedCollection); // Neue Methode aufrufen
            if (success) {
                collectionsObservableList.remove(selectedCollection); // ObservableList aktualisieren
                showInfo("Deleted Successfully", "Collection '" + selectedCollection + "' was deleted successfully.");
                collectionComboBox.getSelectionModel().selectFirst(); // Wähle die erste Collection aus
            } else {
                showAlert("Delete Failed", "Collection '" + selectedCollection + "' could not be deleted.");
            }
        }
    }

    /**
     * Überprüft, ob ein String ein gültiger Dateiname ist.
     */
    private boolean isValidFileName(String name) {
        // Überprüft, ob der String nur alphanumerische Zeichen, Bindestriche, Unterstriche und Leerzeichen enthält
        return name.matches("^[a-zA-Z0-9-_ ]+$");
    }

    /**
     * Zeigt eine Fehlermeldung als Alert.
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Image bookIcon = new Image(getClass().getResource("/icons/book.png").toExternalForm());
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(bookIcon);
        alert.showAndWait();
    }

    /**
     * Zeigt eine Informationsmeldung als Alert.
     */
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Image bookIcon = new Image(getClass().getResource("/icons/book.png").toExternalForm());
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(bookIcon);
        alert.showAndWait();
    }

    /**
     * Stellt sicher, dass das Verzeichnis "collections" existiert.
     */

    private void ensureCollectionsDirectoryExists() {
        // Ruft die Methode im CollectionManager auf, um sicherzustellen, dass das Verzeichnis vorhanden ist
        collectionManager.ensureCollectionsDirectoryExists();
    }

    /**
     * Lädt die Bücher für die aktuell ausgewählte Collection.
     */
    private void loadBooksForCurrentCollection() {
        if (currentCollection == null) { // Überprüfung, ob eine Collection ausgewählt ist
            System.err.println("Error: currentCollection is null.");
            return;
        }

        // Bücher aus der aktuellen Sammlung laden
        List<Book> books = currentCollection.getBooks();

        // Tabellenansicht in der GUI löschen und aktualisieren
        bookTableView.getItems().clear();
        bookTableView.getItems().addAll(books);
    }

    /**
     * Sucht nach Büchern, die das Keyword enthalten (Titel, Autor, Jahr, ISBN).
     */
    private void searchBooks(TextField searchField) {
        // Holt das eingegebene Keyword aus dem Textfeld und wandelt es in Kleinbuchstaben um
        String keyword = searchField.getText().toLowerCase();
        bookListData.clear(); // Löscht die aktuelle Anzeige der Buchliste
        if (currentCollection != null) { // Überprüft, ob eine aktuelle Sammlung existiert
            // Führt die Suche in der Sammlung durch und fügt die Ergebnisse der Buchliste hinzu
            currentCollection.search(keyword).forEach(bookListData::add);
        }
    }

    /**
     * Öffnet ein Fenster, um ein neues Book anzulegen.
     */
    private void openAddBookWindow() {
        // Erstellt ein neues Buch mit leeren Attributen und öffnet das Formular im "Hinzufügen"-Modus (true = neues Buch)
        openBookForm(new Book("", "", "","", 0, 0), true);
    }

    /**
     * Öffnet ein Dialogfenster, um ein existierendes Book zu bearbeiten.
     */
    private void openEditWindow(Book book) {
        // Öffnet das Formular im "Bearbeiten"-Modus (false = existierendes Buch bearbeiten)
        openBookForm(book, false);
    }

    // Methode zur Steuerung, welches Element den Fokus bei "Enter" erhält
    private void setEnterKeyTraversal(TextField currentField, Control nextField) {
        // Event, das auf Enter-Tastenanschlag hört
        currentField.setOnKeyPressed(event -> { // Überprüft, ob die gedrückte Taste Enter ist
            if (event.getCode() == KeyCode.ENTER) {
                nextField.requestFocus(); // Nächstes Feld erhält den Fokus
            }
        });
    }

    /**
     * Öffnet ein Fenster zur Anzeige der Book-Details.
     * @param book Das Buch, das bearbeitet oder angezeigt werden soll.
     * @param isNew Gibt an, ob ein neues Buch hinzugefügt werden soll (true) oder ein bestehendes bearbeitet wird (false).
     */
    // Ein neues Fenster (Stage) initialisieren
    private void openBookForm(Book book, boolean isNew) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL); // Das Fenster ist modal (blockiert andere Fenster)
        stage.setTitle(isNew ? "Add Book" : "Edit Book"); // Setzt den Fenstertitel abhängig davon, ob es "Neu" oder "Bearbeiten" ist
        LoginScreen.setBookIcon(stage);

        // Konfiguriert die Fenstergröße und das Verhalten
        stage.setHeight(480);
        stage.setWidth(720);
        stage.setResizable(false); // Erlaubt keine Größenanpassung
        GridPane grid = new GridPane();
        grid.setHgap(5); // Horizontaler Abstand zwischen Spalten
        grid.setVgap(5); // Vertikaler Abstand zwischen Zeilen
        grid.setPadding(new Insets(5)); // Randabstand innerhalb des Grids

        // Felder mit ID für Debugging konfigurieren
        Label titleLabel = new Label("Title:");
        TextField titleField = new TextField(book.getTitle()); // Vorbelegung mit dem Titel des Buchs
        titleField.setId("titleField"); // ID setzen für Debugging

        // Eingabefeld für den Vorname des Autors inkl. Vorbelegung
        Label firstNameLabel = new Label("First Name:");
        TextField firstNameField = new TextField(book.getFirstName());
        firstNameField.setId("firstNameField");

        // Eingabefeld für den Nachname des Autors inkl. Vorbelegung
        Label lastNameLabel = new Label("Last Name:");
        TextField lastNameField = new TextField(book.getLastName());
        lastNameField.setId("lastNameField");

        // Eingabefeld für das Genre des Buchs inkl. Vorbelegung
        Label genreLabel = new Label("Genre:");
        TextField genreField = new TextField(book.getGenre());
        genreField.setId("genreField");

        // Eingabefeld für das Veröffentlichungsjahr des Buchs inkl. Vorbelegung
        Label yearLabel = new Label("Year:");
        TextField yearField = new TextField(String.valueOf(
                book.getPublicationYear() > 0 ? book.getPublicationYear() : Year.now().getValue()));
        yearField.setId("yearField");

        // Formatter für numerische Eingabe (nur Zahlen erlauben)
        TextFormatter<Integer> yearFormatter = new TextFormatter<>(
                new IntegerStringConverter(),
                book.getPublicationYear() > 0 ? book.getPublicationYear() : Year.now().getValue(), // Standardwert setzen
                change -> change.getControlNewText().matches("\\d*") ? change : null // Nur Ziffern erlauben
        );
        yearField.setTextFormatter(yearFormatter);

        // Eingabefeld für die ISBN des Buchs inkl. Vorbelegung
        Label isbnLabel = new Label("ISBN:");
        TextField isbnField = new TextField(String.valueOf(book.getIsbn()));
        isbnField.setId("isbnField");

        // CheckBox für "Gelesen" (bereits gelesen oder nicht) inkl. Vorbelegung
        Label readLabel = new Label("Read:");
        CheckBox readCheckBox = new CheckBox();
        readCheckBox.setSelected(book.isRead());

        // Dropdown für die Bewertung des Buchs
        Label ratingLabel = new Label("Rating:");
        ComboBox<String> ratingComboBox = new ComboBox<>();
        ratingComboBox.getItems().addAll("1", "2", "3"); // Fügt Bewertungsauswahl hinzu
        // Vorbelegung mit aktueller Bewertung (falls vorhanden)
        ratingComboBox.setValue(book.getRating() == null ? "" : book.getRating());
        ratingComboBox.setId("ratingComboBox"); // ID setzen für Debugging

        // Textarea für Kommentare zum Buch
        Label commentLabel = new Label("Comment:");
        TextArea commentArea = new TextArea(book.getComment());
        commentArea.setId("commentArea");

        // Save-Button mit Aktion
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            try { // Liest Eingabedaten aus den Feldern und schneidet unnötige Leerzeichen ab
                String title = titleField.getText().trim();
                String firstName = firstNameField.getText().trim();
                String lastName = lastNameField.getText().trim();
                String genre = genreField.getText().trim();
                int year = Integer.parseInt(yearField.getText().trim());
                long isbn = Long.parseLong(isbnField.getText().trim());

                // Validierung der Pflichtfelder
                if (title.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                    showAlert("Invalid Input", "Title, First Name, and Last Name cannot be empty.");
                    return;
                }

                // Validierung des Jahres
                if (!isValidYear(String.valueOf(year))) {
                    showAlert("Invalid Year", "Please enter a valid year.");
                    return;
                }

                // Buch-Daten aktualisieren
                book.setTitle(title);
                book.setFirstName(firstName);
                book.setLastName(lastName);
                book.setGenre(genre);
                book.setPublicationYear(year);
                book.setIsbn(isbn);
                book.setRead(readCheckBox.isSelected());
                book.setRating(ratingComboBox.getValue());
                book.setComment(commentArea.getText());

                // Fügt das Buch einer Sammlung hinzu, wenn es neu ist
                if (isNew) {
                    boolean success = currentCollection.addBook(book);
                    if (success) {
                        bookListData.add(book); // Aktualisieren, wenn erfolgreich
                        System.out.println("Added new book: " + book);
                    } else {
                        showAlert("Duplicate Book", "A book with the same title or ISBN already exists.");
                        return;
                    }
                } else { // Prüft auf Duplikate (falls das Buch bearbeitet wird)
                    if (currentCollection.isDuplicateExcept(book)) {
                        showAlert("Duplicate Book", "A book with the same title or ISBN already exists.");
                        return;
                    }

                    // Aktualisieren der bestehenden Buchanzeige
                    int index = bookListData.indexOf(book);
                    if (index >= 0) {
                        bookListData.set(index, book);
                    }

                }

                // Speichern der aktuellen Collection
                collectionManager.saveBooksForCollection(currentCollection);

                stage.close(); // Fenster schließen
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter valid numerical values for Year and ISBN.");
            }
        });

        // Setzt die Reihenfolge der Navigation mit der Enter-Taste
        setEnterKeyTraversal(titleField, firstNameField);
        setEnterKeyTraversal(firstNameField, lastNameField);
        setEnterKeyTraversal(lastNameField, yearField);
        setEnterKeyTraversal(yearField, isbnField);
        setEnterKeyTraversal(isbnField, genreField);
        setEnterKeyTraversal(genreField, saveButton);

        // Zusätzliche Navigation für Rating und Kommentar
        ratingComboBox.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                System.out.println("Enter pressed in: " + ratingComboBox.getId());
                commentArea.requestFocus(); // Springe zur Kommentarbox bei Enter
            }
        });

        commentArea.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                System.out.println("Enter pressed in: " + commentArea.getId());
                saveButton.requestFocus(); // Springe zum Save-Button
            }
        });

        // Anordnung der Elemente im Grid-Layout für das Buch-Formular
        grid.add(titleLabel, 0, 0);
        grid.add(titleField, 1, 0, 3, 1);

        grid.add(firstNameLabel, 0, 1);
        grid.add(firstNameField, 1, 1);
        grid.add(lastNameLabel, 2, 1);
        grid.add(lastNameField, 3, 1);

        grid.add(yearLabel, 0, 2);
        grid.add(yearField, 1, 2);
        grid.add(isbnLabel, 2, 2);
        grid.add(isbnField, 3, 2);

        grid.add(genreLabel, 0,3);
        grid.add(genreField, 1,3);

        grid.add(readLabel, 0, 4);
        grid.add(readCheckBox, 1, 4);
        grid.add(ratingLabel, 2, 4);
        grid.add(ratingComboBox, 3, 4);

        grid.add(commentLabel, 0, 5);
        grid.add(commentArea, 1, 5, 3, 1);

        grid.add(saveButton, 0, 6, 4, 1);

        // Szene und Fenster anzeigen
        Scene scene = new Scene(grid, 500, 400); // Szene erstellen mit Grid-Layout und Abmessungen 500x400
        stage.setScene(scene);                  // Bühne (Fenster) die Szene zuweisen
        stage.showAndWait();                    // Fenster anzeigen und warten, bis es geschlossen wird (modal)
    }

    /**
     * Öffnet ein schreibgeschütztes Fenster zur Anzeige der Buchdetails.
     */
    private void openShowWindow(Book book) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL); // Modaler Dialog, blockiert andere Fenster
        stage.setTitle("Show Book"); // Titel des Fensters setzen

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        // Erstellen der Labels und schreibgeschützten Felder für die Buchdetails
        // Alle Felder sind deaktiviert, um Bearbeitung zu verhindern
        Label titleLabel = new Label("Title:");
        TextField titleField = new TextField(book.getTitle());
        titleField.setDisable(true);

        Label firstNameLabel = new Label("First Name:");
        TextField firstNameField = new TextField(book.getFirstName());
        firstNameField.setDisable(true);

        Label lastNameLabel = new Label("Last Name:");
        TextField lastNameField = new TextField(book.getLastName());
        lastNameField.setDisable(true);

        Label genreLabel = new Label("Genre");
        TextField genreField = new TextField(book.getGenre());
        genreField.setDisable(true);

        Label yearLabel = new Label("Year:");
        TextField yearField = new TextField(String.valueOf(book.getPublicationYear() > 0 ?
                book.getPublicationYear() :
                Year.now().getValue()));

        Label isbnLabel = new Label("ISBN:");
        TextField isbnField = new TextField(String.valueOf(book.getIsbn()));
        isbnField.setDisable(true);

        Label readLabel = new Label("Read:");
        CheckBox readCheckBox = new CheckBox();
        readCheckBox.setSelected(book.isRead());
        readCheckBox.setDisable(true);

        Label ratingLabel = new Label("Rating:");
        ComboBox<String> ratingComboBox = new ComboBox<>();
        ratingComboBox.getItems().addAll("1", "2", "3");
        ratingComboBox.setValue(book.getRating());
        ratingComboBox.setDisable(true);

        Label commentLabel = new Label("Comment:");
        TextArea commentArea = new TextArea(book.getComment());
        commentArea.setDisable(true);

        // Schließen-Button zur Rückkehr in die Hauptanwendung
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> stage.close());

        // Fokusreihenfolge für das Formular setzen
        setEnterKeyTraversal(titleField, firstNameField);
        setEnterKeyTraversal(firstNameField, lastNameField);
        setEnterKeyTraversal(lastNameField, yearField);
        setEnterKeyTraversal(yearField, isbnField);
        setEnterKeyTraversal(isbnField, genreField);
        setEnterKeyTraversal(genreField, ratingComboBox);
        ratingComboBox.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                commentArea.requestFocus(); // Springe zur Kommentarbox
            }
        });
    }
}