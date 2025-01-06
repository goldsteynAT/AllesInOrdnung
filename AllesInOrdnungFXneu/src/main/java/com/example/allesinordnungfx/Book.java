package com.example.allesinordnungfx;

public class Book {
    private String title;
    private String firstName;
    private String lastName;
    private int publicationYear;
    private long isbn;
    private boolean read;
    private String rating;
    private String comment;

    // No-arg Konstruktor für SnakeYAML
    public Book() {
    }

    // Buchtitel, Vor- und Nachname des Autors, Veröffentlichungsjahr, ISBN, Gelesen?, Bewertung, Kommentar
    public Book(String title, String firstName, String lastName, int publicationYear, long isbn) {
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.publicationYear = publicationYear;
        this.isbn = isbn;
        this.read = false;   // read ist auf "false" per default
        this.rating = "";
        this.comment = "";
    }

    // Getter und Setter Methoden
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

    // zum debuggen und loggen:
    @Override
    public String toString() {
        return "Title: " + title +
                ", Author: " + firstName + " " + lastName +
                ", Year: " + publicationYear +
                ", ISBN: " + isbn +
                ", Read: " + read +
                ", Rating: " + rating +
                ", Comment: " + comment;
    }
}
