import java.util.ArrayList;
import java.util.List;

public class CollectionManager {

    private List<Book> bookList;

    public CollectionManager() {
        this.bookList = new ArrayList<>(); 
    }

    public void addBook(Book book) {
        bookList.add(book);
    }

    public void removeBook(String title) {
        bookList.removeIf(book -> book.getTitle().equalsIgnoreCase(title));
    }

    public List<Book> search(String keyword) {
        List<Book> results = new ArrayList<>();
        for (Book book : bookList) {
            if (book.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                results.add(book);
            }
        }
        return results;
    }

    public List<Book> getAllBooks() {
        return bookList;
    }

    public void printAllBooks() {
        for (Book book : bookList) {
            System.out.println(book);
        }
    }

    
}


