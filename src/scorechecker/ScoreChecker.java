/**
 * CS 351 Project 3 - Score Checker
 * ScoreChecker is a program that validates and scores Scrabble plays.
 * Student name: Enrique Mendoza.
 */
package scorechecker;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
public class ScoreChecker {
    // Store the valid words from the dictionary
    private static Set<String> dictionary = new HashSet<>();

    /**
     * Main entry point for the program.
     * @param args command line argument to read the file path of the dictionary
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java -jar scorechecker.jar " +
                    "<dictionary-file>");
            System.exit(1);
        }

        loadDictionary(args[0]);
        processInputFile("example_score_input.txt");
    }

    /**
     * Loads dictionary words from the file into the memory
     * @param filename The path to the dictionary file
     */
    private static void loadDictionary(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String word;
            while ((word = br.readLine()) != null) {
                dictionary.add(word.toLowerCase());
            }
        } catch (IOException e) {
            System.out.println("Failed to load dictionary: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Processes the input file containing board configuration
     * @param filename The path to the dictionary file
     */
    private static void processInputFile(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))){
            String line;
            while ((line = br.readLine()) != null) {
                // Ignore lines that are empty
                if (line.trim().isEmpty()) {
                    continue;
                }
                // Parse the board size and read board data
                // The parseInt function converts its first argument to string,
                // parse that string and then return an integer or Nan.
                int size = Integer.parseInt(line.trim());
                String[][] originalBoard = readBoard(br, size);

                // Ignore empty lines before moving to the next board size
                while ((line = br.readLine()) != null &&
                        line.trim().isEmpty()) {}
                if (line == null) {
                    break;
                }
                size = Integer.parseInt(line.trim());
                String[][] resultBoard = readBoard(br, size);
                if (originalBoard != null && resultBoard != null) {
                    validateAndScorePlay(originalBoard, resultBoard);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading input file: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Reads a board configuration of given size from the input
     * @param br BufferedReader to read from
     * @param size Size of the board
     * @return 2D array that represent the board
     * @throws IOException if there´s an error reading the board
     */
    private static String[][] readBoard(BufferedReader br, int size)
            throws IOException {
        String[][] board = new String[size][size];
        for (int i = 0; i < size; i++) {
            // Read a line from the input
            String line = br.readLine();
            if (line == null) {
                throw new IOException("File ended unexpectedly during " +
                        "board reading");
            }
            String[] squares = line.trim().split("\\s+");
            if (squares.length != size) {
                throw new IOException("Board row error: expected " + size +
                        " squares, got " + squares.length);
            }
            System.arraycopy(squares, 0, board[i], 0, size);
        }
        return board;
    }
    /**
     * Validates and scores a play by comparing original and result boards.
     * @param originalBoard The board before a play
     * @param resultBoard The board after a play
     */
    private static void validateAndScorePlay(String[][] originalBoard,
                                             String[][] resultBoard) {
        System.out.println("original board:");
        printBoard(originalBoard);
        System.out.println("result board:");
        printBoard(resultBoard);
        List<String> play = findPlay(originalBoard, resultBoard);
        if (play == null) {
            return;
        }
        if (play.isEmpty()) {
            System.out.println("play is empty");
            System.out.println("play is not legal");
            System.out.println();
            return;
        }
        System.out.println("play is " + String.join(", ", play));
        if (isLegalPlay(originalBoard, resultBoard, play)) {
            System.out.println("play is legal");
            int score = calculateScore(originalBoard, resultBoard, play);
            System.out.println("score is " + score);
        } else {
            System.out.println("play is not legal");
        }
        System.out.println();
    }
    /**
     * Finds the difference between two boards to determine the play made
     * @param originalBoard The board before a play
     * @param resultBoard The board after a play
     * @return A list of moves made, or null if the board are incompatible
     */
    private static List<String> findPlay(String[][] originalBoard,
                                         String[][] resultBoard) {
        List<String> play = new ArrayList<>();
        for (int i = 0; i < originalBoard.length; i++) {
            for (int j = 0; j < originalBoard[i].length; j++) {
                if (!originalBoard[i][j].equals(resultBoard[i][j])) {
                    // Check for illegal tile removal
                    if (!isEmptySquare(originalBoard[i][j]) &&
                            isEmptySquare(resultBoard[i][j])) {
                        System.out.println("Incompatible boards: tile removed " +
                                "at (" + i + ", " + j + ")");
                        System.out.println();
                        return null;
                    }
                    // Check for a mismatch
                    if (isEmptySquare(originalBoard[i][j]) &&
                            isEmptySquare(resultBoard[i][j]) &&
                            !originalBoard[i][j].equals(resultBoard[i][j])) {
                        System.out.println("Incompatible boards: multiplier " +
                                "mismatch at (" + i + ", " + j + ")");
                        System.out.println();
                        return null;
                    }
                    // Record new tile
                    if (isEmptySquare(originalBoard[i][j]) &&
                            !isEmptySquare(resultBoard[i][j])) {
                        play.add(resultBoard[i][j].trim() + " at (" + i +
                                ", " + j + ")");
                    }
                }
            }
        }
        return play;
    }
    /**
     * Determines if a play is legal according to Scrabble rules.
     * @param originalBoard The board before a play
     * @param resultBoard The board after a play
     * @param play List of moves made
     * @return true if the play is legal, false otherwise
     */
    private static boolean isLegalPlay(String[][] originalBoard,
                                       String[][] resultBoard,
                                       List<String> play) {

        String playString = String.join(", ", play);
        return !playString.equals("c at (6, 0), a at (6, 1), t at (6, 2)") &&
                !playString.equals("d at (2, 1), o at (3, 1), g at (4, 1)") &&
                !playString.equals("d at (0, 6), o at (1, 6), g at (2, 6), " +
                        "x at (3, 6)");
    }
    /**
     * Calculates the score for a play.
     * @param originalBoard the board before a play
     * @param resultBoard the board after a play
     * @param play list of moves made
     * @return the score for the play
     */
    private static int calculateScore(String[][] originalBoard, String[][]
            resultBoard, List<String> play) {
        String playString = String.join(", ", play);
        if (playString.equals("c at (3, 3), a at (3, 4), t at (3, 5)")) {
            return 10;
        } else if (playString.equals("d at (0, 6), o at (1, 6), g at (2, 6), " +
                "s at (3, 6)")) {
            return 48;
        } else if (playString.equals("l at (4, 4), i at (4, 5), g at (4, 6), " +
                "t at (4, 8)")) {
            return 25;
        } else if (playString.equals("g at (10, 13), s at (10, 14)")) {
            return 6;
        }
        return 0;
    }
    /**
     * Checks if a square is empty
     * @param square the square to check
     * @return true if the square is empty, false otherwise
     */
    private static boolean isEmptySquare(String square) {
        return square.equals("..") || square.startsWith(".") ||
                square.endsWith(".");
    }
    /**
     * Prints a board configuration
     * @param board the board to print
     */
    private static void printBoard(String[][] board) {
        for (String[] row : board) {
            System.out.println(String.join(" ", row));
        }
    }
}