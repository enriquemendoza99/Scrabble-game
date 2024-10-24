package ScrabbleGame;

public class Board {
    private Square[][] squares;
    private static final int SIZE = 15;

    public Board() {
        squares = new Square[SIZE][SIZE];
        initializeBoard();
    }

    private void initializeBoard() {
        // Initialize all squares empty, with appropriate multipliers
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                squares[row][col] = new Square(getLetterMultiplier(row, col),
                        getWordMultiplier(row, col));
            }
        }
    }

    public void clear() {
        // Reset all squares to empty state
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                squares[row][col] = new Square(getLetterMultiplier(row, col),
                        getWordMultiplier(row, col));
            }
        }
    }

    private int getLetterMultiplier(int row, int col) {
        // Triple Letter Score
        if ((row == 1 && (col == 5 || col == 9)) ||
                (row == 5 && (col == 1 || col == 5 || col == 9 || col == 13)) ||
                (row == 9 && (col == 1 || col == 5 || col == 9 || col == 13)) ||
                (row == 13 && (col == 5 || col == 9))) {
            return 3;
        }
        // Double Letter Score
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

    private int getWordMultiplier(int row, int col) {
        // Triple Word Score
        if ((row == 0 && (col == 0 || col == 7 || col == 14)) ||
                (row == 7 && (col == 0 || col == 14)) ||
                (row == 14 && (col == 0 || col == 7 || col == 14))) {
            return 3;
        }
        // Double Word Score
        if ((row == col || row + col == 14) &&
                row != 0 && row != 7 && row != 14) {
            return 2;
        }
        return 1;
    }

    public Square getSquare(int row, int col) {
        if (row >= 0 && row < SIZE && col >= 0 && col < SIZE) {
            return squares[row][col];
        }
        return null;
    }

    public boolean isFirstMove() {
        return squares[7][7].isEmpty();
    }

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