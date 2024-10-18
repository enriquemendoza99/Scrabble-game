package ScrabbleGame;
public class Square {
    private SquareType type;
    private Tile tile;

    public Square(SquareType type) {
        this.type = type;
        this.tile = null;
    }

    public SquareType getType() {
        return type;
    }

    public Tile getTile() {
        return tile;
    }

    public void setTile(Tile tile) {
        this.tile = tile;
    }

    public void removeTile() {
        this.tile = null;
    }

    public boolean isEmpty() {
        return tile == null;
    }

    @Override
    public String toString() {
        if (tile != null) {
            return "[" + tile.getLetter() + "]";
        } else {
            switch (type) {
                case TRIPLE_WORD: return "[TW]";
                case DOUBLE_WORD: return "[DW]";
                case TRIPLE_LETTER: return "[TL]";
                case DOUBLE_LETTER: return "[DL]";
                default: return "[ ]";
            }
        }
    }
}

enum SquareType {
    NORMAL, DOUBLE_LETTER, TRIPLE_LETTER, DOUBLE_WORD, TRIPLE_WORD
}
