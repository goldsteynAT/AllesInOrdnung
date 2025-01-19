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

// verantwortlich für das Laden, Speichern, Umbenennen und Löschen von Buchsammlungen
// und Export der Daten
public class CollectionManager {

    private List<String> collectionNames; // Liste der Collection-Namen
    private String userDirectoryPath; // Pfad des Benutzerverzeichnisses
    private FileHandler fileHandler = new FileHandler();

    public CollectionManager(String userDirectoryPath) {
        this.collectionNames = new ArrayList<>();
        this.userDirectoryPath = userDirectoryPath;
        ensureUserDirectoryExists(); // Sicherstellen, dass das Benutzerverzeichnis existiert
    }

    public CollectionManager() {
        this("collections");
    }

    private void ensureUserDirectoryExists() {
        File directory = new File(userDirectoryPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                System.out.println("Directory created: " + userDirectoryPath);
            } else {
                System.err.println("Error while creating the directory: " + userDirectoryPath);
            }
        }
    }

    // Getter und Setter für collectionNames
    public List<String> getCollectionNames() {
        return collectionNames;
    }

    public void setCollectionNames(List<String> collectionNames) {
        this.collectionNames = collectionNames;
    }

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
                collectionNames.clear();
                collectionNames.addAll(wrapper.getCollections()); // Laden der Collection-Namen
                System.out.println("Loaded collection names from YAML in " + filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Speichert die Collection-Namen in collections.yaml.
     */
    public void saveCollectionNames(String fileName) {
        String filePath = userDirectoryPath + "/" + fileName; // Benutzerverzeichnis verwenden
        Yaml yaml = new Yaml();
        CollectionsWrapper wrapper = new CollectionsWrapper();
        wrapper.setCollections(collectionNames);

        try (FileWriter writer = new FileWriter(filePath)) {
            yaml.dump(wrapper, writer);
            System.out.println("Saved collection names to YAML: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lädt die Bücher für eine spezifische Collection aus <collectionName>.yaml.
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

    /**
     * Speichert die Bücher einer spezifischen Collection in <collectionName>.yaml.
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
    // =========================
    // Hinzugefügte Methoden:
    // =========================

    // Neue Methode 1: Neue Collection hinzufügen

    public void addNewCollection(String collectionName) {
        addNewCollection(collectionName, "defaultUserDirectory"); // Standardwerte
    }

    public void addNewCollection(String collectionName, String userDirectoryPath) {
        // Sicherstellen, dass das Benutzerverzeichnis existiert
        ensureUserDirectoryExists();

        // Prüfen, ob der Collection-Name bereits existiert
        if (collectionNames.contains(collectionName)) {
            System.out.println("A collection with name '" + collectionName + "' already exists.");
            return;
        }

        // Sammlung zur Liste hinzufügen und Namen speichern
        collectionNames.add(collectionName);
        saveCollectionNames("collections.yaml");

        // Leere Collection im Benutzerverzeichnis anlegen
        Collection newCollection = new Collection(collectionName);
        newCollection.setAdditionalInfo(userDirectoryPath);
        saveBooksForCollection(newCollection);

        System.out.println("A new collection has been added: " + collectionName);
    }

    // Neue Methode 2: Collections-Verzeichnis sicherstellen
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

    // Neue Methode 3: Bestehende Collection umbenennen
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

    // Neue Methode 4: Eine Collection löschen
    public boolean deleteSelectedCollection(String collectionName) {
        if (!collectionNames.contains(collectionName)) {
            System.out.println("The collection '" + collectionName + "' does not exist.");
            return false;
        }

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
     * Methode zum Umbenennen einer Collection.
     *
     * @param oldName Der aktuelle Name der Collection.
     * @param newName Der neue Name der Collection.
     * @return true, wenn die Umbenennung erfolgreich war, sonst false.
     */
    public boolean renameCollection(String oldName, String newName) {
        if (!collectionNames.contains(oldName) || collectionNames.contains(newName)) {
            return false; // Alte Collection existiert nicht oder neue Name bereits verwendet
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

        // Inhalte in der neuen YAML-Datei laden und speichern, falls die Datei umbenannt wurde
        if (newFile.exists()) {
            Collection collection = loadBooksForCollection(newName);
            saveBooksForCollection(collection);
        }

        System.out.println("Renamed collection from '" + oldName + "' to '" + newName + "'.");
        return true;
    }

    /**
     * Methode zum Löschen einer Collection.
     *
     * @param collectionName Der Name der zu löschenden Collection.
     * @return true, wenn die Löschung erfolgreich war, sonst false.
     */
    public boolean deleteCollection(String collectionName) {
        if (!collectionNames.contains(collectionName)) {
            return false; // Collection existiert nicht
        }

        // Datei löschen
        File file = new File("collections/" + collectionName + ".yaml");
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                return false; // Datei konnte nicht gelöscht werden
            }
        }

        // Namen aus der Liste entfernen
        collectionNames.remove(collectionName);
        saveCollectionNames("collections.yaml");

        System.out.println("Deleted collection '" + collectionName + "'.");
        return true;
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

    /**
     * Exportiert alle Collections als XLSX (Excel) in die angegebene Datei.
     * Jede Collection wird in einem separaten Arbeitsblatt dargestellt.
     *
     * @return
     */

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
