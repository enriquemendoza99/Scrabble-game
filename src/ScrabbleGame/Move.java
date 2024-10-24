/**
 * Represents a move in the game
 */
package ScrabbleGame;
import java.util.ArrayList;
import java.util.List;

public class Move {
    private List<Tile> tiles;
    // Position for each tile
    private List<Position> positions;
    private int score;
    private boolean isHorizontal;

    /**
     * Creates a new empty move with no tiles or positions and a score of 0
     */
    public Move() {
        tiles = new ArrayList<>();
        positions = new ArrayList<>();
        score = 0;
    }

    /**
     * Constructor to copy another move
     * @param other the move to copy
     */
    public Move(Move other) {
        this.tiles = new ArrayList<>(other.tiles);
        this.positions = new ArrayList<>(other.positions);
        this.score = other.score;
        this.isHorizontal = other.isHorizontal;
    }

    /**
     * Adds a tile and its corresponding position to move
     * @param tile the tile to add
     * @param position the position where the tile be placed
     */
    public void addTile(Tile tile, Position position) {
        tiles.add(tile);
        positions.add(position);
    }

    /**
     * Removes a tile at the specified position from the move
     * @param position the position of the tile to remove
     * @return true if a tile was removed
     */
    public boolean removeTileAt(Position position) {
        int index = positions.indexOf(position);
        if (index != -1) {
            tiles.remove(index);
            positions.remove(index);
            return true;
        }
        return false;
    }

    /**
     * Returns a copy of the tiles in this move
     * @return a new list containing all tiles in this move
     */
    public List<Tile> getTiles() {
        return new ArrayList<>(tiles);
    }

    /**
     * Returns a copy of the position in this move
     * @return a new list containing all positions in this move
     */
    public List<Position> getPositions() {
        return new ArrayList<>(positions);
    }

    /**
     * Gets the tile at a specific position in this move
     * @param position  the position to check
     * @return the tile at the specific position
     */
    public Tile getTileAt(Position position) {
        int index = positions.indexOf(position);
        return index >= 0 ? tiles.get(index) : null;
    }

    /**
     * Sets the score for this move
     * @param score the score value to set
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Gets the score for this move
     * @return the current score value
     */
    public int getScore() {
        return score;
    }

    /**
     * Sets the tiles in this move are placed horizontally
     * @param horizontal
     */
    public void setHorizontal(boolean horizontal) {
        isHorizontal = horizontal;
    }

    /**
     * Checks if the tiles in this move are placed horizontally
     * @return true if tiles are placed horizontally, false if vertically
     */
    public boolean isHorizontal() {
        return isHorizontal;
    }

    /**
     * Validates if this move is legal
     * @return true if the move is legal, false otherwise
     */
    public boolean isValid() {
        if (tiles.isEmpty() || positions.isEmpty() || tiles.size() != positions.size()) {
            return false;
        }
        if (positions.size() > 1) {
            boolean sameRow = true;
            boolean sameCol = true;
            int row = positions.get(0).getRow();
            int col = positions.get(0).getCol();

            for (int i = 1; i < positions.size(); i++) {
                if (positions.get(i).getRow() != row) sameRow = false;
                if (positions.get(i).getCol() != col) sameCol = false;
            }

            if (!sameRow && !sameCol) return false;
            isHorizontal = sameRow;
        }

        return true;
    }
}