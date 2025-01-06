package com.example.allesinordnungfx;

import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

import java.io.File;
import java.util.Optional;

public class BookManagerApp extends Application {

    private CollectionManager collectionManager = new CollectionManager();
    private ObservableList<Book> bookListData = FXCollections.observableArrayList();
    private String collectionsFilePath = "collections.yaml"; // Pfad zu collections.yaml

    private Collection currentCollection; // Aktuell ausgewählte Sammlung
    private ComboBox<String> collectionComboBox; // Klassenvariable für die ComboBox
    private ObservableList<String> collectionsObservableList; // ObservableList für Collections

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Sicherstellen, dass das Verzeichnis existiert
        ensureCollectionsDirectoryExists();

        // Vorhandene Collection-Namen laden
        collectionManager.loadCollectionNames(collectionsFilePath);

        // Wenn keine Collections vorhanden sind, eine Standard-Collection erstellen
        if (collectionManager.getCollectionNames().isEmpty()) {
            String defaultName = "default";
            collectionManager.getCollectionNames().add(defaultName);
            collectionManager.saveCollectionNames(collectionsFilePath);
            collectionManager.saveBooksForCollection(new Collection(defaultName));
        }

        // Initialisiere das ObservableList mit den Collection-Namen
        collectionsObservableList = FXCollections.observableArrayList(collectionManager.getCollectionNames());

        // GUI-Komponenten für die erste Zeile (Collection-Auswahl und Add Collection)
        collectionComboBox = new ComboBox<>(collectionsObservableList);
        collectionComboBox.setPrefWidth(150);
        collectionComboBox.setPromptText("Select Collection");

        // Button zum Hinzufügen einer neuen Collection
        Button addCollectionButton = new Button("Add Collection");
        addCollectionButton.setOnAction(e -> addNewCollection());

        // Button zum Umbenennen einer Collection
        Button renameCollectionButton = new Button("Rename Collection");
        renameCollectionButton.setOnAction(e -> renameSelectedCollection());

        // Button zum Löschen einer Collection
        Button deleteCollectionButton = new Button("Delete Collection");
        deleteCollectionButton.setOnAction(e -> deleteSelectedCollection());



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
        HBox collectionBox = new HBox(10, collectionComboBox, addCollectionButton, renameCollectionButton, deleteCollectionButton, spacer1, importButton);
        collectionBox.setPadding(new Insets(5));

        // Spacer für die zweite Zeile
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, javafx.scene.layout.Priority.ALWAYS);

        // HBox für die zweite Zeile (Search, Buttons, Export)
        HBox actionBox = new HBox(10, searchField, searchButton, resetButton, addButton, spacer2, exportButton);
        actionBox.setPadding(new Insets(5));



        // VBox, die beide HBoxes enthält
        VBox topContainer = new VBox(0, collectionBox, actionBox);

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
        yearColumn.setOnEditCommit(event -> {
            Book book = event.getRowValue();
            book.setPublicationYear(event.getNewValue());
            collectionManager.saveBooksForCollection(currentCollection);
        });
        yearColumn.setEditable(true);

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
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editButton);
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
            }
        });

        // Spalten zur Tabelle hinzufügen
        bookTableView.getColumns().addAll(
                titleColumn, authorColumn, yearColumn, isbnColumn,
                readColumn, ratingColumn, commentColumn, editColumn, deleteColumn
        );

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
            if (!trimmedName.isEmpty()) {
                if (!collectionManager.getCollectionNames().contains(trimmedName)) {
                    if (isValidFileName(trimmedName)) {
                        collectionManager.getCollectionNames().add(trimmedName);
                        collectionManager.saveCollectionNames(collectionsFilePath);
                        collectionManager.saveBooksForCollection(new Collection(trimmedName));

                        // Füge die neue Collection zum ObservableList hinzu
                        collectionsObservableList.add(trimmedName);

                        // Wähle die neu hinzugefügte Collection aus
                        collectionComboBox.getSelectionModel().select(trimmedName);

                        showInfo("Collection Added", "Collection '" + trimmedName + "' wurde erfolgreich hinzugefügt.");
                        System.out.println("Added and selected new collection: " + trimmedName);
                    } else {
                        showAlert("Invalid Name", "Collection name contains invalid characters.");
                    }
                } else {
                    showAlert("Duplicate Collection", "A collection with this name already exists.");
                }
            } else {
                showAlert("Invalid Name", "Collection name cannot be empty.");
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
            if (trimmedName.isEmpty()) {
                showAlert("Invalid Name", "Der Name darf nicht leer sein.");
                return;
            }
            if (!isValidFileName(trimmedName)) {
                showAlert("Invalid Name", "Der Name enthält ungültige Zeichen.");
                return;
            }
            if (collectionManager.getCollectionNames().contains(trimmedName)) {
                showAlert("Duplicate Name", "Eine Collection mit diesem Namen existiert bereits.");
                return;
            }

            boolean success = collectionManager.renameCollection(selectedCollection, trimmedName);
            if (success) {
                // Aktualisiere das ObservableList
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

        // Optional: Verhindere das Löschen der Standard-Collection
        if (selectedCollection.equals("default")) {
            showAlert("Cannot Delete", "Die Standard-Collection 'default' kann nicht gelöscht werden.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Collection");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete the collection '" + selectedCollection + "'? This action cannot be undone.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = collectionManager.deleteCollection(selectedCollection);
            if (success) {
                collectionsObservableList.remove(selectedCollection);
                showInfo("Deleted Successfully", "Die Collection wurde erfolgreich gelöscht.");

                // Wähle die erste verfügbare Collection aus, falls die gelöschte Collection aktuell war
                if (currentCollection.getName().equals(selectedCollection)) {
                    if (!collectionsObservableList.isEmpty()) {
                        collectionComboBox.getSelectionModel().selectFirst();
                    } else {
                        // Falls keine Collections mehr existieren, erstelle eine Standard-Collection
                        String defaultName = "default";
                        collectionManager.getCollectionNames().add(defaultName);
                        collectionManager.saveCollectionNames(collectionsFilePath);
                        collectionManager.saveBooksForCollection(new Collection(defaultName));
                        collectionsObservableList.add(defaultName);
                        collectionComboBox.getSelectionModel().select(defaultName);
                        showInfo("Default Collection Created", "Eine neue Standard-Collection 'default' wurde erstellt.");
                    }
                }
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
    private void ensureCollectionsDirectoryExists() {
        File collectionsDir = new File("collections");
        if (!collectionsDir.exists()) {
            boolean created = collectionsDir.mkdirs();
            if (!created) {
                showAlert("Fehler", "Konnte das Verzeichnis 'collections' nicht erstellen.");
            }
        }
    }

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
     */
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
        TextField yearField = new TextField(String.valueOf(book.getPublicationYear()));
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
    }

    /**
     * Öffnet ein Fenster zur Anzeige der Book-Details.
     */
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
        TextField yearField = new TextField(String.valueOf(book.getPublicationYear()));
        yearField.setDisable(true);

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
