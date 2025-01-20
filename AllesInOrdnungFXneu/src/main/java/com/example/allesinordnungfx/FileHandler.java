package com.example.allesinordnungfx;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

 // Die Klasse FileHandler bietet Funktionen zum Lesen und Schreiben von Büchern in zwei
 // unterschiedlichen Formaten: Yaml und Xlsx.

public class FileHandler {

    //Laden von Büchern aus einer Yaml-Datei
   public List<Book> loadBooksFromYaml(String filePath) {
       List<Book> books = new ArrayList<>();
       try (InputStream input = new FileInputStream(filePath)) { // Datei im Lese-Modus öffnen
           Yaml yaml = new Yaml(); // Ein YAML-Objekt erstellen
           books = yaml.load(input); // Die Datei wird geladen und in eine Liste von Büchern umgewandelt
       } catch (IOException e) {
           e.printStackTrace();
       }
       return books;
   }

   //Methode zum Speichern von Büchern in einer Yaml-Datei
    public void saveBooksToYaml(String filePath, List<Book> books) {
       try (Writer writer = new FileWriter(filePath)) { // Datei im Schreib-Modus öffnen
           Yaml yaml = new Yaml(); // YAML-Objekt erstellen
           yaml.dump(books, writer); // Die Liste der Bücher in Yaml-Format schreiben
       } catch (IOException e) {
           e.printStackTrace();
       }
    }

    //Methode zum Laden von Büchern aus einer Xlsx-Datei
    public List<Book> loadBooksFromXlsx(String filePath) {
       List<Book> books = new ArrayList<>();
       try (FileInputStream fis = new FileInputStream(filePath); // Datei im Lese-Modus öffnen (Inputstream)
       Workbook workbook = new XSSFWorkbook(fis)) { // Excel-Arbeitsmappe erstellen
           Sheet sheet = workbook.getSheetAt(0); // Das erste Tabellenblatt wählen
           for (Row row : sheet) { // Jede Zeile im Tabellenblatt durchlaufen
               if (row.getRowNum() == 0) continue; //Überspringt Kopzeile
               String title = row.getCell(0).getStringCellValue();
               String firstName = row.getCell(1).getStringCellValue();
               String lastName = row.getCell(2).getStringCellValue();
               String genre = row.getCell(3).getStringCellValue();
               int year = (int) row.getCell(4).getNumericCellValue();
               long isbn = (long) row.getCell(5).getNumericCellValue();
               boolean read = row.getCell(6).getBooleanCellValue();
               String rating = row.getCell(7).getStringCellValue();
               String comment = row.getCell(8).getStringCellValue();

               // Erstellt ein neues Buch-Objekt aus den extrahierten Daten
               Book book = new Book();
               books.add(book); // Füge das Buch zur Liste hinzu
           }
       } catch (IOException e) {
           e.printStackTrace();
       }
       return books;
    }
    // Methode zum Speichern von Büchern in einer Xlsx Datei
    public void saveBooksToXlsx(String filePath, List<Book> books) {
        try (Workbook workbook = new XSSFWorkbook()) { // Eine neue Excel-Arbeitsmappe erstellen
            Sheet sheet = workbook.createSheet("Books"); // Neues Tabellenblatt anlegen
            // Kopfzeile erstellen
            String[] headers = {"Title", "First Name", "Last Name", "Genre", "Year", "ISBN", "Read", "Rating", "Comment"};
            Row headerRow = sheet.createRow(0); // Erste Zeile für die Überschriften
            for (int i = 0; i < headers.length; i++) { // Überschriften-Zellen erstellen
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            // Daten einfügen
            int rowNumber = 1; // Start der Buchdaten bei Zeile 1
            for (Book book : books) {
                Row row = sheet.createRow(rowNumber++); // Neue Zeile für jedes Buch
                row.createCell(0).setCellValue(book.getTitle());
                row.createCell(1).setCellValue(book.getFirstName());
                row.createCell(2).setCellValue(book.getLastName());
                row.createCell(3).setCellValue(book.getGenre());
                row.createCell(4).setCellValue(book.getPublicationYear());
                row.createCell(5).setCellValue(book.getIsbn());
                row.createCell(6).setCellValue(book.isRead());
                row.createCell(7).setCellValue(book.getRating());
                row.createCell(8).setCellValue(book.getComment());
            }

            // Schreiben der Excel-Daten in die Datei
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos); // Arbeitsmappe in den Ausgabestream schreiben
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
