package ScrabbleGame;

import java.util.ArrayList;
import java.util.List;

public abstract class Player {
    protected String name;
    protected List<Tile> rack;
    protected int score;

    public Player(String name) {
        this.name = name;
        this.rack = new ArrayList<>();
        this.score = 0;
    }

    public abstract void playTurn(Board board, TileBag tileBag, Dictionary dictionary);

    public void drawTiles(TileBag tileBag, int count) {
        for (int i = 0; i < count && rack.size() < 7; i++) {
            Tile tile = tileBag.drawTile();
            if (tile != null) {
                rack.add(tile);
            } else {
                break; // No more tiles in the bag
            }
        }
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

    public List<Tile> getRack() {
        return new ArrayList<>(rack);
    }
}