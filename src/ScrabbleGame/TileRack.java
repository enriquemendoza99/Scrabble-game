package ScrabbleGame;

import java.util.ArrayList;
import java.util.List;

public class TileRack {
    private List<Tile> tiles;
    private static final int RACK_SIZE = 7;

    public TileRack() {
        tiles = new ArrayList<>();
    }

    public boolean addTile(Tile tile) {
        if (tiles.size() < RACK_SIZE) {
            tiles.add(tile);
            return true;
        }
        return false;
    }

    public Tile removeTile(int index) {
        if (index >= 0 && index < tiles.size()) {
            return tiles.remove(index);
        }
        return null;
    }

    public Tile removeTile(char letter) {
        for (int i = 0; i < tiles.size(); i++) {
            if (tiles.get(i).getLetter() == letter) {
                return tiles.remove(i);
            }
        }
        return null;
    }

    public List<Tile> getTiles() {
        return new ArrayList<>(tiles);
    }

    public int getSize() {
        return tiles.size();
    }

    public boolean isEmpty() {
        return tiles.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Tile tile : tiles) {
            sb.append(tile.getLetter());
        }
        return sb.toString();
    }
}
