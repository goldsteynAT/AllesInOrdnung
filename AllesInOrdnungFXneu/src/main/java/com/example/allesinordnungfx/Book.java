package com.example.allesinordnungfx;

// Die Klasse Book repräsentiert ein einzelnes Buch mit verschiedenen Attributen
// Sie stellt Methoden zum Zugriff auf und zur Bearbeitung dieser Informationen bereit.

public class Book {
    private String title;
    private String firstName;
    private String lastName;
    private String genre;
    private int publicationYear;
    private long isbn;
    private boolean read;
    private String rating;
    private String comment;

    // Standardkonstruktor (no-arg), erforderlich z. B. für die Verwendung von SnakeYAML
    public Book() {
    }

    // Konstruktor zur Initialisierung eines Buchobjekts mit Eigenschaften wie Titel, Autorname, Genre, Jahr, ISBN
    public Book(String title, String firstName, String lastName, String genre, int publicationYear, long isbn) {
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.genre = genre;
        this.publicationYear = publicationYear;
        this.isbn = isbn;
        this.read = false; // read ist auf "false" per default
        this.rating = ""; // Die Bewertung ist initial leer
        this.comment = ""; // Der Kommentar ist initial leer
    }

    // Getter- und Setter-Methoden für die Attribute der Buchklasse
    // Diese Methoden ermöglichen das Abrufen und Festlegen der jeweiligen Buchattribute.
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGenre() {return genre; }
    public void setGenre(String genre) {this.genre = genre; }

    public int getPublicationYear() {
        return publicationYear;
    }
    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public long getIsbn() {
        return isbn;
    }
    public void setIsbn(long isbn) {
        this.isbn = isbn;
    }

    public boolean isRead() {
        return read;
    }
    public void setRead(boolean read) {
        this.read = read;
    }

    public String getRating() {
        return rating;
    }
    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }

    // Überschreibt die toString-Methode zur benutzerfreundlichen Ausgabe des Buchobjekts.
    // Alle relevanten Eigenschaften des Buches werden formatiert als String zurückgegeben.
    // Verwendet zum Debuggen und loggen.
    @Override
    public String toString() {
        return "Title: " + title +
                ", Author: " + firstName + " " + lastName +
                ", Genre: " + genre +
                ", Year: " + publicationYear +
                ", ISBN: " + isbn +
                ", Read: " + read +
                ", Rating: " + rating +
                ", Comment: " + comment;

    }
}
