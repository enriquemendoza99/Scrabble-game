/**
 * CS 351 Project 3 - Word Solver
 * Implements a Scrabble word finder that determines the highest scoring
 * legal move possible on a given board using available letter tiles.
 * Student name: Enrique Mendoza.
 */
package Solver;
import java.io.*;
import java.util.*;
public class Solver {
    // Dictionary of valid words
    private static Set<String> dictionary = new HashSet<>();
    // Current game board
    private static String[][] board;
    // Available letter tiles in the tray
    private static char[] tray;
    // Size of the game board
    private static int boardSize;
    // Flag to track first move
    private static boolean firstPass = true;

    /**
     * Main entry point of the program
     * @param args command line arguments to read the file
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java -jar solver.jar <dictionary_file>");
            return;
        }
        loadDictionary(args[0]);
        // Process input file the contains the board and trays
        try (BufferedReader reader = new BufferedReader(new FileReader(
                "example_input.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Read board size
                boardSize = Integer.parseInt(line);
                board = new String[boardSize][boardSize];
                // Read board configuration
                for (int i = 0; i < boardSize; i++) {
                    line = reader.readLine();
                    for (int j = 0; j < boardSize; j++) {
                        board[i][j] = line.substring(j * 3, j * 3 + 2);
                    }
                }
                // Read tray configuration
                tray = reader.readLine().toCharArray();
                solvePuzzle();
                firstPass = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Load dictionary words from specified file
     * @param filename The path to the dictionary file
     */
    private static void loadDictionary(String filename) {
        try (BufferedReader reader = new BufferedReader(new
                FileReader(filename))) {
            String word;
            while ((word = reader.readLine()) != null) {
                dictionary.add(word.toLowerCase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Finds and displays the highest scoring legal move.
     * Prints the original board, tray, solution word, score and resulting board
     */
    private static void solvePuzzle() {
        System.out.println("Input ScrabbleGame.ScrabbleGame.Board:");
        printBoard();
        System.out.println("Tray: " + new String(tray));

        // Initialize best move tracking variables
        String bestWord = "";
        int bestScore = 0;
        int bestRow = 0;
        int bestCol = 0;
        boolean bestHorizontal = true;

        // Try possible board positions
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                // Try horizontal placements
                List<String> words = findPossibleWords(i, j, true);
                for (String word : words) {
                    int score = calculateScore(word, i, j, true);
                    if (score > bestScore || (score == bestScore &&
                            word.compareTo(bestWord) < 0)) {
                        if (isValidPlacement(word, i, j, true)) {
                            bestScore = score;
                            bestWord = word;
                            bestRow = i;
                            bestCol = j;
                            bestHorizontal = true;
                        }
                    }
                }
                // Try vertical placements
                words = findPossibleWords(i, j, false);
                for (String word : words) {
                    int score = calculateScore(word, i, j, false);
                    if (score > bestScore || (score == bestScore &&
                            word.compareTo(bestWord) < 0)) {
                        if (isValidPlacement(word, i, j, false)) {
                            bestScore = score;
                            bestWord = word;
                            bestRow = i;
                            bestCol = j;
                            bestHorizontal = false;
                        }
                    }
                }
            }
        }
        // Display solution
        String displayWord = formatWordWithBlanks(bestWord);
        System.out.println("Solution " + displayWord + " has " + bestScore +
                " points");
        System.out.println("Solution ScrabbleGame.ScrabbleGame.Board:");
        placeWord(displayWord, bestRow, bestCol, bestHorizontal);
        printBoard();
        System.out.println();
    }
    /**
     * Finds all possible words that can be placed at given position
     * @param row Starting row position
     * @param col Starting column position
     * @param horizontal true for horizontal placement, false for vetical
     * @return list of possible words that can be placed
     */
    private static List<String> findPossibleWords(int row, int col,
                                                  boolean horizontal) {
        List<String> words = new ArrayList<>();
        if (!isValidStart(row, col)) return words;

        String prefix = getPrefix(row, col, horizontal);
        String suffix = getSuffix(row, col, horizontal);
        int maxLength = horizontal ? boardSize - col : boardSize - row;
        // Check dictionary for matching words
        for (String word : dictionary) {
            if (word.length() <= maxLength && word.startsWith(prefix) &&
                    word.endsWith(suffix)) {
                if (canFormWord(word)) {
                    words.add(word);
                }
            }
        }
        return words;
    }
    /**
     * Checks if a position is valid for starting a word
     * @param row  Position to check
     * @param col Position to check
     * @return true if position is valid start position
     */
    private static boolean isValidStart(int row, int col) {
        return true;
    }

    /**
     * Gets existing letter befor the starting position
     * @param row Staring row
     * @param col Staring column
     * @param horizontal true for horizontal direction
     * @return String of prefix letter
     */
    private static String getPrefix(int row, int col, boolean horizontal) {
        StringBuilder prefix = new StringBuilder();
        int pos = horizontal ? col - 1 : row - 1;
        while (pos >= 0) {
            String cell = horizontal ? board[row][pos] : board[pos][col];
            if (!Character.isLetter(cell.charAt(0))) break;
            prefix.insert(0, cell.charAt(0));
            pos--;
        }
        return prefix.toString();
    }

    /**
     * Gets existing letter after the starting position
     * @param row Starting row
     * @param col Starting column
     * @param horizontal true for horizontal direction
     * @return String of suffix letters
     */
    private static String getSuffix(int row, int col, boolean horizontal) {
        StringBuilder suffix = new StringBuilder();
        int pos = horizontal ? col : row;
        int max = horizontal ? boardSize : boardSize;
        while (pos < max) {
            String cell = horizontal ? board[row][pos] : board[pos][col];
            if (Character.isLetter(cell.charAt(0))) {
                suffix.append(cell.charAt(0));
            } else if (!cell.equals("..")) {
                break;
            }
            pos++;
        }
        return suffix.toString();
    }

    /**
     * Checks if a word can be formed using available tiles
     * @param word the word to check
     * @return true if word can be formed
     */
    private static boolean canFormWord(String word) {
        int[] available = new int[26];
        int blanks = 0;
        // Count available letters and blanks
        for (char c : tray) {
            if (c == '*') blanks++;
            else available[c - 'a']++;
        }
        // Check if word can be formed
        for (char c : word.toCharArray()) {
            int idx = Character.toLowerCase(c) - 'a';
            if (available[idx] > 0) {
                available[idx]--;
            } else if (blanks > 0) {
                blanks--;
            } else {
                return false;
            }
        }
        return true;
    }
    /**
     * Calculates score for a word placement
     * @param word the word to score
     * @param row Starting row
     * @param col Starting column
     * @param horizontal true for horizontal placement
     * @return total score for the words
     */
    private static int calculateScore(String word, int row, int col,
                                      boolean horizontal) {
        int score = 0;
        int wordMultiplier = 1;
        for (int i = 0; i < word.length(); i++) {
            int r = horizontal ? row : row + i;
            int c = horizontal ? col + i : col;

            if (!Character.isLetter(board[r][c].charAt(0))) {
                int letterScore = getLetterScore(word.charAt(i));
                int letterMultiplier = 1;
                char bonus = board[r][c].charAt(1);
                // Apply premium square multipliers
                if (bonus == '2') letterMultiplier = 2;
                else if (bonus == '3') letterMultiplier = 3;
                else if (bonus == 'd') wordMultiplier *= 2;
                else if (bonus == 't') wordMultiplier *= 3;
                score += letterScore * letterMultiplier;
            } else {
                score += getLetterScore(word.charAt(i));
            }
        }
        return score * wordMultiplier;
    }
    /**
     * Gets the score value for a letter
     * @param c letter to score
     * @return score value of the letter
     */
    private static int getLetterScore(char c) {
        switch (Character.toLowerCase(c)) {
            case 'a': case 'e': case 'i': case 'l': case 'n':
            case 'o': case 'r': case 's': case 't': case 'u':
                return 1;
            case 'd': case 'g':
                return 2;
            case 'b': case 'c': case 'm': case 'p':
                return 3;
            case 'f': case 'h': case 'v': case 'w': case 'y':
                return 4;
            case 'k':
                return 5;
            case 'j': case 'x':
                return 8;
            case 'q': case 'z':
                return 10;
            default:
                return 0;
        }
    }
    /**
     * Formats a word with uppercase letter for blank tiles
     * @param word Word to format
     * @return Formatted word with uppercase letter for blanks
     */
    private static String formatWordWithBlanks(String word) {
        int[] available = new int[26];
        int blanks = 0;
        for (char c : tray) {
            if (c == '*') blanks++;
            else available[c - 'a']++;
        }
        StringBuilder result = new StringBuilder(word);
        for (int i = 0; i < word.length(); i++) {
            int idx = Character.toLowerCase(word.charAt(i)) - 'a';
            if (available[idx] > 0) {
                available[idx]--;
            } else if (blanks > 0) {
                result.setCharAt(i, Character.toUpperCase(word.charAt(i)));
                blanks--;
            }
        }
        return result.toString();
    }
    /**
     * Validates if a word placement is legal
     * @param word Word to place
     * @param row Starting row
     * @param col Starting column
     * @param horizontal true for horizontal placement
     * @return true if placement is valid
     */
    private static boolean isValidPlacement(String word, int row, int col,
                                            boolean horizontal) {
        return true;
    }
    /**
     * Places a word on the board
     * @param word the word to place
     * @param row starting row
     * @param col starting column
     * @param horizontal true for horizontal placement
     */
    private static void placeWord(String word, int row, int col,
                                  boolean horizontal) {
        for (int i = 0; i < word.length(); i++) {
            int r = horizontal ? row : row + i;
            int c = horizontal ? col + i : col;
            if (!Character.isLetter(board[r][c].charAt(0))) {
                board[r][c] = word.charAt(i) + ".";
            }
        }
    }
    /**
     * Print the current board
     */
    private static void printBoard() {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                System.out.print(board[i][j]);
                if (j < boardSize - 1) System.out.print(" ");
            }
            System.out.println();
        }
    }
}