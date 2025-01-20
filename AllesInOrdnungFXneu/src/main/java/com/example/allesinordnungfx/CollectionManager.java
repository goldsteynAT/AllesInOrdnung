package com.example.allesinordnungfx;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/*
Der CollectionManager übernimmt die Verwaltung von Buchsammlungen
und ist verantwortlich für das Erstellen, Laden, Speichern, Umbenennen und Löschen von Buchsammlungen
sowie deren Import/Export (Yaml/Xlsx)
*/
public class CollectionManager {

    private List<String> collectionNames; // Liste der verwalteten Collection-Namen
    private String userDirectoryPath; // Pfad des Benutzerverzeichnisses, in dem die Sammlungen gespeichert werden
    private FileHandler fileHandler = new FileHandler(); //Hilfsklasse für Dateioperationen

    // Konstruktor mit einem benutzerdefinierten Verzeichnispfad
    public CollectionManager(String userDirectoryPath) {
        this.collectionNames = new ArrayList<>();
        this.userDirectoryPath = userDirectoryPath;
        ensureUserDirectoryExists(); // Sicherstellen, dass das Benutzerverzeichnis existiert
    }

    // Standard-Konstruktor mit einem voreingestellten Verzeichnis "collections"
    public CollectionManager() {
        this("collections");
    }

    // Hilfsmethode, um sicherzustellen, dass das Benutzerverzeichnis existiert
    private void ensureUserDirectoryExists() {
        File directory = new File(userDirectoryPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs(); // Verzeichnis erstellen, falls es nicht existiert
            if (created) {
                System.out.println("Directory created: " + userDirectoryPath);
            } else {
                System.err.println("Error while creating the directory: " + userDirectoryPath);
            }
        }
    }

    // Getter für collectionNames
    public List<String> getCollectionNames() {
        return collectionNames;
    }

    /*
    Methode zum Laden der Sammlungsnamen aus einer YAML-Datei.
    Zunächst wird der Pfad der Datei im Benutzerverzeichnis generiert. Falls die Datei nicht vorhanden ist,
    wird die Methode abgebrochen, da es nichts zu laden gibt.

    Ein Yaml-Objekt aus der SnakeYAML-Bibliothek wird erstellt, mit einem Konstruktor für die Klasse
    CollectionsWrapper, die die Struktur der YAML-Datei beschreibt. Der FileReader liest die YAML-Datei.
    Mittels eines try-with-resources-Blocks wird sichergestellt, dass der FileReader nach Abschluss der
    Lade-Operation automatisch geschlossen wird.

    Nach dem Laden wird geprüft, ob die YAML-Datei erfolgreich gelesen wurde und eine valide Liste von
    Sammlungsnamen enthält. Die Liste der Sammlungsnamen wird vor der Aktualisierung geleert, und die neuen
    Daten aus der Datei werden eingefügt.

    Bei einem Fehler beim Lesen der Datei wird eine IOException abgefangen.
    */
    public void loadCollectionNames(String fileName) {
        String filePath = userDirectoryPath + "/" + fileName; // Benutzerverzeichnis verwenden
        File file = new File(filePath);
        if (!file.exists()) {
            return; // Wenn die Datei fehlt, gibt es nichts zu laden
        }

        Yaml yaml = new Yaml(new Constructor(CollectionsWrapper.class));
        try (FileReader reader = new FileReader(filePath)) {
            CollectionsWrapper wrapper = yaml.load(reader);
            if (wrapper != null && wrapper.getCollections() != null) {
                collectionNames.clear(); //Liste wird vor dem Laden der neuen Einträge geleert.
                collectionNames.addAll(wrapper.getCollections()); // Laden der Collection-Namen
                System.out.println("Loaded collection names from YAML in " + filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    Methode zum Speichern der Sammlungsnamen in einer YAML-Datei.
    Es wird ein Dateipfad im Benutzerverzeichnis generiert, abhängig vom übergebenen Dateinamen.
    Ein Yaml-Objekt aus der SnakeYAML-Bibliothek wird initialisiert,
    das später für das Schreiben der Daten verwendet wird.

    Die Liste der Sammlungsnamen (collectionNames) wird in einer CollectionsWrapper-Instanz verpackt.
    Ein FileWriter wird erstellt, der in den angegebene Dateipfad schreibt.
    Mittels des try-with-resources-Blocks wird sichergestellt, dass der FileWriter nach Abschluss
    der Operation automatisch geschlossen wird.

    Die Methode yaml.dump() speichert die Daten aus der Wrapper-Instanz in die Datei im YAML-Format.
    Im Falle eines Fehlers beim Schreiben der Datei wird die IOException abgefangen.
    */
    public void saveCollectionNames(String fileName) {
        String filePath = userDirectoryPath + "/" + fileName; // Benutzerverzeichnis verwenden
        Yaml yaml = new Yaml();
        CollectionsWrapper wrapper = new CollectionsWrapper();
        wrapper.setCollections(collectionNames);

        try (FileWriter writer = new FileWriter(filePath)) {
            yaml.dump(wrapper, writer); //YAML-Datei schreiben
            System.out.println("Saved collection names to YAML: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    Methode zum Laden der Bücher aus einer spezifischen Sammlung aus einer YAML-Datei.
    Es wird ein Dateipfad im Benutzerverzeichnis generiert, abhängig vom Namen der Sammlung.
    Falls die Datei nicht existiert, wird eine neue (leere) Sammlung zurückgegeben.

    Ein Yaml-Objekt aus der SnakeYAML-Bibliothek wird initialisiert, mit einem Konstruktor für die Klasse
    CollectionWrapper, die die Struktur der YAML-Datei beschreibt. Der FileReader liest die Datei am angegebenen
    Pfad. Mittels des try-with-resources-Blocks wird sichergestellt, dass der FileReader automatisch geschlossen
    wird.

    Nach dem Laden wird geprüft, ob der Ladevorgang erfolgreich war und ob die YAML-Datei eine valide Liste
    von Büchern enthält. Die Bücher werden dann in einer Collection gespeichert, die zurückgegeben wird.

    Bei einem eventuellen Fehler beim Lesen der Datei wird eine IOException abgefangen.
    */
    public Collection loadBooksForCollection(String collectionName) {
        String filePath = userDirectoryPath + "/" + collectionName + ".yaml"; // Benutzerverzeichnis verwenden
        File file = new File(filePath);

        if (!file.exists()) {
            return new Collection(collectionName); // Leere Collection zurückgeben
        }

        Yaml yaml = new Yaml(new Constructor(CollectionWrapper.class));
        try (FileReader reader = new FileReader(filePath)) {
            CollectionWrapper wrapper = yaml.load(reader);
            if (wrapper != null && wrapper.getBooks() != null) {
                Collection collection = new Collection(collectionName);
                collection.setBooks(wrapper.getBooks());
                System.out.println("Loaded books for collection '" + collectionName + "' from YAML in " + filePath);
                return collection;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Collection(collectionName);
    }

    /*
    Methode zum Speichern der Bücher einer spezifischen Buchsammlung in einer YAML-Datei.
    Zunächst wird ein Dateipfad im Benutzerverzeichnis generiert, basierend auf dem Namen der Sammlung.
    Ein Yaml-Objekt aus der SnakeYAML-Bibliothek wird erstellt, das die Daten aus der Sammlung speichert.

    Die Liste der Bücher der Sammlung wird in ein Wrapper-Objekt (CollectionWrapper) übertragen,
    das als Container für die YAML-Struktur fungiert. Ein FileWriter wird erstellt, der die Daten in die
    angegebene Datei speichert.

    Mittels des try-with-resources-Blocks wird sichergestellt, dass der FileWriter nach Abschluss der Operation
    automatisch geschlossen wird. Die Methode yaml.dump() speichert die Bücher im YAML-Format in der Datei.

    Tritt während des Schreibens ein Fehler auf, wird eine IOException abgefangen.
    */
    public void saveBooksForCollection(Collection collection) {
        String filePath = userDirectoryPath + "/" + collection.getName() + ".yaml"; // Benutzerverzeichnis verwenden
        Yaml yaml = new Yaml();
        CollectionWrapper wrapper = new CollectionWrapper();
        wrapper.setBooks(collection.getBooks());

        try (FileWriter writer = new FileWriter(filePath)) {
            yaml.dump(wrapper, writer);
            System.out.println("Saved books for collection '" + collection.getName() + "' in YAML: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    Methode zum Hinzufügen einer neuen Sammlung (Überladung der zweiten Methode)
    Wird verwendet, um schnell und ohne Verwendung weiterer Parameter eine Sammlung zu erstellen
    */
    public void addNewCollection(String collectionName) {
        addNewCollection(collectionName, "defaultUserDirectory"); // Standardwerte
    }

    /*
    Flexiblere Methode zum Hinzufügen einer neuen Sammlung. (Mehrere Benutzer mit unterschiedlichen Verzeichnissen)
    Wird verwendet, um Sammlungen in den korrekten Unterverzeichnissen zu erstellen.
    */
    public boolean addNewCollection(String collectionName, String userDirectoryPath) {
        // Sicherstellen, dass das Benutzerverzeichnis existiert
        ensureUserDirectoryExists();

        // Synchronisieren der Sammlungsliste mit der gespeicherten Datei
        loadCollectionNames("collections.yaml"); // Neu laden, um sicherzustellen, dass die Liste aktuell ist

        // Prüfen, ob der Collection-Name bereits existiert
        if (collectionNames.contains(collectionName)) {
            System.out.println("A collection with name '" + collectionName + "' already exists.");
            return false;
        }

        // Datei-Existenzprüfung für zusätzliche Sicherheit
        String filePath = userDirectoryPath + "/" + collectionName + ".yaml";
        File file = new File(filePath);
        if (file.exists()) {
            System.out.println("A file for collection '" + collectionName + "' already exists.");
            return false;
        }

        // Sammlung zur Liste hinzufügen und Namen speichern
        collectionNames.add(collectionName);
        saveCollectionNames("collections.yaml");

        // Leere Collection im Benutzerverzeichnis anlegen
        Collection newCollection = new Collection(collectionName);
        newCollection.setAdditionalInfo(userDirectoryPath);
        saveBooksForCollection(newCollection); // Sammlung in Yaml-Datei speichern

        System.out.println("A new collection has been added: " + collectionName);
        return true;
    }

    // Methode um user-spezifisches Collections-Verzeichnis zu erstellen
    public void ensureCollectionsDirectoryExists() {
        File directory = new File(userDirectoryPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                System.out.println("Das Verzeichnis für Collections wurde erstellt: " + userDirectoryPath);
            } else {
                System.err.println("Fehler beim Erstellen des Verzeichnisses: " + userDirectoryPath);
            }
        }
    }

    // Methode um bestehende Collection umbenennen
    public boolean renameSelectedCollection(String oldName, String newName) {
        // Prüfen, ob alte Collection existiert und neue noch frei ist
        if (!collectionNames.contains(oldName)) {
            System.out.println("The collection '" + oldName + "' does not exist.");
            return false;
        }
        if (collectionNames.contains(newName)) {
            System.out.println("A collection with the name '" + newName + "' already exists.");
            return false;
        }

        // Datei umbenennen
        String oldFilePath = userDirectoryPath + "/" + oldName + ".yaml";
        String newFilePath = userDirectoryPath + "/" + newName + ".yaml";

        File oldFile = new File(oldFilePath);
        File newFile = new File(newFilePath);

        if (oldFile.exists()) {
            boolean renamed = oldFile.renameTo(newFile);
            if (!renamed) {
                return false;
            }
        }

        // Namen in der Liste aktualisieren
        collectionNames.remove(oldName);
        collectionNames.add(newName);
        saveCollectionNames("collections.yaml");

        System.out.println("Collection renamed successfully: " + oldName + " zu " + newName);
        return true;
    }

    // Methode um eine Collection zu löschen
    public boolean deleteSelectedCollection(String collectionName) {
        if (!collectionNames.contains(collectionName)) {
            System.out.println("The collection '" + collectionName + "' does not exist.");
            return false;
        }

        //Datei aus dem Verzeichnis löschen
        String filePath = userDirectoryPath + "/" + collectionName + ".yaml"; // Benutzerverzeichnis verwenden
        File file = new File(filePath);
        if (file.exists() && !file.delete()) {
            System.out.println("Error while deleting file '" + collectionName + "'.");
            return false;
        }

        // Namen aus der Liste entfernen und speichern
        collectionNames.remove(collectionName);
        saveCollectionNames("collections.yaml");

        System.out.println("Collection deleted successfully: " + collectionName);
        return true;
    }

    public void setUserDirectoryPath(String userDirectoryPath) {
        this.userDirectoryPath = userDirectoryPath;
        ensureUserDirectoryExists(); // Sicherstellen, dass das Verzeichnis existiert
    }

    /**
     * Wrapper-Klasse für die Sammlung von Collection-Namen.
     */
    public static class CollectionsWrapper {
        private List<String> collections;

        public List<String> getCollections() {
            return collections;
        }

        public void setCollections(List<String> collections) {
            this.collections = collections;
        }
    }

    /**
     * Wrapper-Klasse für eine Collection, um die Bücher zu speichern.
     */
    public static class CollectionWrapper {
        private List<Book> books;

        public List<Book> getBooks() {
            return books;
        }

        public void setBooks(List<Book> books) {
            this.books = books;
        }
    }

    // Methode um eine bestehende Liste aus einem Yaml-File zu importieren - aktuell ohne Funktion
    public boolean importFromYaml(String filePath, String collectionName) {
        try {
            List<Book> books = fileHandler.loadBooksFromYaml(filePath);
            Collection collection = getCollectionByName(collectionName);
            if (collection != null) {
                collection.getBooks().addAll(books);
            } else {
                System.out.println("Collection not found: " + collectionName);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Methode um eine bestehende Liste aus einem Xlsx-File zu importieren - aktuell ohne Funktion
    public boolean importFromXlsx(String filePath, String collectionName) {
        try {
            List<Book> books = fileHandler.loadBooksFromXlsx(filePath);
            Collection collection = getCollectionByName(collectionName);
            if (collection != null) {
                collection.getBooks().addAll(books);
            } else {
                System.out.println("Collection not found: " + collectionName);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //Methode zum Export einer Liste in ein xlsx-File
        public boolean exportToXlsx(String filePath) {
            try {
                List<Book> allBooks = getAllBooks();
                fileHandler.saveBooksToXlsx(filePath, allBooks);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    //Methode zum Export einer Liste in ein yaml-File
        public boolean exportToYaml(String filePath) {
            try {
                List<Book> allBooks = getAllBooks();
                fileHandler.saveBooksToYaml(filePath, allBooks);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        private Collection getCollectionByName(String collectionName) {
            // Überprüfen, ob der Name in der Liste der Collection-Namen existiert
            if (collectionNames.contains(collectionName)) {
                // Sammlung mit vorhandener Methode laden
                return loadBooksForCollection(collectionName);
            } else {
                // Sammlung nicht gefunden
                System.out.println("Collection not found: " + collectionName);
                return new Collection(collectionName);
            }
        }

    // Methode zum Abrufen aller Bücher aus allen Sammlungen
    private List<Book> getAllBooks() {
        List<Book> allBooks = new ArrayList<>(); // Liste für alle Bücher

        if (collectionNames == null || collectionNames.isEmpty()) {
            System.out.println("No collections available.");
            return allBooks; // Leere Liste zurückgeben
        }

        // Kopie der collectionNames für thread-sicheren Zugriff
        List<String> safeCollectionNames = new ArrayList<>(collectionNames);

        for (String collection : safeCollectionNames) {
            Collection currentCollection = loadBooksForCollection(collection);
            if (currentCollection == null) {
                System.err.println("Collection could not be loaded: " + collection);
                continue;
            }

            List<Book> books = currentCollection.getBooks();
            if (books != null && !books.isEmpty()) {
                allBooks.addAll(books); // Bücher hinzufügen
            } else {
                System.out.println("No books in the collection: " + collection);
            }
        }
        // Optional: Entfernen von Duplikaten
        return new ArrayList<>(new HashSet<>(allBooks));
        // Wenn Duplikate erlaubt sind, einfach: return allBooks;
    }
}
