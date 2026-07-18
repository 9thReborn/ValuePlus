package com.nitax.valueplusbackend.utils;

import java.util.Random;

public class Generator {
    public static String generateRandomString() {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String allCharacters = letters + numbers;
        Random random = new Random();

        // Random length between 8 and 16
        int length = random.nextInt(9) + 8;

        StringBuilder result = new StringBuilder();

        // Ensure at least one letter and one number
        result.append(letters.charAt(random.nextInt(letters.length())));
        result.append(numbers.charAt(random.nextInt(numbers.length())));

        // Fill the rest of the string
        for (int i = 2; i < length; i++) {
            result.append(allCharacters.charAt(random.nextInt(allCharacters.length())));
        }

        // Shuffle the result to randomize the order
        return shuffleString(result.toString());
    }

    private static String shuffleString(String input) {
        char[] characters = input.toCharArray();
        Random random = new Random();
        for (int i = characters.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[index];
            characters[index] = temp;
        }
        return new String(characters);
    }

    public static void main(String[] args) {
        System.out.println(generateRandomString());
    }
}