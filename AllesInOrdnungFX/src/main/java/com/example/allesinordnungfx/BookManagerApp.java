package com.example.allesinordnungfx;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class BookManagerApp extends Application {

    private CollectionManager collectionManager = new CollectionManager();
    private ObservableList<Book> bookListData = FXCollections.observableArrayList();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        //Lädt die Bücher aus der bestehenden YAML-Datei zur Anzeige der Bücher beim Starten des Programms
        bookListData.addAll(collectionManager.getAllBooks());

        TableView<Book> bookTableView = new TableView<>();

        TableColumn<Book, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));

        TableColumn<Book, String> authorColumn = new TableColumn<>("Author");
        authorColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstName() + " " + cellData.getValue().getLastName()));

        TableColumn<Book, Integer> yearColumn = new TableColumn<>("Publication Year");
        yearColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getPublicationYear()).asObject());

        TableColumn<Book, Long> isbnColumn = new TableColumn<>("ISBN");
        isbnColumn.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().getIsbn()).asObject());

        TableColumn<Book, Void> editColumn = new TableColumn<>("Edit");
        editColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("✏️");

            {
                editButton.setOnAction(event -> {
                    Book book = getTableView().getItems().get(getIndex());
                    openEditWindow(book);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editButton);
                }
            }
        });

        TableColumn<Book, Void> deleteColumn = new TableColumn<>("Delete");
        deleteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("🗑️");

            {
                deleteButton.setOnAction(event -> {
                    Book book = getTableView().getItems().get(getIndex());
                    collectionManager.removeBook(book.getTitle());
                    bookListData.remove(book);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        bookTableView.getColumns().addAll(titleColumn, authorColumn, yearColumn, isbnColumn, editColumn, deleteColumn);
        bookTableView.setItems(bookListData);

        TextField searchField = new TextField();
        Button searchButton = new Button("Search");
        Button addButton = new Button("Add Book");

        HBox searchBar = new HBox(10, searchField, searchButton);

        searchButton.setOnAction(e -> searchBooks(searchField));
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                searchBooks(searchField);
            }
        });

        addButton.setOnAction(e -> openStepByStepAddWindow());

        VBox layout = new VBox(10,
                searchBar,
                addButton, bookTableView);

        Scene scene = new Scene(layout, 700, 600);
        primaryStage.setTitle("Book Collection Manager");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void searchBooks(TextField searchField) {
        String keyword = searchField.getText().toLowerCase();
        bookListData.clear();
        collectionManager.search(keyword).forEach(bookListData::add);
    }

    private void openEditWindow(Book book) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Edit Book");

        VBox layout = new VBox(10);
        TextField titleField = new TextField(book.getTitle());
        TextField firstNameField = new TextField(book.getFirstName());
        TextField lastNameField = new TextField(book.getLastName());
        TextField yearField = new TextField(String.valueOf(book.getPublicationYear()));
        TextField isbnField = new TextField(String.valueOf(book.getIsbn()));
        Button saveButton = new Button("Save");

        saveButton.setOnAction(e -> {
            try {
                book.setTitle(titleField.getText());
                book.setFirstName(firstNameField.getText());
                book.setLastName(lastNameField.getText());
                book.setPublicationYear(Integer.parseInt(yearField.getText()));
                book.setIsbn(Long.parseLong(isbnField.getText()));

                bookListData.clear();
                collectionManager.getAllBooks().forEach(bookListData::add);
                stage.close();
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter valid values.");
            }
        });

        layout.getChildren().addAll(
                new Label("Title:"), titleField,
                new Label("First Name:"), firstNameField,
                new Label("Last Name:"), lastNameField,
                new Label("Year:"), yearField,
                new Label("ISBN:"), isbnField,
                saveButton
        );

        Scene scene = new Scene(layout, 300, 400);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void openStepByStepAddWindow() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Add Book");

        VBox layout = new VBox(10);
        Label prompt = new Label("Book Title:");
        TextField inputField = new TextField();
        Button nextButton = new Button("Next");

        final String[] title = new String[1];
        final String[] firstName = new String[1];
        final String[] lastName = new String[1];
        final int[] year = new int[1];
        final long[] isbn = new long[1];

        nextButton.setOnAction(e -> processInput(prompt, inputField, title, firstName, lastName, year, isbn, stage));

        layout.getChildren().addAll(prompt, inputField, nextButton);

        Scene scene = new Scene(layout, 300, 200);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void processInput(Label prompt, TextField inputField, String[] title, String[] firstName, String[] lastName, int[] year, long[] isbn, Stage stage) {
        if (prompt.getText().equals("Book Title:")) {
            title[0] = inputField.getText();
            prompt.setText("First Name:");
            inputField.clear();
        } else if (prompt.getText().equals("First Name:")) {
            firstName[0] = inputField.getText();
            prompt.setText("Last Name:");
            inputField.clear();
        } else if (prompt.getText().equals("Last Name:")) {
            lastName[0] = inputField.getText();
            prompt.setText("Year:");
            inputField.clear();
        } else if (prompt.getText().equals("Year:")) {
            year[0] = Integer.parseInt(inputField.getText());
            prompt.setText("ISBN:");
            inputField.clear();
        } else if (prompt.getText().equals("ISBN:")) {
            isbn[0] = Long.parseLong(inputField.getText());
            Book newBook = new Book(title[0], firstName[0], lastName[0], year[0], isbn[0]);
            collectionManager.addBook(newBook);
            bookListData.add(newBook);
            stage.close();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}