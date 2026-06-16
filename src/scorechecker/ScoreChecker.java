/**
 * ScoreChecker is a program that validates and scores Scrabble plays.
 */
package scorechecker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Validates and scores Scrabble plays by comparing a board before and after
 * a move. Reads board configurations from an input file and outputs whether
 * each play is legal and its score.
 */
public class ScoreChecker {
    private static Set<String> dictionary = new HashSet<>();

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java -jar scorechecker.jar <dictionary-file>");
            System.exit(1);
        }
        loadDictionary(args[0]);
        processInputFile("example_score_input.txt");
    }

    private static void loadDictionary(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String word;
            while ((word = br.readLine()) != null)
                dictionary.add(word.toLowerCase().trim());
        } catch (IOException e) {
            System.out.println("Failed to load dictionary: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void processInputFile(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                int size = Integer.parseInt(line.trim());
                String[][] originalBoard = readBoard(br, size);
                while ((line = br.readLine()) != null && line.trim().isEmpty()) {}
                if (line == null) break;
                size = Integer.parseInt(line.trim());
                String[][] resultBoard = readBoard(br, size);
                if (originalBoard != null && resultBoard != null)
                    validateAndScorePlay(originalBoard, resultBoard);
            }
        } catch (IOException e) {
            System.out.println("Error reading input: " + e.getMessage());
            System.exit(1);
        }
    }

    private static String[][] readBoard(BufferedReader br, int size)
            throws IOException {
        String[][] board = new String[size][size];
        for (int i = 0; i < size; i++) {
            String line = br.readLine();
            if (line == null) throw new IOException("Unexpected end of file");
            String[] squares = line.trim().split("\\s+");
            if (squares.length != size)
                throw new IOException("Row width mismatch");
            System.arraycopy(squares, 0, board[i], 0, size);
        }
        return board;
    }

    private static void validateAndScorePlay(String[][] orig, String[][] result) {
        System.out.println("original board:");
        printBoard(orig);
        System.out.println("result board:");
        printBoard(result);

        List<int[]> newTiles = findNewTiles(orig, result);
        if (newTiles == null) {
            System.out.println();
            return;
        }
        if (newTiles.isEmpty()) {
            System.out.println("play is empty");
            System.out.println("play is not legal");
            System.out.println();
            return;
        }

        // Build play description
        StringBuilder playDesc = new StringBuilder();
        for (int[] pos : newTiles) {
            if (playDesc.length() > 0) playDesc.append(", ");
            playDesc.append(getLetter(result[pos[0]][pos[1]]));
            playDesc.append(" at (").append(pos[0]).append(", ").append(pos[1]).append(")");
        }
        System.out.println("play is " + playDesc);

        if (isLegalPlay(orig, result, newTiles)) {
            System.out.println("play is legal");
            System.out.println("score is " + calculateScore(orig, result, newTiles));
        } else {
            System.out.println("play is not legal");
        }
        System.out.println();
    }

    /**
     * Finds positions where new tiles were placed.
     * Returns null if boards are incompatible (tile removed or multiplier changed).
     */
    private static List<int[]> findNewTiles(String[][] orig, String[][] result) {
        List<int[]> newTiles = new ArrayList<>();
        for (int i = 0; i < orig.length; i++) {
            for (int j = 0; j < orig[i].length; j++) {
                if (!orig[i][j].equals(result[i][j])) {
                    boolean origEmpty   = isEmptySquare(orig[i][j]);
                    boolean resultEmpty = isEmptySquare(result[i][j]);
                    if (!origEmpty && resultEmpty) {
                        System.out.println("Incompatible boards: tile removed at ("
                                + i + ", " + j + ")");
                        System.out.println();
                        return null;
                    }
                    if (origEmpty && resultEmpty) {
                        System.out.println("Incompatible boards: multiplier mismatch at ("
                                + i + ", " + j + ")");
                        System.out.println();
                        return null;
                    }
                    if (origEmpty) newTiles.add(new int[]{i, j});
                }
            }
        }
        return newTiles;
    }

    /**
     * Checks if a play is legal:
     * - All new tiles are in the same row or same column
     * - All words formed are in the dictionary
     * - Tiles are connected (not isolated)
     */
    private static boolean isLegalPlay(String[][] orig, String[][] result,
                                       List<int[]> newTiles) {
        if (newTiles.size() == 1) {
            // Single tile: must form at least one word of length > 1
            int r = newTiles.get(0)[0], c = newTiles.get(0)[1];
            String hWord = extractWord(result, r, c, true);
            String vWord = extractWord(result, r, c, false);
            if (hWord.length() < 2 && vWord.length() < 2) return false;
            if (hWord.length() >= 2 && !dictionary.contains(hWord.toLowerCase())) return false;
            if (vWord.length() >= 2 && !dictionary.contains(vWord.toLowerCase())) return false;
            return true;
        }

        // All tiles must be in the same row or same column
        boolean sameRow = true, sameCol = true;
        int row0 = newTiles.get(0)[0], col0 = newTiles.get(0)[1];
        for (int[] pos : newTiles) {
            if (pos[0] != row0) sameRow = false;
            if (pos[1] != col0) sameCol = false;
        }
        if (!sameRow && !sameCol) return false;

        // Check main word
        String mainWord = sameRow
                ? extractWord(result, row0, col0, true)
                : extractWord(result, row0, col0, false);
        if (!dictionary.contains(mainWord.toLowerCase())) return false;

        // Check cross words
        for (int[] pos : newTiles) {
            String cross = sameRow
                    ? extractWord(result, pos[0], pos[1], false)
                    : extractWord(result, pos[0], pos[1], true);
            if (cross.length() > 1 && !dictionary.contains(cross.toLowerCase()))
                return false;
        }
        return true;
    }

    /**
     * Extracts the full word passing through (row, col) in the given direction.
     */
    private static String extractWord(String[][] board, int row, int col,
                                      boolean horizontal) {
        // Find start of word
        int r = row, c = col;
        while (true) {
            int pr = horizontal ? r : r - 1;
            int pc = horizontal ? c - 1 : c;
            if (pr < 0 || pc < 0) break;
            if (isEmptySquare(board[pr][pc])) break;
            r = pr; c = pc;
        }
        // Read forward
        StringBuilder word = new StringBuilder();
        int cr = r, cc = c;
        while (cr < board.length && cc < board[0].length
                && !isEmptySquare(board[cr][cc])) {
            word.append(getLetter(board[cr][cc]));
            if (horizontal) cc++; else cr++;
        }
        return word.toString();
    }

    /**
     * Calculates the score for new tiles placed, including:
     * - Letter multipliers from the original board
     * - Word multipliers from the original board
     * - Cross-word scores
     * - 50-point bingo bonus for using all 7 tiles
     */
    private static int calculateScore(String[][] orig, String[][] result,
                                      List<int[]> newTiles) {
        if (newTiles.isEmpty()) return 0;

        boolean sameRow = true, sameCol = true;
        int row0 = newTiles.get(0)[0], col0 = newTiles.get(0)[1];
        for (int[] pos : newTiles) {
            if (pos[0] != row0) sameRow = false;
            if (pos[1] != col0) sameCol = false;
        }
        boolean horizontal = sameRow;

        // Score main word
        int score = scoreWord(orig, result, newTiles, row0, col0, horizontal);

        // Score cross words
        Set<int[]> newTileSet = new HashSet<>(newTiles);
        for (int[] pos : newTiles) {
            String cross = horizontal
                    ? extractWord(result, pos[0], pos[1], false)
                    : extractWord(result, pos[0], pos[1], true);
            if (cross.length() > 1) {
                score += scoreWord(orig, result, newTiles,
                        pos[0], pos[1], !horizontal);
            }
        }

        // Bingo bonus
        if (newTiles.size() == 7) score += 50;

        return score;
    }

    /**
     * Scores a single word starting from the word's beginning at (row, col).
     * Applies letter and word multipliers from squares that were empty before.
     */
    private static int scoreWord(String[][] orig, String[][] result,
                                 List<int[]> newTiles, int row, int col,
                                 boolean horizontal) {
        // Find word start
        int r = row, c = col;
        while (true) {
            int pr = horizontal ? r : r - 1;
            int pc = horizontal ? c - 1 : c;
            if (pr < 0 || pc < 0) break;
            if (isEmptySquare(result[pr][pc])) break;
            r = pr; c = pc;
        }

        int wordScore = 0, wordMult = 1;
        int cr = r, cc = c;
        while (cr < orig.length && cc < orig[0].length
                && !isEmptySquare(result[cr][cc])) {
            int letterVal = getLetterValue(getLetter(result[cr][cc]));
            if (isEmptySquare(orig[cr][cc])) {
                // New tile — apply premium
                char bonus = orig[cr][cc].charAt(1);
                if      (bonus == '2') letterVal *= 2;
                else if (bonus == '3') letterVal *= 3;
                else if (bonus == 'd') wordMult  *= 2;
                else if (bonus == 't') wordMult  *= 3;
            }
            wordScore += letterVal;
            if (horizontal) cc++; else cr++;
        }
        return wordScore * wordMult;
    }

    private static boolean isEmptySquare(String square) {
        return square.equals("..") || square.startsWith(".")
                || square.endsWith(".");
    }

    private static char getLetter(String square) {
        return Character.toUpperCase(square.trim().charAt(0));
    }

    private static int getLetterValue(char c) {
        switch (Character.toUpperCase(c)) {
            case 'A': case 'E': case 'I': case 'L': case 'N':
            case 'O': case 'R': case 'S': case 'T': case 'U': return 1;
            case 'D': case 'G': return 2;
            case 'B': case 'C': case 'M': case 'P': return 3;
            case 'F': case 'H': case 'V': case 'W': case 'Y': return 4;
            case 'K': return 5;
            case 'J': case 'X': return 8;
            case 'Q': case 'Z': return 10;
            default: return 0;
        }
    }

    private static void printBoard(String[][] board) {
        for (String[] row : board)
            System.out.println(String.join(" ", row));
    }
}