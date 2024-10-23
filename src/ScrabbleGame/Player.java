package ScrabbleGame;

import java.util.ArrayList;
import java.util.List;
public abstract class Player {
    protected String name;
    protected TileRack rack;
    protected int score;
    protected boolean isComputer;

    public Player(String name) {
        this.name = name;
        this.rack = new TileRack();
        this.score = 0;
    }

    public void drawTiles(TileBag bag) {
        while (rack.getSize() < 7 && !bag.isEmpty()) {
            rack.addTile(bag.drawTile());
        }
    }

    public List<Tile> exchangeTiles(List<Tile> tilesToExchange, TileBag bag) {
        if (bag.getRemainingTiles() < 7) {
            return null;
        }

        List<Tile> newTiles = new ArrayList<>();

        // Remove selected tiles from rack
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

    public void updateScore(int points) {
        score += points;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public TileRack getRack() {
        return rack;
    }

    public boolean isComputer() {
        return isComputer;
    }

    public abstract Move makeMove(Board board);
}