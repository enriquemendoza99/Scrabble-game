public class Square {
    private SquareType type;
    private Tile tile;

    public Square(SquareType type) {
        this.type = type;
        this.tile = null;
    }

    public boolean isOccupied() {
        return tile != null;
    }

    public void setTile(Tile tile) {
        this.tile = tile;
    }

    public Tile getTile() {
        return tile;
    }

    public SquareType getType() {
        return type;
    }

    public int getLetterMultiplier() {
        switch (type) {
            case DOUBLE_LETTER:
                return 2;
            case TRIPLE_LETTER:
                return 3;
            default:
                return 1;
        }
    }

    public int getWordMultiplier() {
        switch (type) {
            case DOUBLE_WORD:
                return 2;
            case TRIPLE_WORD:
                return 3;
            default:
                return 1;
        }
    }
}
