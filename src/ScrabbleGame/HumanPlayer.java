package ScrabbleGame;

import java.util.ArrayList;
import java.util.List;

public class HumanPlayer extends Player {
    private List<Tile> selectedTiles;
    private List<int[]> selectedPositions;

    public HumanPlayer(String name) {
        super(name);
        this.selectedTiles = new ArrayList<>();
        this.selectedPositions = new ArrayList<>();
    }

    @Override
    public void playTurn(Board board, TileBag tileBag, Dictionary dictionary) {
        // This method will be called by the game loop
        // The actual implementation will depend on your GUI
        // It should wait for user input to place tiles on the board

        System.out.println(name + "'s turn. Tiles in rack: " + rack);
    }

    public void selectTile(Tile tile) {
        if (rack.contains(tile) && !selectedTiles.contains(tile)) {
            selectedTiles.add(tile);
        }
    }

    public void deselectTile(Tile tile) {
        selectedTiles.remove(tile);
    }

    public void selectPosition(int row, int col) {
        selectedPositions.add(new int[]{row, col});
    }

    public void deselectPosition(int row, int col) {
        selectedPositions.removeIf(pos -> pos[0] == row && pos[1] == col);
    }

    public boolean playWord(Board board, Dictionary dictionary) {
        if (selectedTiles.size() != selectedPositions.size() || selectedTiles.isEmpty()) {
            return false;
        }

        // Sort positions to ensure correct word order
        selectedPositions.sort((a, b) -> a[0] == b[0] ? a[1] - b[1] : a[0] - b[0]);

        // Check if the word is in a straight line
        boolean isHorizontal = selectedPositions.stream().allMatch(pos -> pos[0] == selectedPositions.get(0)[0]);
        boolean isVertical = selectedPositions.stream().allMatch(pos -> pos[1] == selectedPositions.get(0)[1]);
        if (!isHorizontal && !isVertical) {
            return false;
        }

        // Check if the word is connected to existing tiles (except for the first turn)
        if (!board.isEmpty() && !isConnectedToExistingTiles(board)) {
            return false;
        }

        // Build the word and check if it's valid
        StringBuilder wordBuilder = new StringBuilder();
        for (int i = 0; i < selectedTiles.size(); i++) {
            int[] pos = selectedPositions.get(i);
            if (board.getSquare(pos[0], pos[1]).isEmpty()) {
                wordBuilder.append(selectedTiles.get(i).getLetter());
            } else {
                wordBuilder.append(board.getSquare(pos[0], pos[1]).getTile().getLetter());
            }
        }
        String word = wordBuilder.toString();

        if (!dictionary.isValidWord(word)) {
            return false;
        }

        // Place tiles on the board and remove from rack
        for (int i = 0; i < selectedTiles.size(); i++) {
            Tile tile = selectedTiles.get(i);
            int[] pos = selectedPositions.get(i);
            if (board.getSquare(pos[0], pos[1]).isEmpty()) {
                board.placeTile(tile, pos[0], pos[1]);
                rack.remove(tile);
            }
        }

        // Calculate score
        int score = calculateScore(board);
        addToScore(score);

        // Clear selections
        selectedTiles.clear();
        selectedPositions.clear();

        return true;
    }

    private boolean isConnectedToExistingTiles(Board board) {
        for (int[] pos : selectedPositions) {
            if (board.hasAdjacentTile(pos[0], pos[1])) {
                return true;
            }
        }
        return false;
    }

    private int calculateScore(Board board) {
        int score = 0;
        int wordMultiplier = 1;
        for (int i = 0; i < selectedPositions.size(); i++) {
            int[] pos = selectedPositions.get(i);
            Square square = board.getSquare(pos[0], pos[1]);
            Tile tile = selectedTiles.get(i);

            int letterScore = tile.getValue();
            if (square.getType() == SquareType.DOUBLE_LETTER) {
                letterScore *= 2;
            } else if (square.getType() == SquareType.TRIPLE_LETTER) {
                letterScore *= 3;
            }
            score += letterScore;

            if (square.getType() == SquareType.DOUBLE_WORD) {
                wordMultiplier *= 2;
            } else if (square.getType() == SquareType.TRIPLE_WORD) {
                wordMultiplier *= 3;
            }
        }
        score *= wordMultiplier;

        // Bonus for using all 7 tiles
        if (selectedTiles.size() == 7) {
            score += 50;
        }

        return score;
    }
}
