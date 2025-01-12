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
import javafx.scene.control.cell.ComboBoxTableCell;
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
import java.util.Optional;

public class BookManagerApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Placeholder implementation to fulfill the abstract method requirements
        // The proper initialization can take place within the start(Stage, String) method
        startWithUser(primaryStage, System.getProperty("user.dir"), "DefaultUser");
    }

    private CollectionManager collectionManager = new CollectionManager();
    private ObservableList<Book> bookListData = FXCollections.observableArrayList();
    private String collectionsFilePath = "collections.yaml"; // Pfad zu collections.yaml

    private Collection currentCollection; // Aktuell ausgewählte Sammlung
    private ComboBox<String> collectionComboBox; // Klassenvariable für die ComboBox
    private ObservableList<String> collectionsObservableList; // ObservableList für Collections

    public static void main(String[] args) {
        launch(args);
    }

    public void startWithUser(Stage primaryStage, String userDirectoryPath, String username) {
        this.collectionManager = new CollectionManager(userDirectoryPath);
        this.collectionsFilePath = userDirectoryPath + "/collections.yaml"; // Angepasst für Benutzer

        collectionManager.loadCollectionNames("collections.yaml"); // Collection-Namen aus der Datei laden

        start(primaryStage, username); // Hauptfenster starten
    }


    //@Override
    public void start(Stage primaryStage, String username) {

        // Sicherstellen, dass das Verzeichnis existiert
        //ensureCollectionsDirectoryExists();

        // Vorhandene Collection-Namen laden
        collectionManager.loadCollectionNames(collectionsFilePath);

        // Initialisiere das ObservableList mit den Collection-Namen
        collectionsObservableList = FXCollections.observableArrayList(collectionManager.getCollectionNames());

        // Wenn keine Collections vorhanden sind, eine Standard-Collection erstellen
        if (collectionsObservableList.isEmpty()) {
            String defaultName = "default";
            collectionsObservableList.add(defaultName);
            collectionManager.addNewCollection(defaultName); // Default-Collection auch abspeichern
        }

        // Label zur Anzeige des eingeloggten Benutzers
        Label loggedInUserLabel = new Label("Logged in as: " + username); // "Benutzername" durch den echten Usernamen ersetzen
        loggedInUserLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;"); // Beispielstil

// Platzhalter für den Benutzer (kann angepasst oder überschrieben werden)
        //String currentUsername = "AktuellerBenutzer"; // Dies könnte aus einer Login-Logik kommen
        //loggedInUserLabel.setText("Logged in als: " + currentUsername);

// HBox für den User-Display oben
        HBox userDisplayBox = new HBox(loggedInUserLabel);
        userDisplayBox.setStyle("-fx-padding: 5; -fx-background-color: #f0f0f0; -fx-alignment: center-left;");
        userDisplayBox.setPadding(new Insets(5, 10, 5, 10));

        // GUI-Komponenten für die erste Zeile (Collection-Auswahl und Add Collection)
        collectionComboBox = new ComboBox<>(collectionsObservableList);
        collectionComboBox.setPrefWidth(150);
        collectionComboBox.setPromptText("Select Collection");

        // Wähle standardmäßig die erste Collection aus (z. B. Default)
        if (!collectionsObservableList.isEmpty()) {
            String firstCollection = collectionsObservableList.get(0); // Nimm die erste Collection
            collectionComboBox.getSelectionModel().select(firstCollection); // Setze die Auswahl in der ComboBox
            currentCollection = collectionManager.loadBooksForCollection(firstCollection); // Lade Bücher der ersten Collection
            bookListData.setAll(currentCollection.getBooks()); // Aktualisiere die Buch-Liste (Tabelle)
        }

        // ComboBox-Listener: Aktualisiere `currentCollection`, wenn sich die Auswahl ändert
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
            LoginScreen loginScreen = new LoginScreen(); // Instanziere den LoginScreen
            try {
                loginScreen.start(primaryStage); // Setze die Szene auf den LoginScreen
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });



        // GUI-Komponenten für die zweite Zeile (Search, Buttons, Export)
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");
        Button searchButton = new Button("🔍");

        // Reset-Button
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
            searchField.clear();
            loadBooksForCurrentCollection();
        });

        // Import-Funktion
        Button importButton = new Button("Import...");
        importButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Import Books");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("YAML Files (*.yaml)", "*.yaml"),
                    new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx")
            );

            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                String fileName = file.getName().toLowerCase();
                String filePath = file.getAbsolutePath();
                String collectionName = currentCollection.getName();

                boolean success = false;
                if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
                    success = collectionManager.importFromYaml(filePath, collectionName);
                } else if (fileName.endsWith(".xlsx")) {
                    success = collectionManager.importFromXlsx(filePath, collectionName);
                }

                if (success) {
                    loadBooksForCurrentCollection(); // Tabelle aktualisieren
                    showInfo("Import Successful", "Books have been successfully imported!");
                } else {
                    showAlert("Import Failed", "Could not import books.");
                }
            }
        });


        Button addButton = new Button("Add Book");

        // Export-Button mit FileChooser
        Button exportButton = new Button("Export...");

        // Export-Funktion per FileChooser
        exportButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Books");

            // YAML und XLSX als Optionen
            FileChooser.ExtensionFilter yamlFilter =
                    new FileChooser.ExtensionFilter("YAML Files (*.yaml)", "*.yaml");
            FileChooser.ExtensionFilter xlsxFilter =
                    new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx");
            fileChooser.getExtensionFilters().addAll(yamlFilter, xlsxFilter);

            // Dialog anzeigen
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                String fileName = file.getName().toLowerCase();

                if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
                    // YAML Export aller Collections
                    collectionManager.saveCollectionNames(collectionsFilePath);
                    for (String name : collectionManager.getCollectionNames()) {
                        Collection collection = collectionManager.loadBooksForCollection(name);
                        collectionManager.saveBooksForCollection(collection);
                    }
                    showInfo("Export Successful", "Collections have been successfully exported to " + file.getAbsolutePath());
                } else if (fileName.endsWith(".xlsx")) {
                    // XLSX Export
                    collectionManager.exportToXlsx(file.getAbsolutePath());
                    showInfo("Export Successful", "Collections have been successfully exported to " + file.getAbsolutePath());
                } else {
                    // Warnung: Unbekannte Endung
                    showAlert("Unknown File Extension",
                            "Please use .yaml (or .yml) or .xlsx extension!");
                }
            }
        });

        // Suchfunktion
        searchButton.setOnAction(ev -> searchBooks(searchField));
        searchField.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                searchBooks(searchField);
            }
        });

        // Neues Buch hinzufügen
        addButton.setOnAction(ev -> openAddBookWindow());


        // Spacer für die erste Zeile
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, javafx.scene.layout.Priority.ALWAYS);

        // HBox für Collection-Auswahl und Buttons (erste Zeile)
        HBox collectionBox = new HBox(10, collectionComboBox, addCollectionButton, renameCollectionButton, deleteCollectionButton, logoutButton, spacer1, importButton);
        collectionBox.setPadding(new Insets(5));

        // Spacer für die zweite Zeile
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, javafx.scene.layout.Priority.ALWAYS);

        // HBox für die zweite Zeile (Search, Buttons, Export)
        HBox actionBox = new HBox(10, searchField, searchButton, resetButton, addButton, spacer2, exportButton);
        actionBox.setPadding(new Insets(5));



        // VBox, die beide HBoxes enthält
        //VBox topContainer = new VBox(0, collectionBox, actionBox);

        // VBox, die LogIn-Info und bestehende HBoxes (Collections, Actions) enthält
        VBox topContainer = new VBox(10, userDisplayBox, collectionBox, actionBox);
        topContainer.setPadding(new Insets(10));

        // TableView
        TableView<Book> bookTableView = new TableView<>();
        bookTableView.setEditable(true);

        // RowFactory + ContextMenu (Show / Edit / Delete)
        bookTableView.setRowFactory(tv -> {
            TableRow<Book> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            // "Show"
            MenuItem showItem = new MenuItem("Show");
            showItem.setOnAction(e -> {
                Book rowData = row.getItem();
                if (rowData != null) {
                    openShowWindow(rowData);
                }
            });

            // "Edit"
            MenuItem editItem = new MenuItem("Edit");
            editItem.setOnAction(e -> {
                Book rowData = row.getItem();
                if (rowData != null) {
                    openEditWindow(rowData);
                }
            });

            // "Delete"
            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(e -> {
                Book rowData = row.getItem();
                if (rowData != null) {
                    currentCollection.removeBook(rowData.getTitle());
                    bookListData.remove(rowData);
                    // Sofort in YAML speichern
                    collectionManager.saveBooksForCollection(currentCollection);
                }
            });

            contextMenu.getItems().addAll(showItem, editItem, deleteItem);

            // Kontextmenü nur anzeigen, wenn Zeile nicht leer ist
            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu)null)
                            .otherwise(contextMenu)
            );

            return row;
        });

        // -- Spalten definieren --

        TableColumn<Book, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getTitle()));
        titleColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        titleColumn.setOnEditCommit(event -> {
            Book book = event.getRowValue();
            book.setTitle(event.getNewValue());
            collectionManager.saveBooksForCollection(currentCollection);
        });
        titleColumn.setEditable(true);

        TableColumn<Book, String> authorColumn = new TableColumn<>("Author");
        authorColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(
                        cd.getValue().getFirstName() + " " + cd.getValue().getLastName()
                ));
        authorColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        authorColumn.setOnEditCommit(event -> {
            Book book = event.getRowValue();
            String newValue = event.getNewValue();
            String[] parts = newValue.split("\\s+", 2);
            if (parts.length == 2) {
                book.setFirstName(parts[0]);
                book.setLastName(parts[1]);
            } else {
                book.setFirstName(parts[0]);
                book.setLastName("");
            }
            collectionManager.saveBooksForCollection(currentCollection);
        });
        authorColumn.setEditable(true);

        TableColumn<Book, Integer> yearColumn = new TableColumn<>("Publication Year");
        yearColumn.setCellValueFactory(cd ->
                new SimpleIntegerProperty(cd.getValue().getPublicationYear()).asObject());
        yearColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        /*yearColumn.setOnEditCommit(event -> {
            Book book = event.getRowValue();
            book.setPublicationYear(event.getNewValue());
            collectionManager.saveBooksForCollection(currentCollection);
        });
        yearColumn.setOnEditCommit(event -> {
                    Book book = event.getRowValue();
                    int newYear = event.getNewValue();

                    // Validierung der Jahreszahl
                    if (isValidYear(String.valueOf(newYear))) {
                        book.setPublicationYear(newYear);
                        collectionManager.saveBooksForCollection(currentCollection);
                    } else {
                        showAlert("Invalid Year", "The year must be a valid number between 1000 and 9999.");
                        loadBooksForCurrentCollection(); // Zurücksetzen auf alten Wert
                    }
        });*/

        yearColumn.setOnEditCommit(event -> {
            Book book = event.getRowValue();
            int newYear = event.getNewValue();

            // Validierung der Jahreszahl (falls Tabelle direkt bearbeitet wird)
            if (isValidYear(String.valueOf(newYear))) {
                book.setPublicationYear(newYear);
                collectionManager.saveBooksForCollection(currentCollection); // Speichern
            } else {
                showAlert("Invalid Year", "The year must be a valid number between 1000 and the current year.");
                loadBooksForCurrentCollection(); // Zurücksetzen, wenn ungültig
            }
        });

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

        TableColumn<Book, Boolean> readColumn = new TableColumn<>("Read");
        readColumn.setCellValueFactory(cd ->
                new SimpleBooleanProperty(cd.getValue().isRead()));
        readColumn.setCellFactory(CheckBoxTableCell.forTableColumn(readColumn));
        readColumn.setEditable(true);
        readColumn.setOnEditCommit(event -> {
            Book book = event.getRowValue();
            book.setRead(event.getNewValue());
            collectionManager.saveBooksForCollection(currentCollection);
        });

        TableColumn<Book, String> ratingColumn = new TableColumn<>("Rating");
        ratingColumn.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getRating()));
        ObservableList<String> ratingOptions = FXCollections.observableArrayList("1", "2", "3");
        ratingColumn.setCellFactory(ComboBoxTableCell.forTableColumn(ratingOptions));
        ratingColumn.setOnEditCommit(event -> {
            Book book = event.getRowValue();
            book.setRating(event.getNewValue());
            collectionManager.saveBooksForCollection(currentCollection);
        });
        ratingColumn.setEditable(true);

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
                editButton.setOnAction(evt -> {
                    Book book = getTableView().getItems().get(getIndex());
                    openEditWindow(book);
                });
            }

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editButton);
                setAlignment(Pos.CENTER);
            }
        });

        // Delete-Button (in Zelle)
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
                deleteButton.setOnAction(evt -> {
                    Book book = getTableView().getItems().get(getIndex());
                    currentCollection.removeBook(book.getTitle());
                    bookListData.remove(book);
                    collectionManager.saveBooksForCollection(currentCollection);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
                setAlignment(Pos.CENTER);
            }
        });

        // Spalten zur Tabelle hinzufügen
        bookTableView.getColumns().addAll(
                titleColumn, authorColumn, yearColumn, isbnColumn,
                readColumn, ratingColumn, commentColumn, editColumn, deleteColumn
        );
        editColumn.setSortable(false);
        deleteColumn.setSortable(false);

        // VBox, die beide HBoxes enthält
        // VBox topContainer = new VBox(5, collectionBox, actionBox);

        // Listener für Collection-Auswahl
        collectionComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentCollection = collectionManager.loadBooksForCollection(newVal);
                bookListData.setAll(currentCollection.getBooks());
                System.out.println("Current Collection set to: " + currentCollection.getName());
            }
        });

        // Initialisierung der aktuellen Collection durch Auswahl der ersten Collection
        collectionComboBox.getSelectionModel().selectFirst();

        // Daten an die Tabelle binden
        bookTableView.setItems(bookListData);

        Scene scene = new Scene(new VBox(10, topContainer, bookTableView), 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Book Collection Manager");
        primaryStage.show();

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
            System.out.println("Sammlungen wurden vor dem Beenden gespeichert.");
        });



    }

    //Überprüft ob die Eingabe der Jahreszahl valide ist
    private boolean isValidYear(String yearString) {
        //if (year.isEmpty()) return false;
        try {
            int intYear = Integer.parseInt(yearString);
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
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add New Collection");
        dialog.setHeaderText(null);
        dialog.setContentText("Collection Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            String trimmedName = name.trim();
            if (!trimmedName.isEmpty() && isValidFileName(trimmedName)) {
                collectionManager.addNewCollection(trimmedName); // Methode im CollectionManager aufrufen
                collectionsObservableList.add(trimmedName); // ObservableList aktualisieren
                collectionComboBox.getSelectionModel().select(trimmedName);
                showInfo("Collection Added", "Collection '" + trimmedName + "' wurde erfolgreich hinzugefügt.");
            } else {
                showAlert("Invalid Name", "Collection name cannot be empty or contains invalid characters.");
            }
        });
    }

    /**
     * Handler zum Umbenennen der aktuell ausgewählten Collection.
     */
    private void renameSelectedCollection() {
        String selectedCollection = collectionComboBox.getSelectionModel().getSelectedItem();
        if (selectedCollection == null) {
            showAlert("No Collection Selected", "Bitte wähle eine Collection aus, die du umbenennen möchtest.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selectedCollection);
        dialog.setTitle("Rename Collection");
        dialog.setHeaderText(null);
        dialog.setContentText("New Collection Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            String trimmedName = newName.trim();
            if (trimmedName.isEmpty() || !isValidFileName(trimmedName)) {
                showAlert("Invalid Name", "Invalid or empty name.");
                return;
            }

            boolean success = collectionManager.renameSelectedCollection(selectedCollection, trimmedName); // Neue Methode aufrufen
            if (success) {
                collectionsObservableList.set(collectionsObservableList.indexOf(selectedCollection), trimmedName);
                collectionComboBox.getSelectionModel().select(trimmedName);
                showInfo("Renamed Successfully", "Die Collection wurde erfolgreich umbenannt.");
            } else {
                showAlert("Rename Failed", "Die Collection konnte nicht umbenannt werden.");
            }
        });
    }

    /**
     * Handler zum Löschen der aktuell ausgewählten Collection.
     */
    private void deleteSelectedCollection() {
        String selectedCollection = collectionComboBox.getSelectionModel().getSelectedItem();
        if (selectedCollection == null) {
            showAlert("No Collection Selected", "Bitte wähle eine Collection aus, die du löschen möchtest.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Collection");
        confirmation.setContentText("Are you sure you want to delete the collection '" + selectedCollection + "'? This action cannot be undone.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = collectionManager.deleteSelectedCollection(selectedCollection); // Neue Methode aufrufen
            if (success) {
                collectionsObservableList.remove(selectedCollection); // ObservableList aktualisieren
                showInfo("Deleted Successfully", "Die Collection wurde erfolgreich gelöscht.");
                collectionComboBox.getSelectionModel().selectFirst(); // Wähle die erste Collection aus
            } else {
                showAlert("Delete Failed", "Die Collection konnte nicht gelöscht werden.");
            }
        }
    }

    /**
     * Überprüft, ob ein String ein gültiger Dateiname ist.
     */
    private boolean isValidFileName(String name) {
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
        alert.showAndWait();
    }

    /**
     * Stellt sicher, dass das Verzeichnis "collections" existiert.
     */
    /*
    private void ensureCollectionsDirectoryExists() {
        collectionManager.ensureCollectionsDirectoryExists(); // Direkt die Methode aus CollectionManager verwenden
    }
*/
    /**
     * Lädt die Bücher für die aktuell ausgewählte Collection.
     */
    private void loadBooksForCurrentCollection() {
        if (currentCollection != null) {
            bookListData.setAll(currentCollection.getBooks());
        }
    }

    /**
     * Sucht nach Büchern, die das Keyword enthalten (Titel, Autor, Jahr, ISBN).
     */
    private void searchBooks(TextField searchField) {
        String keyword = searchField.getText().toLowerCase();
        bookListData.clear();
        if (currentCollection != null) {
            currentCollection.search(keyword).forEach(bookListData::add);
        }
    }

    /**
     * Öffnet ein Fenster, um ein neues Book anzulegen.
     */
    private void openAddBookWindow() {
        openBookForm(new Book("", "", "", 0, 0), true);
    }

    /**
     * Öffnet ein Dialogfenster, um ein existierendes Book zu bearbeiten.
     */
    private void openEditWindow(Book book) {
        openBookForm(book, false);
    }

    /**
     * Zeigt ein Book-Formular (Add oder Edit) in einem neuen Dialogfenster.

    private void openBookForm(Book book, boolean isNew) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(isNew ? "Add Book" : "Edit Book");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        Label titleLabel = new Label("Title:");
        TextField titleField = new TextField(book.getTitle());

        Label firstNameLabel = new Label("First Name:");
        TextField firstNameField = new TextField(book.getFirstName());
        Label lastNameLabel = new Label("Last Name:");
        TextField lastNameField = new TextField(book.getLastName());

        Label yearLabel = new Label("Year:");

        TextField yearField = new TextField(String.valueOf(book.getPublicationYear() > 0 ? book.getPublicationYear() : Year.now().getValue()));

        //TextField yearField = new TextField(String.valueOf(book.getPublicationYear()));

        //Formatter für die numerische Eingabe
        TextFormatter<Integer> yearFormatter = new TextFormatter<>(
                new IntegerStringConverter(),
                book.getPublicationYear() > 0 ? book.getPublicationYear() : Year.now().getValue(),
                change -> change.getControlNewText().matches("\\d*") ? change : null // Akzeptiere nur Ziffern oder Leerzeichen
        );
        yearField.setTextFormatter(yearFormatter);

        Label isbnLabel = new Label("ISBN:");
        TextField isbnField = new TextField(String.valueOf(book.getIsbn()));

        Label readLabel = new Label("Read:");
        CheckBox readCheckBox = new CheckBox();
        readCheckBox.setSelected(book.isRead());

        Label ratingLabel = new Label("Rating:");
        ComboBox<String> ratingComboBox = new ComboBox<>();
        ratingComboBox.getItems().addAll("1", "2", "3");
        ratingComboBox.setValue(book.getRating() == null ? "" : book.getRating());

        Label commentLabel = new Label("Comment:");
        TextArea commentArea = new TextArea(book.getComment());

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            try {
                String title = titleField.getText().trim();
                String firstName = firstNameField.getText().trim();
                String lastName = lastNameField.getText().trim();
                int year = Integer.parseInt(yearField.getText().trim());
                long isbn = Long.parseLong(isbnField.getText().trim());

                if (title.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                    showAlert("Invalid Input", "Title, First Name, and Last Name cannot be empty.");
                    return;
                }

                if (!isValidYear(String.valueOf(year))) {
                    showAlert("Invalid Year", "The year must be a valid number between 1000 and current year.");
                    return;
                }

                book.setTitle(title);
                book.setFirstName(firstName);
                book.setLastName(lastName);
                book.setPublicationYear(year);
                book.setIsbn(isbn);

                book.setRead(readCheckBox.isSelected());
                book.setRating(ratingComboBox.getValue());
                book.setComment(commentArea.getText());

                if (isNew) {
                    currentCollection.addBook(book);
                    bookListData.add(book);
                    System.out.println("Added book to collection '" + currentCollection.getName() + "': " + book);
                }else {
                    // Aktualisiere die Anzeige für bestehendes Buch
                    int index = bookListData.indexOf(book); // Index des Buchs
                    if (index >= 0) {
                        bookListData.set(index, book); // Aktualisiere den Eintrag
                    }
                }

                // Speichern der aktuellen Collection
                collectionManager.saveBooksForCollection(currentCollection);

                stage.close();
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter valid numerical values for Year and ISBN.");
            }
        });

        // Anordnung
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

        grid.add(readLabel, 0, 3);
        grid.add(readCheckBox, 1, 3);
        grid.add(ratingLabel, 2, 3);
        grid.add(ratingComboBox, 3, 3);

        grid.add(commentLabel, 0, 4);
        grid.add(commentArea, 1, 4, 3, 1);

        grid.add(saveButton, 0, 5, 4, 1);

        Scene scene = new Scene(grid, 500, 400);
        stage.setScene(scene);
        stage.showAndWait();
    } */
    // Methode zur Steuerung, welcher Element den Fokus bei "Enter" erhält
    private void setEnterKeyTraversal(TextField currentField, Control nextField) {
        currentField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                nextField.requestFocus(); // Nächstes Feld erhält den Fokus
            }
        });
    }

    /**
     * Öffnet ein Fenster zur Anzeige der Book-Details.
     */

    private void openBookForm(Book book, boolean isNew) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(isNew ? "Add Book" : "Edit Book");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        // Felder mit ID für Debugging konfigurieren
        Label titleLabel = new Label("Title:");
        TextField titleField = new TextField(book.getTitle());
        titleField.setId("titleField"); // ID setzen für Debugging

        Label firstNameLabel = new Label("First Name:");
        TextField firstNameField = new TextField(book.getFirstName());
        firstNameField.setId("firstNameField");

        Label lastNameLabel = new Label("Last Name:");
        TextField lastNameField = new TextField(book.getLastName());
        lastNameField.setId("lastNameField");

        Label yearLabel = new Label("Year:");
        TextField yearField = new TextField(String.valueOf(
                book.getPublicationYear() > 0 ? book.getPublicationYear() : Year.now().getValue()));
        yearField.setId("yearField");

        // Formatter für numerische Eingabe (nur Zahlen erlauben)
        TextFormatter<Integer> yearFormatter = new TextFormatter<>(
                new IntegerStringConverter(),
                book.getPublicationYear() > 0 ? book.getPublicationYear() : Year.now().getValue(),
                change -> change.getControlNewText().matches("\\d*") ? change : null // Nur Ziffern erlauben
        );
        yearField.setTextFormatter(yearFormatter);

        Label isbnLabel = new Label("ISBN:");
        TextField isbnField = new TextField(String.valueOf(book.getIsbn()));
        isbnField.setId("isbnField");

        Label readLabel = new Label("Read:");
        CheckBox readCheckBox = new CheckBox();
        readCheckBox.setSelected(book.isRead());

        Label ratingLabel = new Label("Rating:");
        ComboBox<String> ratingComboBox = new ComboBox<>();
        ratingComboBox.getItems().addAll("1", "2", "3");
        ratingComboBox.setValue(book.getRating() == null ? "" : book.getRating());
        ratingComboBox.setId("ratingComboBox"); // ID setzen für Debugging

        Label commentLabel = new Label("Comment:");
        TextArea commentArea = new TextArea(book.getComment());
        commentArea.setId("commentArea");

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            try {
                String title = titleField.getText().trim();
                String firstName = firstNameField.getText().trim();
                String lastName = lastNameField.getText().trim();
                int year = Integer.parseInt(yearField.getText().trim());
                long isbn = Long.parseLong(isbnField.getText().trim());

                if (title.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                    showAlert("Invalid Input", "Title, First Name, and Last Name cannot be empty.");
                    return;
                }

                if (!isValidYear(String.valueOf(year))) {
                    showAlert("Invalid Year", "The year must be a valid number between 1000 and current year.");
                    return;
                }

                // Buch-Daten aktualisieren
                book.setTitle(title);
                book.setFirstName(firstName);
                book.setLastName(lastName);
                book.setPublicationYear(year);
                book.setIsbn(isbn);

                book.setRead(readCheckBox.isSelected());
                book.setRating(ratingComboBox.getValue());
                book.setComment(commentArea.getText());

                if (isNew) {
                    currentCollection.addBook(book);
                    bookListData.add(book);
                    System.out.println("Added book to collection '" + currentCollection.getName() + "': " + book);
                } else {
                    // Aktualisiere Anzeige für bestehendes Buch
                    int index = bookListData.indexOf(book); // Index des Buchs finden
                    if (index >= 0) {
                        bookListData.set(index, book); // Aktualisiere Eintrag
                    }
                }

                // Speichern der aktuellen Collection
                collectionManager.saveBooksForCollection(currentCollection);

                stage.close(); // Fenster schließen
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter valid numerical values for Year and ISBN.");
            }
        });

        // Fokus-Reihenfolge setzen
        setEnterKeyTraversal(titleField, firstNameField);
        setEnterKeyTraversal(firstNameField, lastNameField);
        setEnterKeyTraversal(lastNameField, yearField);
        setEnterKeyTraversal(yearField, isbnField);
        setEnterKeyTraversal(isbnField, saveButton);

        ratingComboBox.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                System.out.println("Enter gedrückt in: " + ratingComboBox.getId());
                commentArea.requestFocus(); // Springe zur Kommentarbox bei Enter
            }
        });

        commentArea.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                System.out.println("Enter gedrückt in: " + commentArea.getId());
                saveButton.requestFocus(); // Springe zum Save-Button
            }
        });

        // Anordnung von Elementen im Grid
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

        grid.add(readLabel, 0, 3);
        grid.add(readCheckBox, 1, 3);
        grid.add(ratingLabel, 2, 3);
        grid.add(ratingComboBox, 3, 3);

        grid.add(commentLabel, 0, 4);
        grid.add(commentArea, 1, 4, 3, 1);

        grid.add(saveButton, 0, 5, 4, 1);

        // Szene und Fenster anzeigen
        Scene scene = new Scene(grid, 500, 400);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void openShowWindow(Book book) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Show Book");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        Label titleLabel = new Label("Title:");
        TextField titleField = new TextField(book.getTitle());
        titleField.setDisable(true);

        Label firstNameLabel = new Label("First Name:");
        TextField firstNameField = new TextField(book.getFirstName());
        firstNameField.setDisable(true);

        Label lastNameLabel = new Label("Last Name:");
        TextField lastNameField = new TextField(book.getLastName());
        lastNameField.setDisable(true);

        Label yearLabel = new Label("Year:");
        TextField yearField = new TextField(String.valueOf(book.getPublicationYear() > 0 ?
                book.getPublicationYear() :
                Year.now().getValue()));
        /*TextField yearField = new TextField(String.valueOf(book.getPublicationYear()));
        yearField.setDisable(true);*/

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

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> stage.close());

        // Fokusreihenfolge für das Formular setzen
        setEnterKeyTraversal(titleField, firstNameField);
        setEnterKeyTraversal(firstNameField, lastNameField);
        setEnterKeyTraversal(lastNameField, yearField);
        setEnterKeyTraversal(yearField, isbnField);
        setEnterKeyTraversal(isbnField, ratingComboBox);
        ratingComboBox.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                commentArea.requestFocus(); // Springe zur Kommentarbox
            }
        });


        // Anordnung
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

        grid.add(readLabel, 0, 3);
        grid.add(readCheckBox, 1, 3);
        grid.add(ratingLabel, 2, 3);
        grid.add(ratingComboBox, 3, 3);

        grid.add(commentLabel, 0, 4);
        grid.add(commentArea, 1, 4, 3, 1);

        grid.add(closeButton, 0, 5, 4, 1);

        Scene scene = new Scene(grid, 500, 400);
        stage.setScene(scene);
        stage.showAndWait();
    }

}
