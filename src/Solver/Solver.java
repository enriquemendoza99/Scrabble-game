/**
 * Implements a Scrabble word finder that determines the highest scoring
 * legal move possible on a given board using available letter tiles.
 */
package Solver;

import java.io.*;
import java.util.*;

/**
 * Scrabble word solver that finds the highest-scoring legal move on a given
 * board using the available letter tiles in a tray.
 * Reads board and tray configurations from an input file and outputs
 * the best word, its score, and the resulting board state.
 *
 * Scoring rules:
 * - Letter values with premium square multipliers for new tiles only
 * - Word multipliers (DW/TW) from new tile squares apply to full word
 * - Cross words formed by new tiles are also scored
 * - Using all 7 tray tiles awards a 50-point bingo bonus
 */
public class Solver {
    private static Set<String> dictionary = new HashSet<>();
    private static String[][] board;
    private static char[] tray;
    private static int boardSize;
    private static boolean firstMove = true;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java -jar solver.jar <dictionary_file>");
            return;
        }
        loadDictionary(args[0]);
        try (BufferedReader reader = new BufferedReader(
                new FileReader("example_input.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                boardSize = Integer.parseInt(line);
                board = new String[boardSize][boardSize];
                for (int i = 0; i < boardSize; i++) {
                    line = reader.readLine();
                    String[] parts = line.trim().split("\\s+");
                    for (int j = 0; j < boardSize; j++)
                        board[i][j] = parts[j];
                }
                line = reader.readLine();
                if (line != null) tray = line.trim().toCharArray();
                solvePuzzle();
                firstMove = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadDictionary(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String word;
            while ((word = reader.readLine()) != null)
                dictionary.add(word.toLowerCase().trim());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void solvePuzzle() {
        System.out.println("Input ScrabbleGame.ScrabbleGame.Board:");
        printBoard();
        System.out.println("Tray: " + new String(tray));

        String bestWord       = "";
        int    bestScore      = -1;
        int    bestRow        = 0;
        int    bestCol        = 0;
        boolean bestHorizontal = true;
        int[]   bestTilesUsed = new int[]{0};

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                for (boolean horiz : new boolean[]{true, false}) {
                    for (String word : dictionary) {
                        if (word.length() < 2) continue;
                        int maxLen = horiz ? boardSize - j : boardSize - i;
                        if (word.length() > maxLen) continue;

                        int[] tilesFromTray = new int[1];
                        if (!canPlaceWord(word, i, j, horiz, tilesFromTray)) continue;
                        if (!isConnected(word, i, j, horiz)) continue;
                        if (!allWordsValid(word, i, j, horiz)) continue;

                        int score = calculateFullScore(word, i, j, horiz,
                                tilesFromTray[0]);

                        if (score > bestScore ||
                                (score == bestScore &&
                                        word.compareTo(bestWord) < 0)) {
                            bestScore      = score;
                            bestWord       = word;
                            bestRow        = i;
                            bestCol        = j;
                            bestHorizontal = horiz;
                            bestTilesUsed[0] = tilesFromTray[0];
                        }
                    }
                }
            }
        }

        if (bestWord.isEmpty()) {
            System.out.println("No valid move found.");
        } else {
            String display = formatWithBlanks(bestWord, bestRow, bestCol,
                    bestHorizontal);
            System.out.println("Solution " + display + " has " +
                    bestScore + " points");
            System.out.println("Solution ScrabbleGame.ScrabbleGame.Board:");
            placeWord(display, bestRow, bestCol, bestHorizontal);
            printBoard();
        }
        System.out.println();
    }

    /**
     * Checks if a word can be placed at (row,col) in the given direction.
     * Returns true if:
     * - Every square is either empty or already has the correct letter
     * - At least one new tile is placed from the tray
     * tilesFromTray[0] is set to the number of tray tiles used.
     */
    private static boolean canPlaceWord(String word, int row, int col,
                                        boolean horizontal, int[] tilesFromTray) {
        int[] avail = new int[26];
        int blanks = 0;
        for (char c : tray) {
            if (c == '*') blanks++;
            else avail[Character.toLowerCase(c) - 'a']++;
        }

        int newTiles = 0;
        for (int i = 0; i < word.length(); i++) {
            int r = horizontal ? row : row + i;
            int c2 = horizontal ? col + i : col;
            if (r >= boardSize || c2 >= boardSize) return false;

            String sq = board[r][c2];
            char existing = getLetterAt(sq);

            if (existing != 0) {
                // Existing tile must match
                if (Character.toLowerCase(existing) !=
                        Character.toLowerCase(word.charAt(i)))
                    return false;
            } else {
                // Need a tile from tray
                int idx = Character.toLowerCase(word.charAt(i)) - 'a';
                if (avail[idx] > 0) avail[idx]--;
                else if (blanks > 0) blanks--;
                else return false;
                newTiles++;
            }
        }
        tilesFromTray[0] = newTiles;
        return newTiles > 0;
    }

    /**
     * Checks connectivity:
     * - First move must cover the center square
     * - Subsequent moves must touch at least one existing tile
     */
    private static boolean isConnected(String word, int row, int col,
                                       boolean horizontal) {
        int center = boardSize / 2;
        if (isFirstMove()) {
            for (int i = 0; i < word.length(); i++) {
                int r = horizontal ? row : row + i;
                int c = horizontal ? col + i : col;
                if (r == center && c == center) return true;
            }
            return false;
        }
        for (int i = 0; i < word.length(); i++) {
            int r = horizontal ? row : row + i;
            int c = horizontal ? col + i : col;
            if (getLetterAt(board[r][c]) != 0) return true; // uses existing
            // Check adjacency
            int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
            for (int[] d : dirs) {
                int nr = r + d[0], nc = c + d[1];
                if (nr >= 0 && nr < boardSize && nc >= 0 && nc < boardSize
                        && getLetterAt(board[nr][nc]) != 0)
                    return true;
            }
        }
        return false;
    }

    private static boolean isFirstMove() {
        for (String[] row : board)
            for (String sq : row)
                if (getLetterAt(sq) != 0) return false;
        return true;
    }

    /**
     * Validates all words formed by the placement against the dictionary.
     */
    private static boolean allWordsValid(String word, int row, int col,
                                         boolean horizontal) {
        // Check main word
        if (!dictionary.contains(word.toLowerCase())) return false;

        // Check cross words
        for (int i = 0; i < word.length(); i++) {
            int r = horizontal ? row : row + i;
            int c = horizontal ? col + i : col;
            if (getLetterAt(board[r][c]) != 0) continue; // existing tile, skip

            String cross = extractCrossWord(word.charAt(i), r, c, !horizontal,
                    word, row, col, horizontal);
            if (cross != null && cross.length() > 1 &&
                    !dictionary.contains(cross.toLowerCase()))
                return false;
        }
        return true;
    }

    /**
     * Extracts the cross word through position (r,c) in the perpendicular direction.
     */
    private static String extractCrossWord(char newLetter, int r, int c,
                                           boolean crossHoriz,
                                           String word, int wordRow, int wordCol,
                                           boolean wordHoriz) {
        StringBuilder sb = new StringBuilder();
        // Go backward
        int pr = crossHoriz ? r : r - 1;
        int pc = crossHoriz ? c - 1 : c;
        while (pr >= 0 && pc >= 0 && getLetterAt(board[pr][pc]) != 0) {
            sb.insert(0, Character.toLowerCase(getLetterAt(board[pr][pc])));
            if (crossHoriz) pc--; else pr--;
        }
        sb.append(Character.toLowerCase(newLetter));
        // Go forward
        int nr = crossHoriz ? r : r + 1;
        int nc = crossHoriz ? c + 1 : c;
        while (nr < boardSize && nc < boardSize && getLetterAt(board[nr][nc]) != 0) {
            sb.append(Character.toLowerCase(getLetterAt(board[nr][nc])));
            if (crossHoriz) nc++; else nr++;
        }
        return sb.length() > 1 ? sb.toString() : null;
    }

    /**
     * Calculates the full score for a word placement including:
     * - Main word score with letter/word multipliers
     * - Cross word scores
     * - 50-point bingo bonus for using all 7 tray tiles
     */
    private static int calculateFullScore(String word, int row, int col,
                                          boolean horizontal, int tilesFromTray) {
        int mainScore = 0;
        int mainWM    = 1;

        for (int i = 0; i < word.length(); i++) {
            int r  = horizontal ? row : row + i;
            int c  = horizontal ? col + i : col;
            String sq = board[r][c];
            char existing = getLetterAt(sq);

            int val = getLetterValue(word.charAt(i));

            if (existing != 0) {
                // Existing tile — no premium
                mainScore += val;
            } else {
                // New tile — apply premium
                String prem = sq;
                if      (prem.equals(".2")) val *= 2;
                else if (prem.equals(".3")) val *= 3;
                else if (prem.equals("2.")) mainWM *= 2;
                else if (prem.equals("3.")) mainWM *= 3;
                else if (prem.equals("4.")) mainWM *= 4;
                else if (prem.equals(".4")) val *= 4;
                mainScore += val;
            }
        }
        mainScore *= mainWM;

        // Cross words
        int crossScore = 0;
        for (int i = 0; i < word.length(); i++) {
            int r  = horizontal ? row : row + i;
            int c  = horizontal ? col + i : col;
            if (getLetterAt(board[r][c]) != 0) continue; // existing, skip

            String cross = extractCrossWord(word.charAt(i), r, c,
                    !horizontal, word, row, col, horizontal);
            if (cross == null || cross.length() < 2) continue;

// Score this cross word — premium applies only to the new tile
            int cScore = 0, cWM = 1;
            String sq2 = board[r][c];
            int newVal = getLetterValue(word.charAt(i));
            if      (sq2.equals(".2")) newVal *= 2;
            else if (sq2.equals(".3")) newVal *= 3;
            else if (sq2.equals("2.")) cWM    *= 2;
            else if (sq2.equals("3.")) cWM    *= 3;
            else if (sq2.equals("4.")) cWM    *= 4;
            else if (sq2.equals(".4")) newVal *= 4;
            cScore += newVal;

// Add existing tiles going backward
            int pr2 = !horizontal ? r : r - 1;
            int pc2 = !horizontal ? c - 1 : c;
            while (pr2 >= 0 && pc2 >= 0 && getLetterAt(board[pr2][pc2]) != 0) {
                cScore += getLetterValue(getLetterAt(board[pr2][pc2]));
                if (!horizontal) pc2--; else pr2--;
            }
// Add existing tiles going forward
            int nr2 = !horizontal ? r : r + 1;
            int nc2 = !horizontal ? c + 1 : c;
            while (nr2 < boardSize && nc2 < boardSize
                    && getLetterAt(board[nr2][nc2]) != 0) {
                cScore += getLetterValue(getLetterAt(board[nr2][nc2]));
                if (!horizontal) nc2++; else nr2++;
            }
            crossScore += cScore * cWM;
        }

        // Bingo bonus
        int bingo = (tilesFromTray == 7) ? 50 : 0;

        return mainScore + crossScore + bingo;
    }

    private static char getLetterAt(String sq) {
        if (sq == null || sq.isEmpty()) return 0;
        // Single char — must be a letter from parsed board
        if (sq.length() == 1) {
            return Character.isLetter(sq.charAt(0)) ? sq.charAt(0) : 0;
        }
        // Two chars — check both positions
        char c0 = sq.charAt(0);
        char c1 = sq.charAt(1);
        if (Character.isLetter(c0)) return c0;
        if (Character.isLetter(c1)) return c1;
        return 0;
    }

    private static int getLetterValue(char c) {
        if (Character.isUpperCase(c)) return 0;
        switch (c) {
            case 'a': case 'e': case 'i': case 'l': case 'n':
            case 'o': case 'r': case 's': case 't': case 'u': return 1;
            case 'd': case 'g': return 2;
            case 'b': case 'c': case 'm': case 'p': return 3;
            case 'f': case 'h': case 'v': case 'w': case 'y': return 4;
            case 'k': return 5;
            case 'j': case 'x': return 8;
            case 'q': case 'z': return 10;
            default: return 0;
        }
    }

    private static String formatWithBlanks(String word, int row, int col,
                                           boolean horizontal) {
        int[] avail = new int[26];
        int blanks = 0;
        for (char c : tray) {
            if (c == '*') blanks++;
            else avail[Character.toLowerCase(c) - 'a']++;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            int r  = horizontal ? row : row + i;
            int c2 = horizontal ? col + i : col;
            if (getLetterAt(board[r][c2]) != 0) {
                result.append(word.charAt(i));
                continue;
            }
            int idx = Character.toLowerCase(word.charAt(i)) - 'a';
            if (avail[idx] > 0) {
                avail[idx]--;
                result.append(Character.toLowerCase(word.charAt(i)));
            } else {
                blanks--;
                // Uppercase = blank tile used for this letter
                result.append(Character.toUpperCase(word.charAt(i)));
            }
        }
        return result.toString();
    }

    private static void placeWord(String word, int row, int col,
                                  boolean horizontal) {
        for (int i = 0; i < word.length(); i++) {
            int r  = horizontal ? row : row + i;
            int c2 = horizontal ? col + i : col;
            if (getLetterAt(board[r][c2]) == 0)
                board[r][c2] = String.valueOf(word.charAt(i));
        }
    }

    private static void printBoard() {
        for (int i = 0; i < boardSize; i++) {
            StringBuilder line = new StringBuilder();
            for (int j = 0; j < boardSize; j++) {
                if (j > 0) line.append(" ");
                line.append(board[i][j]);
            }
            System.out.println(line);
        }
    }
}