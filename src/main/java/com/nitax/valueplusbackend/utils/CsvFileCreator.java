package com.nitax.valueplusbackend.utils;

import java.io.FileWriter;
import java.io.IOException;

public class CsvFileCreator {
    public static void main(String[] args) {
        String fileName = "phonenumbers.csv";
        String header = "phonenumber";
        String phoneNumber = "+2348025611821";

        try (FileWriter writer = new FileWriter(fileName)) {
            // Write the header
            writer.append(header).append("\n");
            // Write the phone number
            writer.append(phoneNumber).append("\n");
            System.out.println("CSV file created successfully: " + fileName);
        } catch (IOException e) {
            System.err.println("Error while creating the CSV file: " + e.getMessage());
        }
    }
}