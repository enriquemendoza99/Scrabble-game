import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ScoreChecker {
    private Set<String> dictionary;
    private static final int BOARD_SIZE = 15;
    private static final char BLANK = '.';
    private static final char DOUBLE_LETTER = '2';
    private static final char TRIPLE_LETTER = '3';
    private static final char DOUBLE_WORD = 'd';
    private static final char TRIPLE_WORD = 't';

    public ScoreChecker(String dictionaryFile) {
        loadDictionary(dictionaryFile);
    }

    private void loadDictionary(String filename) {
        dictionary = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String word;
            while ((word = br.readLine()) != null) {
                dictionary.add(word.toLowerCase());
            }
        } catch (IOException e) {
            System.err.println("Error loading dictionary: " + e.getMessage());
            System.exit(1);
        }
    }

    private char[][] readBoard(Scanner scanner) {
        char[][] board = new char[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            String line = scanner.nextLine();
            String[] squares = line.split(" ");
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = squares[j].charAt(1);
            }
        }
        return board;
    }

    private boolean isPlayLegal(char[][] originalBoard, char[][] newBoard) {
        List<String> newWords = findNewWords(originalBoard, newBoard);
        for (String word : newWords) {
            if (!dictionary.contains(word.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    private List<String> findNewWords(char[][] originalBoard, char[][] newBoard) {
        // TODO: Implement this method to find all new words formed by the play
        // This should return a list of all new words formed, both horizontally and vertically
        return new ArrayList<>();
    }

    private int calculateScore(char[][] originalBoard, char[][] newBoard) {
        int score = 0;
        int wordMultiplier = 1;
        boolean usedAllTiles = true;
        int tilesUsed = 0;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (originalBoard[i][j] != newBoard[i][j]) {
                    int letterScore = getLetterScore(newBoard[i][j]);
                    char multiplier = originalBoard[i][j];

                    switch (multiplier) {
                        case DOUBLE_LETTER:
                            letterScore *= 2;
                            break;
                        case TRIPLE_LETTER:
                            letterScore *= 3;
                            break;
                        case DOUBLE_WORD:
                            wordMultiplier *= 2;
                            break;
                        case TRIPLE_WORD:
                            wordMultiplier *= 3;
                            break;
                    }

                    score += letterScore;
                    tilesUsed++;
                }
            }
        }

        score *= wordMultiplier;

        // Add bonus for using all tiles
        if (tilesUsed == 7) {
            score += 50;
        }

        return score;
    }

    private int getLetterScore(char letter) {
        switch (Character.toLowerCase(letter)) {
            case 'a': case 'e': case 'i': case 'o': case 'u': case 'l': case 'n': case 'r': case 's': case 't':
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
                return 0; // for blank tiles or invalid characters
        }
    }

    public void processBoards() {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            char[][] originalBoard = readBoard(scanner);
            char[][] newBoard = readBoard(scanner);

            boolean isLegal = isPlayLegal(originalBoard, newBoard);
            int score = calculateScore(originalBoard, newBoard);

            // Print the results
            System.out.println("original board:");
            printBoard(originalBoard);
            System.out.println("result board:");
            printBoard(newBoard);

            // TODO: Implement logic to identify which tiles were played
            // System.out.println("play is <tiles played>");

            System.out.println("play is " + (isLegal ? "legal" : "not legal"));
            if (isLegal) {
                System.out.println("score is " + score);
            }
            System.out.println(); // Blank line between test cases
        }
        scanner.close();
    }

    private void printBoard(char[][] board) {
        for (char[] row : board) {
            for (char square : row) {
                System.out.print(square + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java -jar scorechecker.jar <dictionary_file>");
            System.exit(1);
        }
        ScoreChecker checker = new ScoreChecker(args[0]);
        checker.processBoards();
    }
}
