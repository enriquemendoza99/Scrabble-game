/**
 * Represents the board with 15 x 15 grid of squares
 */
package ScrabbleGame;

public class Board {
    private Square[][] squares;
    private static final int SIZE = 15;

    /**
     * Creates a new board and initializes all squares
     */
    public Board() {
        squares = new Square[SIZE][SIZE];
        initializeBoard();
    }

    /**
     * Initializes the board by creating Square objects for each position
     */
    private void initializeBoard() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                squares[row][col] = new Square(getLetterMultiplier(row, col),
                        getWordMultiplier(row, col));
            }
        }
    }

    /**
     * Resets the board to its initial state, clearing all tiles
     */
    public void clear() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                squares[row][col] = new Square(getLetterMultiplier(row, col),
                        getWordMultiplier(row, col));
            }
        }
    }

    /**
     * Determines the letter multiplier for a given position
     * @param row the row position on the board
     * @param col the column position on the board
     * @return 3 for triple letter score, 2 for double letter score,
     * 1 for normal squares
     */
    private int getLetterMultiplier(int row, int col) {
        if ((row == 1 && (col == 5 || col == 9)) ||
                (row == 5 && (col == 1 || col == 5 || col == 9 || col == 13)) ||
                (row == 9 && (col == 1 || col == 5 || col == 9 || col == 13)) ||
                (row == 13 && (col == 5 || col == 9))) {
            return 3;
        }
        if ((row == 0 && (col == 3 || col == 11)) ||
                (row == 2 && (col == 6 || col == 8)) ||
                (row == 3 && (col == 0 || col == 7 || col == 14)) ||
                (row == 6 && (col == 2 || col == 6 || col == 8 || col == 12)) ||
                (row == 7 && (col == 3 || col == 11)) ||
                (row == 8 && (col == 2 || col == 6 || col == 8 || col == 12)) ||
                (row == 11 && (col == 0 || col == 7 || col == 14)) ||
                (row == 12 && (col == 6 || col == 8)) ||
                (row == 14 && (col == 3 || col == 11))) {
            return 2;
        }
        return 1;
    }

    /**
     * Determines the word multiplier for a given position
     * @param row the row position on the board
     * @param col the column position on the board
     * @return 3 for triple letter score, 2 for double letter score,
     * 1 for normal squares
     */
    private int getWordMultiplier(int row, int col) {
        if ((row == 0 && (col == 0 || col == 7 || col == 14)) ||
                (row == 7 && (col == 0 || col == 14)) ||
                (row == 14 && (col == 0 || col == 7 || col == 14))) {
            return 3;
        }
        if ((row == col || row + col == 14) &&
                row != 0 && row != 7 && row != 14) {
            return 2;
        }
        return 1;
    }

    /**
     * Returns the Square object at the specified position on the board
     * @param row the row position
     * @param col the column position
     * @return the square at the specified position or null if position
     * is invalid
     */
    public Square getSquare(int row, int col) {
        if (row >= 0 && row < SIZE && col >= 0 && col < SIZE) {
            return squares[row][col];
        }
        return null;
    }

    /**
     * Checks is this is the first move of the game
     * @return true is center square is empty, false otherwise
     */
    public boolean isFirstMove() {
        return squares[7][7].isEmpty();
    }

    /**
     * Places tiles on the board according to the specified move
     * @param move The move object containing tile placement
     * @return true if tile were succesfullu placed, false if move is invalid
     */
    public boolean placeTiles(Move move) {
        if (!move.isValid()) {
            return false;
        }

        for (Position pos : move.getPositions()) {
            if (pos.getRow() >= 0 && pos.getRow() < SIZE &&
                    pos.getCol() >= 0 && pos.getCol() < SIZE) {
                squares[pos.getRow()][pos.getCol()].placeTile(move.getTileAt(pos));
                squares[pos.getRow()][pos.getCol()].usePremium();
            }
        }
        return true;
    }
}