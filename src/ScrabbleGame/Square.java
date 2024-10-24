/**
 * Represents a single square on the board
 */
package ScrabbleGame;

public class Square {
    private Tile tile;
    // Letter score multiplier
    private int letterMultiplier;
    // Word score multiplier
    private int wordMultiplier;
    private boolean isPremiumUsed;

    /**
     * Creates a new square with specified multiplier
     * @param letterMultiplier Multiplier for letter score
     * @param wordMultiplier multiplier for word score
     */
    public Square(int letterMultiplier, int wordMultiplier) {
        this.letterMultiplier = letterMultiplier;
        this.wordMultiplier = wordMultiplier;
        this.tile = null;
        this.isPremiumUsed = false;
    }
    /**
     * Checks if the square is empty
     * @return true if no tile is present, false otherwise
     */
    public boolean isEmpty() {
        return tile == null;
    }
    /**
     * Places a tile on the square
     * @param tile the tile to place
     */
    public void placeTile(Tile tile) {
        this.tile = tile;
    }
    /**
     * Removes and returns the tile from this square
     * @return the removed tile or null if there is no tile
     */
    public Tile removeTile() {
        Tile removedTile = this.tile;
        this.tile = null;
        return removedTile;
    }
    /**
     * Gets the tile currently on this square
     * @return the current tile or null if its empty
     */
    public Tile getTile() {
        return tile;
    }

    /**
     * Gets the current letter score multiplier
     * @return 1 if the premium has already been used
     */
    public int getLetterMultiplier() {
        return isPremiumUsed ? 1 : letterMultiplier;
    }

    /**
     * Gets the current word score multiplier
     * @return 1 if the premium has already been used
     */
    public int getWordMultiplier() {
        return isPremiumUsed ? 1 : wordMultiplier;
    }

    /**
     * Marks the premium multiplier as used
     */
    public void usePremium() {
        isPremiumUsed = true;
    }

    /**
     * Returns a string representation of the square for board display
     * @return String representation of the square
     */
    @Override
    public String toString() {
        if (tile != null) {
            return " " + tile.toString();
        }
        if (wordMultiplier > 1) {
            return wordMultiplier + ".";
        }
        if (letterMultiplier > 1) {
            return "." + letterMultiplier;
        }
        return "..";
    }
}
