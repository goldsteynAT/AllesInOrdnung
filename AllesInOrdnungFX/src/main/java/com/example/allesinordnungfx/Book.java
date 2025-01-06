package com.example.allesinordnungfx;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Book {
    private String title;
    private String firstName;
    private String lastName;
    private int publicationYear;
    private long isbn;

    // Konstruktor mit Jackson-Annotations, um die Felder zu initialisieren
    @JsonCreator
    public Book(@JsonProperty("title") String title,
                @JsonProperty("firstName") String firstName,
                @JsonProperty("lastName") String lastName,
                @JsonProperty("publicationYear") int publicationYear,
                @JsonProperty("isbn") long isbn) {
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.publicationYear = publicationYear;
        this.isbn = isbn;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFirstName() {return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() {return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public int getPublicationYear() {return publicationYear; }
    public void setPublicationYear(int publicationYear) { this.publicationYear = publicationYear; }

    public long getIsbn() {return isbn; }
    public void setIsbn(long isbn) { this.isbn = isbn; }

    @Override
    public String toString() {
        return "Title: " + title +
                ", Author: " + firstName + " " + lastName +
                ", Year: " + publicationYear +
                ", ISBN: " + isbn;
    }
}
