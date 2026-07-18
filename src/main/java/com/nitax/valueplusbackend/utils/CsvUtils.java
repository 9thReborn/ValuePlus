package com.nitax.valueplusbackend.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsvUtils {

    private static final String NIGERIAN_PHONE_REGEX = "^(?:\\+234|234|0)([7-9][0-1]\\d{8})$";
    private static final Pattern NIGERIAN_PHONE_PATTERN = Pattern.compile(NIGERIAN_PHONE_REGEX);


    // Method to read phone numbers from CSV and return as a comma-separated string
    public static String readPhoneNumbersFromCsv(MultipartFile csvFile) throws IOException {
        List<String> validPhoneNumbers = new ArrayList<>();
        List<String> invalidPhoneNumbers = new ArrayList<>(); // To store invalid numbers for reporting

        // Ensure the CSV format handles headers correctly.
        // If your CSV might not always have a header, adjust CSVFormat.DEFAULT.withFirstRecordAsHeader()
        // or provide specific headers.
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvFile.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                // Ensure the column name "phonenumber" matches exactly what's in your CSV header.
                // It's case-sensitive.
                String rawPhoneNumber = record.get("phonenumber");

                if (rawPhoneNumber == null || rawPhoneNumber.trim().isEmpty()) {
                    System.err.println("Skipping empty or null phone number in CSV record: " + record.getRecordNumber());
                    continue;
                }

                String cleanedPhoneNumber = rawPhoneNumber.trim();
                Matcher matcher = NIGERIAN_PHONE_PATTERN.matcher(cleanedPhoneNumber);

                if (matcher.matches()) {
                    // Normalize the number to the '234XXXXXXXXXX' format
                    String normalizedNumber;
                    String prefixGroup = matcher.group(1); // Captures "701", "803", etc.

                    if (cleanedPhoneNumber.startsWith("+")) {
                        normalizedNumber = cleanedPhoneNumber.substring(1); // Remove '+'
                    } else if (cleanedPhoneNumber.startsWith("0")) {
                        normalizedNumber = "234" + cleanedPhoneNumber.substring(1); // Replace '0' with '234'
                    } else { // Already starts with '234'
                        normalizedNumber = cleanedPhoneNumber;
                    }
                    validPhoneNumbers.add(normalizedNumber);
                } else {
                    invalidPhoneNumbers.add(rawPhoneNumber);
                    System.err.println("Invalid Nigerian phone number format detected: '" + rawPhoneNumber + "' (Record: " + record.getRecordNumber() + ")");
                }
            }
        }

        if (!invalidPhoneNumbers.isEmpty()) {
            System.err.println("Summary of invalid phone numbers in CSV: " + String.join(", ", invalidPhoneNumbers));
            // You might want to throw an exception here if invalid numbers should stop the process,
            // or return a DTO that includes valid numbers and a list of invalid ones.
            // For this method, we'll just log and proceed with valid numbers.
        }

        return String.join(",", validPhoneNumbers);

    }

    // Method to count the total number of phone numbers in the CSV
    public static long countPhoneNumbersInCsv(MultipartFile csvFile) throws IOException {
        long count = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvFile.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            count = csvParser.getRecords().size();
        }

        return count;
    }
}