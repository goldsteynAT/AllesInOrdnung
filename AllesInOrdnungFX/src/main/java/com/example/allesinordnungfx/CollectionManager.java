package com.example.allesinordnungfx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class CollectionManager {
    private List<Book> books;

    private ObjectMapper objectMapper;
    private File file;

    //Angepasster Konstruktor, der den ObjectMapper initialisiert und die Bücher lädt
    public CollectionManager() {
        objectMapper = new ObjectMapper(new YAMLFactory());
        file = new File("books.yml");
        loadBooks();
    }

    //Methode zum Laden der Bücher aus der YAML-Datei
    private void loadBooks() {
        if (file.exists()) {
            try {
                books = objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, Book.class));
            } catch (IOException e) {
                e.printStackTrace();
                // Falls ein Fehler auftritt, eine leere Liste initialisieren
                books = new ArrayList<>();
            }
        } else {
            // Falls die Datei nicht existiert, eine leere Liste initialisieren
            books = new ArrayList<>();
        }
    }

    // Methode zum Speichern der Bücher in die YAML-Datei
    private void saveBooks() {
        try {
            // Bücher in die Datei schreiben
            objectMapper.writeValue(file, books);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Angepasste Methode zum Hinzufügen eines Buches
    public void addBook(Book book) {
        books.add(book);
        saveBooks();
    }

    //Angepasste Methode zum Entfernen eines Buches
    public void removeBook(String title) {
        books.removeIf(book -> book.getTitle().equalsIgnoreCase(title));
        saveBooks();
    }

    public List<Book> search(String keyword) {
        List<Book> results = new ArrayList<>();
        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(keyword) ||
                    book.getFirstName().toLowerCase().contains(keyword) ||
                    book.getLastName().toLowerCase().contains(keyword) ||
                    String.valueOf(book.getPublicationYear()).contains(keyword) ||
                    String.valueOf(book.getIsbn()).contains(keyword)) {
                results.add(book);
            }
        }
        return results;
    }


    public List<Book> getAllBooks() {
        return books;
    }
    
}
