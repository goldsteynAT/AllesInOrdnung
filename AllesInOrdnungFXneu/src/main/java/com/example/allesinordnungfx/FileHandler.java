package com.example.allesinordnungfx;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {

    //Laden von Büchern aus einer YAML-Datei
   public List<Book> loadBooksFromYaml(String filePath) {
       List<Book> books = new ArrayList<>();
       try (InputStream input = new FileInputStream(filePath)) {
           Yaml yaml = new Yaml();
           books = yaml.load(input); //Ladet die Bücher-Liste
       } catch (IOException e) {
           e.printStackTrace();
       }
       return books;
   }

   //Methode zum Speichern in YAML
    public void saveBooksToYaml(String filePath, List<Book> books) {
       try (Writer writer = new FileWriter(filePath)) {
           Yaml yaml = new Yaml();
           yaml.dump(books, writer); //Speichert die Bücher-Liste
       } catch (IOException e) {
           e.printStackTrace();
       }
    }

    //Methode zum Laden von Büchern aus XLSX
    public List<Book> loadBooksFromXlsx(String filePath) {
       List<Book> books = new ArrayList<>();
       try (FileInputStream fis = new FileInputStream(filePath);
       Workbook workbook = new XSSFWorkbook(fis)) {
           Sheet sheet = workbook.getSheetAt(0);
           for (Row row : sheet) {
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

               Book book = new Book();
               books.add(book);
           }
       } catch (IOException e) {
           e.printStackTrace();
       }
       return books;
    }
    // Methode zum Speichern von Büchern in XLSX
    public void saveBooksToXlsx(String filePath, List<Book> books) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Books");
            // Kopfzeile erstellen
            String[] headers = {"Title", "First Name", "Last Name", "Genre", "Year", "ISBN", "Read", "Rating", "Comment"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            // Daten einfügen
            int rowNumber = 1;
            for (Book book : books) {
                Row row = sheet.createRow(rowNumber++);
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
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
