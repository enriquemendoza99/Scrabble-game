import java.util.*;

public class Player {
    private String name;
    private List<Tile> rack;
    private int score;

    public Player(String name) {
        this.name = name;
        this.rack = new ArrayList<>();
        this.score = 0;
    }

    public void addTileToRack(Tile tile) {
        rack.add(tile);
    }

    public List<Tile> getRack() {
        return new ArrayList<>(rack);
    }

    public void removeTilesFromRack(List<Tile> tiles) {
        rack.removeAll(tiles);
    }

    public void addToScore(int points) {
        score += points;
    }

    public int getScore() {
        return score;
    }

    public String getName() {
        return name;
    }

    public boolean hasTiles(List<Tile> tilesToCheck) {
        Map<Character, Integer> rackTileCount = new HashMap<>();
        for (Tile tile : rack) {
            rackTileCount.put(tile.getLetter(), rackTileCount.getOrDefault(tile.getLetter(), 0) + 1);
        }

        for (Tile tile : tilesToCheck) {
            char letter = tile.getLetter();
            if (!rackTileCount.containsKey(letter) || rackTileCount.get(letter) == 0) {
                return false;
            }
            rackTileCount.put(letter, rackTileCount.get(letter) - 1);
        }
        return true;
    }

    @Override
    public String toString() {
        return name + " (Score: " + score + ")";
    }
}