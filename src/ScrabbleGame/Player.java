/**
 * Provides functionality for human and computer player
 */
package ScrabbleGame;
import java.util.ArrayList;
import java.util.List;
public abstract class Player {
    protected String name;
    protected TileRack rack;
    protected int score;
    protected boolean isComputer;

    /**
     * Creates a new player with a name and initialize an empty rack
     * @param name the display name of the player
     */
    public Player(String name) {
        this.name = name;
        this.rack = new TileRack();
        this.score = 0;
    }

    /**
     * Draws tiles from the bag until the player´s rack is full or the bag
     * is empty
     * @param bag The tile bag to draw new tiles from
     */
    public void drawTiles(TileBag bag) {
        while (rack.getSize() < 7 && !bag.isEmpty()) {
            rack.addTile(bag.drawTile());
        }
    }

    /**
     * Exchanges selected tiles from the player´s rack with new tiles
     * from the bag
     * @param tilesToExchange List of tiles the player wants to exchange
     * @param bag the tile bag to draw new tiles
     * @return List of new tiles drawn from the bag or null if its empty
     */
    public List<Tile> exchangeTiles(List<Tile> tilesToExchange, TileBag bag) {
        if (bag.getRemainingTiles() < 7) {
            return null;
        }
        List<Tile> newTiles = new ArrayList<>();
        // Remove selected tiles
        for (Tile tile : tilesToExchange) {
            rack.removeTile(tile.getLetter());
        }
        // Draw new tiles
        for (int i = 0; i < tilesToExchange.size(); i++) {
            Tile newTile = bag.drawTile();
            if (newTile != null) {
                newTiles.add(newTile);
                rack.addTile(newTile);
            }
        }
        // Return exchanged tiles to bag
        for (Tile tile : tilesToExchange) {
            bag.addTile(tile);
        }
        return newTiles;
    }

    /**
     * Updates the player´s score
     * @param points the number of points to add
     */
    public void updateScore(int points) {
        score += points;
    }

    /**
     * Get the name of the player
     * @return the player´s display name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the score of the player
     * @return the player´s current score
     */
    public int getScore() {
        return score;
    }

    /**
     * Get the tile rack of the player
     * @return the player´s current tile rack
     */
    public TileRack getRack() {
        return rack;
    }

    /**
     * Checks if the player is computer
     * @return true if this is a computer player, false is human player
     */
    public boolean isComputer() {
        return isComputer;
    }

    /**
     * Determine the next move in the game
     * @param board the current board
     * @return the move to be played on the board
     */
    public abstract Move makeMove(Board board);
}