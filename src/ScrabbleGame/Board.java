package ScrabbleGame;
import java.util.Arrays;

public class Board {
    public static final int BOARD_SIZE = 15;
    private Square[][] squares;

    public Board() {
        squares = new Square[BOARD_SIZE][BOARD_SIZE];
        initializeBoard();
    }

    private void initializeBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                squares[row][col] = new Square(getSquareType(row, col));
            }
        }
    }

    private SquareType getSquareType(int row, int col) {
        if (isTripleWordSquare(row, col)) return SquareType.TRIPLE_WORD;
        if (isDoubleWordSquare(row, col)) return SquareType.DOUBLE_WORD;
        if (isTripleLetterSquare(row, col)) return SquareType.TRIPLE_LETTER;
        if (isDoubleLetterSquare(row, col)) return SquareType.DOUBLE_LETTER;
        return SquareType.NORMAL;
    }

    private boolean isTripleWordSquare(int row, int col) {
        return (row == 0 || row == 7 || row == 14) && (col == 0 || col == 7 || col == 14);
    }

    private boolean isDoubleWordSquare(int row, int col) {
        if (row == col || row == BOARD_SIZE - 1 - col) {
            return row != 0 && row != 7 && row != 14;
        }
        return false;
    }

    private boolean isTripleLetterSquare(int row, int col) {
        int[][] tripleLetterPositions = {{1, 5}, {1, 9}, {5, 1}, {5, 5}, {5, 9}, {5, 13}, {9, 1}, {9, 5}, {9, 9}, {9, 13}, {13, 5}, {13, 9}};
        return Arrays.stream(tripleLetterPositions).anyMatch(pos -> pos[0] == row && pos[1] == col);
    }

    private boolean isDoubleLetterSquare(int row, int col) {
        int[][] doubleLetterPositions = {{0, 3}, {0, 11}, {2, 6}, {2, 8}, {3, 0}, {3, 7}, {3, 14}, {6, 2}, {6, 6}, {6, 8}, {6, 12}, {7, 3}, {7, 11}, {8, 2}, {8, 6}, {8, 8}, {8, 12}, {11, 0}, {11, 7}, {11, 14}, {12, 6}, {12, 8}, {14, 3}, {14, 11}};
        return Arrays.stream(doubleLetterPositions).anyMatch(pos -> pos[0] == row && pos[1] == col);
    }

    public boolean placeTile(Tile tile, int row, int col) {
        if (isValidPosition(row, col) && squares[row][col].isEmpty()) {
            squares[row][col].setTile(tile);
            return true;
        }
        return false;
    }

    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    public Square getSquare(int row, int col) {
        if (isValidPosition(row, col)) {
            return squares[row][col];
        }
        return null;
    }

    public void removeTile(int row, int col) {
        if (isValidPosition(row, col)) {
            squares[row][col].removeTile();
        }
    }
    public boolean hasAdjacentTile(int row, int col) {
        int[][] adjacentPositions = {
                {row - 1, col}, {row + 1, col}, {row, col - 1}, {row, col + 1}
        };

        for (int[] pos : adjacentPositions) {
            if (isValidPosition(pos[0], pos[1]) && !getSquare(pos[0], pos[1]).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (!squares[row][col].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                sb.append(squares[row][col]);
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
