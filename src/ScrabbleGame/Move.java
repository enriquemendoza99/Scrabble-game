package ScrabbleGame;

import java.util.ArrayList;
import java.util.List;

public class Move {
    private List<Tile> tiles;
    private List<Position> positions;
    private int score;
    private boolean isHorizontal;

    public Move() {
        tiles = new ArrayList<>();
        positions = new ArrayList<>();
        score = 0;
    }

    public Move(Move other) {
        this.tiles = new ArrayList<>(other.tiles);
        this.positions = new ArrayList<>(other.positions);
        this.score = other.score;
        this.isHorizontal = other.isHorizontal;
    }

    public void addTile(Tile tile, Position position) {
        tiles.add(tile);
        positions.add(position);
    }

    public boolean removeTileAt(Position position) {
        int index = positions.indexOf(position);
        if (index != -1) {
            tiles.remove(index);
            positions.remove(index);
            return true;
        }
        return false;
    }

    public boolean removeTile(Position position) {
        return removeTileAt(position);
    }

    public List<Tile> getTiles() {
        return new ArrayList<>(tiles);
    }

    public List<Position> getPositions() {
        return new ArrayList<>(positions);
    }

    public Tile getTileAt(Position position) {
        int index = positions.indexOf(position);
        return index >= 0 ? tiles.get(index) : null;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void setHorizontal(boolean horizontal) {
        isHorizontal = horizontal;
    }

    public boolean isHorizontal() {
        return isHorizontal;
    }

    public boolean isValid() {
        if (tiles.isEmpty() || positions.isEmpty() || tiles.size() != positions.size()) {
            return false;
        }

        // Check if tiles are in a line
        if (positions.size() > 1) {
            boolean sameRow = true;
            boolean sameCol = true;
            int row = positions.get(0).getRow();
            int col = positions.get(0).getCol();

            for (int i = 1; i < positions.size(); i++) {
                if (positions.get(i).getRow() != row) sameRow = false;
                if (positions.get(i).getCol() != col) sameCol = false;
            }

            if (!sameRow && !sameCol) return false;
            isHorizontal = sameRow;
        }

        return true;
    }
}