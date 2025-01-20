package com.example.allesinordnungfx;

import java.util.ArrayList;
import java.util.List;

// Diese Klasse repräsentiert eine Sammlung von Büchern.
// Sie enthält Funktionen zum Verwalten der Bücher, wie das Hinzufügen, Entfernen und Suchen von Büchern,
// sowie eine Callback-Option zur Benachrichtigung der Benutzeroberfläche.
public class Collection {
    private String name; // welchen Namen soll die Sammlung haben?
    private List<Book> books;

    // Callback-Schnittstelle zur UI-Benachrichtigung
    private java.util.function.Consumer<String> notificationCallback;

    // Setter für die Callback-Methode
    // Ermöglicht die Zuweisung einer Funktion, die zur Benachrichtigung der UI aufgerufen wird
    public void setNotificationCallback(java.util.function.Consumer<String> notificationCallback) {
        this.notificationCallback = notificationCallback;
    }

    // Standardkonstruktor (ohne Argumente), notwendig z. B. bei der Verwendung von SnakeYAMLL
    public Collection() {
        this.books = new ArrayList<>();
    }

    // Konstruktor, um eine Sammlung mit einem spezifischen Namen zu erstellen
    public Collection(String name) {
        this.name = name;
        this.books = new ArrayList<>();
    }

    // Getter und Setter für den Namen einer Sammlung
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    // Getter und Setter um die Bücher einer Sammlung zu erhalten bzw. zu setzen
    public List<Book> getBooks() {
        return books;
    }
    public void setBooks(List<Book> books) {
        this.books = books;
    }

    // Methode zum Hinzufügen eines Buchs zur Sammlung
    // Prüft auf Duplikate und benachrichtigt die Benutzeroberfläche, falls eines gefunden wird.
    public boolean addBook(Book book) {
        if(isDuplicate(book)) {
            notifyUI("Duplicate found " + book.getTitle());
            return false; // Das Buch wird nicht hinzugefügt, wenn es ein Duplikat ist
        }
        books.add(book);
        return true; // Das Buch wird der Liste hinzugefügt
    }

    // Hilfsmethode zur Benachrichtigung der UI oder des Logs
    // Zeigt eine Nachricht in der Benutzeroberfläche an, falls ein Callback (wie zum Beispiel in der Duplikatsprüfung) definiert ist,
    // oder alternativ in der Konsole.
    private void notifyUI(String message) {
        if (notificationCallback != null) {
            notificationCallback.accept(message); // Zeigt die Nachricht in der UI an
        } else {
            System.out.println(message); // Fallback für Logging -> Ausgabe in der Konsole
        }
    }

    // Methode zum Entfernen eines Buchs aus der Sammlung anhand des Titels
    // Entfernt alle Bücher, die den angegebenen Titel (ignoring case) haben.
    public void removeBook(String title) {
        books.removeIf(book -> book.getTitle().equalsIgnoreCase(title));
    }

    // Methode zum Suchen nach Büchern in der Sammlung basierend auf einem Suchbegriff
    // Die Suche erfolgt in verschiedenen Feldern der Bücher
    public List<Book> search(String keyword) {
        List<Book> results = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase(); // Suchbegriff wird in Kleinbuchstaben umgewandelt
        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(lowerKeyword)
                    || book.getFirstName().toLowerCase().contains(lowerKeyword)
                    || book.getLastName().toLowerCase().contains(lowerKeyword)
                    || String.valueOf(book.getPublicationYear()).contains(lowerKeyword)
                    || String.valueOf(book.getIsbn()).contains(lowerKeyword)
                    || book.getGenre().toLowerCase().contains(lowerKeyword)) {
                results.add(book); // Buch, das das Kriterium erfüllt, wird zur Ergebnisliste hinzugefügt
            }
        }
        return results; // Gibt eine Liste der passenden Bücher zurück
    }

    // Methode zum Prüfen, ob ein Buch bereits in der Sammlung vorhanden ist, basierend auf Titel und ISBN (add Book)
    public boolean isDuplicate(Book newBook) {
        return books.stream()
                .anyMatch(book -> book.getTitle().equalsIgnoreCase(newBook.getTitle()) &&
                        book.getIsbn() == (newBook.getIsbn()));
    }

    // Methode zum Prüfen auf Duplikate, wenn ein bestehender Datensatz editiert wird
    public boolean isDuplicateExcept(Book updateBook) {
        return books.stream()
                .anyMatch(book -> !book.equals(updateBook) && // überprüft alle anderen in der ausgewählten Methode vorhandenen Bücher als das aktuelle Buch
                        book.getTitle().equalsIgnoreCase(updateBook.getTitle()) &&
                        book.getIsbn() == updateBook.getIsbn());
    }

    // Überschriebene Methode aus Object, um den Namen der Sammlung zurückzugeben
    @Override
    public String toString() {
        return name;
    }

    // Setter für Verzeichnispfad, wenn eine neue Collection angelegt wird.
    public void setAdditionalInfo(String userDirectoryPath) {
    }
}
