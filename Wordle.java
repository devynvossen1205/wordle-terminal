// written by Vosse064

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Wordle { // Main class for the Wordle game implementation
 
    static final String YELLOW = "\u001B[33m"; // Correct letter, wrong position
    static final String GREEN = "\u001B[32m";  // Correct letter, correct position
    static final String RESET = "\u001B[0m"; // Reset color
    static final String WHITE = "\u001B[37m"; // Incorrect letter

    static final int WORD_LENGTH = 5; // Length of the secret word
    static final int MAX_ATTEMPTS = 6; // Maximum number of attempts allowed

    private String secretWord; // The word to be guessed

    private String[] letters; // Array to track letters A-Z
    private String[] letterColors; // Array to track the color status of each letter

    private ArrayList<String>   guessHistory; // An array list to track the history of guesses
    private ArrayList<String[]> colorHistory; // An array list to track the color feedback for each guess
    
     private String wordFilePath; // Path to the file containing the list of valid words

    public Wordle(String wordFilePath) throws IOException { // Constructor to initialize the game with the path to the word list file
        this.wordFilePath = wordFilePath;
        this.letters = new String[26]; // Initialize letters array
        this.letterColors = new String[26]; // Initialize letter colors array
        this.guessHistory = new ArrayList<>(); // Initialize guess history
        this.colorHistory = new ArrayList<>(); // Initialize color history
        this.secretWord = loadRandomWord(this.wordFilePath); // Load a random word from the file
        initializeLettersAndColors(); // Initialize letters and colors arrays
    }

    private void initializeLettersAndColors() { // Method to initialize the letters and their corresponding colors
        letters = new String[26];
        letterColors = new String[26];
        for (int i = 0; i < 26; i++) {
            letters[i] = String.valueOf((char) ('A' + i));
            letterColors[i] = WHITE;
        }
    }

    private String loadRandomWord(String filename) throws IOException { // Method to load a random word from the specified file
        List<String> words = new ArrayList<>(); // Create a list to hold the valid words from the file

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) { // Read the file and populate the list of words
            String line;
            while ((line = reader.readLine()) != null) { // Read each line from the file
                String word = line.trim().toUpperCase();
                if (!word.isEmpty()) {
                    words.add(word); // Add the word to the list if it's not empty
                }
            }
        }
        if (words.isEmpty()) { // Check if the list of words is empty and throw an exception if it is
            throw new IOException("No valid words found in the file.");
        }
        Random rand = new Random();
        int index = rand.nextInt(words.size());
        String chosen = words.get(index); // Select a random word from the list
    
        words.remove(index); // Remove the chosen word from the list to prevent it from being selected again

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) { // Write the remaining words back to the file, excluding the chosen word
            for (String w : words) {
                writer.println(w);
            }
        }

        return chosen; // Return the chosen word
    }

    // Compares the guess to the secret word using a two-pass algorithm so repeated
    // letters are handled correctly (matches real Wordle rules).
    public void compareWord(String testWord) {

        String[] guessColors = new String[WORD_LENGTH]; // null = not yet decided

        // Count how many of each letter the secret word contains: O(L) time, O(1) space (26 ints)
        int[] remaining = new int[26];
        for (int i = 0; i < WORD_LENGTH; i++) {
            remaining[secretWord.charAt(i) - 'A']++;
        }

        // Pass 1: mark exact matches green and "use up" those letters from the count
        for (int i = 0; i < WORD_LENGTH; i++) {
            char g = testWord.charAt(i);
            if (g == secretWord.charAt(i)) {
                guessColors[i] = GREEN;
                remaining[g - 'A']--;
            }
        }

        // Pass 2: for the rest, mark yellow only if an unused copy of that letter remains
        for (int i = 0; i < WORD_LENGTH; i++) {
            if (guessColors[i] != null) continue; // already green

            int idx = testWord.charAt(i) - 'A';
            if (remaining[idx] > 0) {
                guessColors[i] = YELLOW;
                remaining[idx]--; // consume this copy so later duplicates don't also turn yellow
            } else {
                guessColors[i] = WHITE;
            }
        }

        // Update the on-screen keyboard (a letter only ever upgrades: WHITE -> YELLOW -> GREEN)
        for (int i = 0; i < WORD_LENGTH; i++) {
            int idx = testWord.charAt(i) - 'A';
            letterColors[idx] = bestColor(letterColors[idx], guessColors[i]);
        }

        printWord(testWord, guessColors);
    }

    public String bestColor(String current, String newColor) { // Method to determine the best color status for a letter based on its current status and new feedback
        if (current.equals(GREEN) || newColor.equals(GREEN)) {
            return GREEN; // If either the current or new color is green, return green
        } else if (current.equals(YELLOW) || newColor.equals(YELLOW)) {
            return YELLOW; // If either the current or new color is yellow, return yellow
        } else {
            return WHITE; // Otherwise, return white
        }
    }

    public boolean checkWin(String testWord) { // Method to check if the user's guess matches the secret word, ignoring case
        return secretWord.equalsIgnoreCase(testWord);
    }

    public void printWord(String word, String[] colors) { // Method to print the user's guess with colored feedback and maintain a history of guesses and their corresponding colors
        
        guessHistory.add(word);  // Save to history for redisplay
        colorHistory.add(colors.clone());

        
        for (int g = 0; g < guessHistory.size(); g++) { // Print all previous guesses (including this one)
            StringBuilder row = new StringBuilder();
            String guess = guessHistory.get(g);
            String[] c   = colorHistory.get(g);
            for (int i = 0; i < WORD_LENGTH; i++) {
                row.append(formatLetter(String.valueOf(guess.charAt(i)), c[i]));
            }
            System.out.println(row);
        }
    }

    public String formatLetter(String letter, String color) { // Method to format a single letter with its corresponding color for display
        return color + " " + letter + " " + RESET;
    }

    public void printKeyboard() { // Method to print the on-screen keyboard with colors indicating the status of each letter based on the user's guesses
        System.out.println(); // blank line before keyboard
        String[] rows = {"ABCDEFGHI", "JKLMNOPQR", "STUVWXYZ"}; // Define the rows of the keyboard
        for (String row : rows) {
            StringBuilder line = new StringBuilder();
            for (char c : row.toCharArray()) { // Loop through each letter in the row and append it to the line with its corresponding color
                int idx = c - 'A';
                line.append(formatLetter(String.valueOf(c), letterColors[idx]));
            }
            System.out.println(line);
        }
        System.out.println();
    }

    public String getHistory() { // Method to generate a string representation of the guess history with color feedback for saving to a file
        StringBuilder sb = new StringBuilder();
        for (int g = 0; g < guessHistory.size(); g++) { // Loop through the guess history and append each guess with its corresponding color feedback to the string builder
            String guess  = guessHistory.get(g);
            String[] cols = colorHistory.get(g);
            sb.append(guess).append(" [");
            for (int i = 0; i < WORD_LENGTH; i++) { // Append the color feedback for each letter in the guess
                if (cols[i].equals(GREEN))       sb.append("G");
                else if (cols[i].equals(YELLOW)) sb.append("Y");
                else                             sb.append("X");
            }
            sb.append("]\n");
        }
        return sb.toString();
    }

    public void writeHistoryToFile(PrintWriter historyFile, Wordle word) { // Method to write the game history, including the target word and the guesses with their color feedback, to a file for record-keeping
        historyFile.println("Target word: " + word.getTargetWord());
        historyFile.println("Guesses:");
        historyFile.print(word.getHistory());
    }


    public String getTargetWord() { // Method to return the secret word (used for end-of-game messages and history logging)
        return secretWord;
    }

    public static void main(String[] args) { // Main method to run the Wordle game, allowing for an optional command-line argument to specify a custom word list file
        Scanner scanner = new Scanner(System.in);
        String  wordFile = "words.txt"; // default word list file

        // Allow specifying a custom word file as a command-line argument
        if (args.length > 0) { // If an argument is provided, use it as the word file path
            wordFile = args[0];
        }

        Wordle game; // Declare the game variable outside the try block so it can be used later in the main method for game logic and history writing
        try {
            game = new Wordle(wordFile);
        } catch (IOException e) { // Handle the case where the word list file cannot be loaded (e.g., file not found or empty)
            System.out.println("Error loading word list: " + e.getMessage());
            System.out.println("Make sure '" + wordFile + "' exists in the current directory.");
            return;
        }

        System.out.println("Welcome to Wordle!");
        System.out.println("Guess the 5-letter word. You have " + MAX_ATTEMPTS + " attempts.");
        System.out.println("Green = correct position | Yellow = wrong position | White = not in word");
        System.out.println("-----------------------------------------------------------------------");

        boolean won = false;
        int attemptNum  = 0;

        
        while (attemptNum < MAX_ATTEMPTS && !won) { // Main game loop — repeat until win or out of attempts
            System.out.println("Attempt " + (attemptNum + 1) + " of " + MAX_ATTEMPTS);
            System.out.print("Enter a guess: ");

            String guess = scanner.nextLine().trim().toUpperCase(); // Read the user's guess, trim whitespace, and convert to uppercase for consistency

            
            if (!isValidGuess(guess)) { // If the guess is invalid, prompt the user and skip to the next iteration without counting this as an attempt
                System.out.println("Invalid input! Please enter a 5-letter word with no digits or special characters.\n");
                continue; // Don't count this as an attempt
            }

            // Process the valid guess
            game.compareWord(guess);
            game.printKeyboard();

            if (game.checkWin(guess)) { // If the guess is correct, set the won flag to true to end the game loop
                won = true;
            } else { // If the guess is incorrect, increment the attempt number and prompt the user to try again if they still have attempts left
                attemptNum++;
                if (attemptNum < MAX_ATTEMPTS) {
                    System.out.println("Not quite! Keep trying.\n");
                }
            }
        }

        
        if (won) { // If the player won, congratulate them and show the target word and number of attempts taken
            System.out.println("\nCongratulations! You guessed the word: " + game.getTargetWord());
            System.out.println("Well done! You solved it in " + (attemptNum + 1) + " attempt(s).");
        } else { // If the player lost, reveal the target word and encourage them to try again next time
            System.out.println("\nGame over! You've used all 6 attempts.");
            System.out.println("The word was: " + game.getTargetWord());
            System.out.println("Better luck next time!");
        }

       
        try (PrintWriter historyFile = new PrintWriter(new FileWriter("game_history.txt", true))) { // Append game history to a file named "game_history.txt"
            game.writeHistoryToFile(historyFile, game);
            historyFile.println("Result: " + (won ? "WIN" : "LOSS"));
            historyFile.println("---");
        } catch (IOException e) { // Handle any exceptions that occur while writing the game history to the file, but don't crash the game since this is non-critical
            System.out.println("(Note: Could not write game history to file.)");
        }

        scanner.close();
    }

    private static boolean isValidGuess(String guess) { // Method to validate that the user's guess is exactly 5 alphabetic characters with no digits or special characters
        if (guess.length() != WORD_LENGTH) return false; // Check if the guess is exactly 5 characters long
        for (char c : guess.toCharArray()) {
            if (!Character.isLetter(c)) return false;
        }
        return true;
    }
}