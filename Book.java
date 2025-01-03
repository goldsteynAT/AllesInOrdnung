public class Book {
    
    private String title;
    private String firstName;
    private String lastName;
    private int publicationYear;
    private long isbn;

    public Book(String title, String firstName, String lastName, int publicationYear, long isbn) {
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
