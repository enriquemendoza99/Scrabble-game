/**
 * Represents the tiles from which player draw new tiles.
 */
package ScrabbleGame;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TileBag {
    private List<Tile> tiles;
    public TileBag() {
        tiles = new ArrayList<>();
        initializeTiles();
    }

    /**
     * Initialize the bag with standard distribution. Each letter has a
     * specific frequency and point value
     */
    private void initializeTiles() {
        addTiles('A', 1, 6);
        addTiles('B', 1, 4);
        addTiles('C', 1, 3);
        addTiles('D', 1, 2);
        addTiles('E', 1, 2);
        addTiles('F', 4, 9);
        addTiles('G', 2, 8);
        addTiles('H', 3, 4);
        addTiles('I', 1, 3);
        addTiles('J', 6, 2);
        addTiles('K', 7, 2);
        addTiles('L', 1, 1);
        addTiles('M', 2, 2);
        addTiles('N', 2, 4);
        addTiles('O', 1, 4);
        addTiles('P', 4, 5);
        addTiles('Q', 7, 1);
        addTiles('R', 1, 3);
        addTiles('S', 1, 4);
        addTiles('T', 2, 5);
        addTiles('U', 1, 9);
        addTiles('V', 5, 5);
        addTiles('W', 4, 4);
        addTiles('X', 7, 3);
        addTiles('Y', 5, 2);
        addTiles('Z', 5, 2);
        // Add blank tiles
        addTiles('*', 0, 1, true);

        shuffle();
    }
    /**
     * Adds multiple copies of a tile to the bag
     * @param letter the letter on the tile
     * @param value the point value of the tile
     * @param count the number of copies to add
     */
    private void addTiles(char letter, int value, int count) {
        addTiles(letter, value, count, false);
    }
    /**
     * Adds copies of a tile to the bag with blank tile
     * @param letter the letter on the tile
     * @param value the point value on the tile
     * @param count the number of copies to add
     * @param isBlank true if the tile is blank
     */
    private void addTiles(char letter, int value, int count, boolean isBlank) {
        for (int i = 0; i < count; i++) {
            tiles.add(new Tile(letter, value, isBlank));
        }
    }
    /**
     * Draws and removes a tile from the bag
     * @return the drawn tile, or null if bag is empty
     */
    public Tile drawTile() {
        if (tiles.isEmpty()) {
            return null;
        }
        return tiles.remove(tiles.size() - 1);
    }
    /**
     * Gets the number of tiles remaining in the bag
     * @return number of remaining tiles
     */
    public int getRemainingTiles() {
        return tiles.size();
    }
    /**
     * Checks if the bag is empty
     * @return true if no tiles remain, false otherwise
     */
    public boolean isEmpty() {
        return tiles.isEmpty();
    }
    /**
     * Random shuffles all the tiles in the bag
     */
    public void shuffle() {
        Collections.shuffle(tiles);
    }
    /**
     * Add a tile back to the bag and shuffles
     * @param tile the tile to add
     */
    public void addTile(Tile tile) {
        tiles.add(tile);
        shuffle();
    }
    /**
     * Returns a tile to the bag
     * @param tile the tile to return
     */
    public void returnTile(Tile tile) {
        addTile(tile);
    }
}