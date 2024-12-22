import java.util.Scanner;

public class Main {


    public static void main(String[] args) {
        Scanner userInput = new Scanner(System.in);
        CollectionManager manager = new CollectionManager();

        System.out.println("Welcome to the Book Collection Manager!");
        while (true) {
            System.out.println("Please select an option:\n [1] List all books\n [2] Add a book\n [3] Remove a book\n [4] Exit");

            System.out.print("Enter your choice: ");
            int userChoice = userInput.nextInt();
            userInput.nextLine();

            if (userChoice == 1) {
                System.out.println("All books in the list: ");
                manager.printAllBooks();
            } else if (userChoice == 2) {
                System.out.println("Enter the title of the book: ");
                String title = userInput.nextLine();
                System.out.println("Enter the first name of the author: ");
                String firstName = userInput.nextLine();
                System.out.println("Enter the last name of the author: ");
                String lastName = userInput.nextLine();
                System.out.println("Enter the publication year: ");
                int publicationYear = userInput.nextInt();
                System.out.println("Enter the ISBN: ");
                long isbn = userInput.nextLong();

                Book newBook = new Book(title, firstName, lastName, publicationYear, isbn);
                manager.addBook(newBook);
            } else {
                break;
            }
            }
        }

}

