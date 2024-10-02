package ScrabbleGame;

import java.util.*;

public class Board {
    private Square[][] grid;
    private static final int SIZE = 15;
    private static final int CENTER = 7;

    public Board() {
        initializeBoard();
    }

    private void initializeBoard() {
        grid = new Square[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                grid[row][col] = new Square(getSquareType(row, col));
            }
        }
    }

    private SquareType getSquareType(int row, int col) {
        if ((row == 0 || row == 14) && (col == 0 || col == 7 || col == 14)) return SquareType.TRIPLE_WORD;
        if ((row == 7 && col == 0) || (row == 7 && col == 14)) return SquareType.TRIPLE_WORD;
        if ((row == 1 || row == 13) && (col == 1 || col == 13)) return SquareType.DOUBLE_WORD;
        if ((row == 2 || row == 12) && (col == 2 || col == 12)) return SquareType.DOUBLE_WORD;
        if ((row == 3 || row == 11) && (col == 3 || col == 11)) return SquareType.DOUBLE_WORD;
        if ((row == 4 || row == 10) && (col == 4 || col == 10)) return SquareType.DOUBLE_WORD;
        if (row == 7 && col == 7) return SquareType.DOUBLE_WORD;
        if ((row == 0 || row == 14) && (col == 3 || col == 11)) return SquareType.DOUBLE_LETTER;
        if ((row == 2 || row == 12) && (col == 6 || col == 8)) return SquareType.DOUBLE_LETTER;
        if ((row == 3 || row == 11) && (col == 0 || col == 7 || col == 14)) return SquareType.DOUBLE_LETTER;
        if ((row == 6 || row == 8) && (col == 2 || col == 6 || col == 8 || col == 12)) return SquareType.DOUBLE_LETTER;
        if ((row == 7) && (col == 3 || col == 11)) return SquareType.DOUBLE_LETTER;
        return SquareType.NORMAL;
    }

    public boolean placeTile(Tile tile, int row, int col) {
        if (!isValidPlacement(row, col)) {
            return false;
        }
        grid[row][col].setTile(tile);
        return true;
    }

    public boolean isValidPlacement(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE && !grid[row][col].isOccupied();
    }

    public List<String> getWords(List<Position> positions) {
        List<String> words = new ArrayList<>();
        String horizontalWord = getWordInDirection(positions, 0, 1);
        String verticalWord = getWordInDirection(positions, 1, 0);
        if (horizontalWord.length() > 1) words.add(horizontalWord);
        if (verticalWord.length() > 1) words.add(verticalWord);
        return words;
    }

    private String getWordInDirection(List<Position> positions, int rowDelta, int colDelta) {
        StringBuilder word = new StringBuilder();
        Position start = positions.get(0);
        int row = start.getRow();
        int col = start.getCol();

        // Move to the start of the word
        while (row > 0 && col > 0 && grid[row - rowDelta][col - colDelta].isOccupied()) {
            row -= rowDelta;
            col -= colDelta;
        }

        // Build the word
        while (row < SIZE && col < SIZE && grid[row][col].isOccupied()) {
            word.append(grid[row][col].getTile().getLetter());
            row += rowDelta;
            col += colDelta;
        }

        return word.toString();
    }

    public Square getSquare(int row, int col) {
        return grid[row][col];
    }

    public boolean isEmpty() {
        return grid[CENTER][CENTER].getTile() == null;
    }
    public boolean hasAdjacentTile(int row, int col) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            if (newRow >= 0 && newRow < SIZE && newCol >= 0 && newCol < SIZE) {
                if (grid[newRow][newCol].isOccupied()) {
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (grid[row][col].isOccupied()) {
                    sb.append(grid[row][col].getTile().getLetter());
                } else {
                    sb.append(grid[row][col].getType().getSymbol());
                }
                sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
