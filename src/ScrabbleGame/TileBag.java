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

    private void initializeTiles() {
        // Standard Scrabble distribution
        addTiles('A', 1, 9);
        addTiles('B', 3, 2);
        addTiles('C', 3, 2);
        addTiles('D', 2, 4);
        addTiles('E', 1, 12);
        addTiles('F', 4, 2);
        addTiles('G', 2, 3);
        addTiles('H', 4, 2);
        addTiles('I', 1, 9);
        addTiles('J', 8, 1);
        addTiles('K', 5, 1);
        addTiles('L', 1, 4);
        addTiles('M', 3, 2);
        addTiles('N', 1, 6);
        addTiles('O', 1, 8);
        addTiles('P', 3, 2);
        addTiles('Q', 10, 1);
        addTiles('R', 1, 6);
        addTiles('S', 1, 4);
        addTiles('T', 1, 6);
        addTiles('U', 1, 4);
        addTiles('V', 4, 2);
        addTiles('W', 4, 2);
        addTiles('X', 8, 1);
        addTiles('Y', 4, 2);
        addTiles('Z', 10, 1);
        // Add blank tiles
        addTiles('*', 0, 2, true);

        shuffle();
    }

    private void addTiles(char letter, int value, int count) {
        addTiles(letter, value, count, false);
    }

    private void addTiles(char letter, int value, int count, boolean isBlank) {
        for (int i = 0; i < count; i++) {
            tiles.add(new Tile(letter, value, isBlank));
        }
    }

    public void shuffle() {
        Collections.shuffle(tiles);
    }

    public Tile drawTile() {
        if (tiles.isEmpty()) {
            return null;
        }
        return tiles.remove(tiles.size() - 1);
    }

    public int getRemainingTiles() {
        return tiles.size();
    }

    public boolean isEmpty() {
        return tiles.isEmpty();
    }
    public void addTile(Tile tile) {
        tiles.add(tile);
        shuffle();
    }

    public void returnTile(Tile tile) {
        addTile(tile);
    }
}