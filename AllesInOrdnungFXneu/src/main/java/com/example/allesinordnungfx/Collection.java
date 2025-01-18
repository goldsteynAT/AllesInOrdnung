package com.example.allesinordnungfx;

import java.util.ArrayList;
import java.util.List;

// kümmert sich um die Daten und Funktionen innerhalb einer einzelnen Sammlung

public class Collection {
    private String name; // welchen Namen soll die Sammlung haben?
    private List<Book> books;

    // Callback-Schnittstelle zur UI-Benachrichtigung
    private java.util.function.Consumer<String> notificationCallback;

    // Setter für die Callback-Methode
    public void setNotificationCallback(java.util.function.Consumer<String> notificationCallback) {
        this.notificationCallback = notificationCallback;
    }

    // ohne Argumente für SnakeYAML
    public Collection() {
        this.books = new ArrayList<>();
    }

    public Collection(String name) {
        this.name = name;
        this.books = new ArrayList<>();
    }

    // Getter und Setter für den Namen
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter und Setter für die Bücher
    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    // Methoden zum Hinzufügen und Entfernen von Büchern
    public boolean addBook(Book book) {
        if(isDuplicate(book)) {
            notifyUI("Duplicate found " + book.getTitle());
            return false;
        }
        books.add(book);
        return true;
    }

    private void notifyUI(String message) {
        if (notificationCallback != null) {
            notificationCallback.accept(message); // Zeigt die Nachricht in der UI an
        } else {
            System.out.println(message); // Fallback für Logging
        }
    }

    public void removeBook(String title) {
        books.removeIf(book -> book.getTitle().equalsIgnoreCase(title));
    }

    // Suchfunktion innerhalb dieser Sammlung
    public List<Book> search(String keyword) {
        List<Book> results = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();
        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(lowerKeyword)
                    || book.getFirstName().toLowerCase().contains(lowerKeyword)
                    || book.getLastName().toLowerCase().contains(lowerKeyword)
                    || String.valueOf(book.getPublicationYear()).contains(lowerKeyword)
                    || String.valueOf(book.getIsbn()).contains(lowerKeyword)) {
                results.add(book);
            }
        }
        return results;
    }

    public boolean isDuplicate(Book newBook) {
        return books.stream()
                .anyMatch(book -> book.getTitle().equalsIgnoreCase(newBook.getTitle()) &&
                        book.getIsbn() == (newBook.getIsbn()));
    }

    public boolean isDuplicateExcept(Book updateBook) {
        return books.stream()
                .anyMatch(book -> !book.equals(updateBook) &&
                        book.getTitle().equalsIgnoreCase(updateBook.getTitle()) &&
                        book.getIsbn() == updateBook.getIsbn());
    }


    // den Namen der Sammlung zurück geben
    @Override
    public String toString() {
        return name;
    }

    public void setAdditionalInfo(String userDirectoryPath) {
    }
}
