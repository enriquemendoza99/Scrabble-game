package ScrabbleGame;

public class Square {
    private Tile tile;
    private int letterMultiplier;
    private int wordMultiplier;
    private boolean isPremiumUsed;

    public Square(int letterMultiplier, int wordMultiplier) {
        this.letterMultiplier = letterMultiplier;
        this.wordMultiplier = wordMultiplier;
        this.tile = null;
        this.isPremiumUsed = false;
    }

    public boolean isEmpty() {
        return tile == null;
    }

    public void placeTile(Tile tile) {
        this.tile = tile;
    }

    public Tile removeTile() {
        Tile removedTile = this.tile;
        this.tile = null;
        return removedTile;
    }

    public Tile getTile() {
        return tile;
    }

    public int getLetterMultiplier() {
        return isPremiumUsed ? 1 : letterMultiplier;
    }

    public int getWordMultiplier() {
        return isPremiumUsed ? 1 : wordMultiplier;
    }

    public void usePremium() {
        isPremiumUsed = true;
    }

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
