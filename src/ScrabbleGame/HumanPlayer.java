package ScrabbleGame;

import java.util.ArrayList;
import java.util.List;
public class HumanPlayer extends Player {
    private Move currentMove;

    public HumanPlayer(String name) {
        super(name);
        this.isComputer = false;
        this.currentMove = new Move();
    }

    @Override
    public Move makeMove(Board board) {
        // This will be called by the UI controller
        // The actual move implementation will be handled by the UI
        Move completedMove = currentMove;
        currentMove = new Move();
        return completedMove;
    }

    public boolean placeTile(Tile tile, Position position, Board board) {
        // Validate the tile placement
        if (!isValidTilePlacement(tile, position, board)) {
            return false;
        }

        // Add tile to current move
        currentMove.addTile(tile, position);

        // Determine move direction if this is the second tile
        if (currentMove.getTiles().size() == 2) {
            List<Position> positions = currentMove.getPositions();
            currentMove.setHorizontal(positions.get(0).getRow() == positions.get(1).getRow());
        }

        return true;
    }

    public boolean removeTile(Position position) {
        // Remove tile from current move
        if (currentMove.removeTileAt(position)) {
            // Return tile to rack
            return true;
        }
        return false;
    }

    public void clearMove() {
        currentMove = new Move();
    }

    public Move getCurrentMove() {
        return currentMove;
    }

    private boolean isValidTilePlacement(Tile tile, Position position, Board board) {
        // Check if position is on board
        if (position.getRow() < 0 || position.getRow() >= 15 ||
                position.getCol() < 0 || position.getCol() >= 15) {
            return false;
        }

        // Check if square is empty
        if (!board.getSquare(position.getRow(), position.getCol()).isEmpty()) {
            return false;
        }

        // If this is first tile of move, it's valid
        if (currentMove.getTiles().isEmpty()) {
            return true;
        }

        // Check if new tile maintains straight line
        if (currentMove.getTiles().size() == 1) {
            return true; // Second tile can go anywhere adjacent to first
        }

        // For subsequent tiles, must maintain line
        boolean isHorizontal = currentMove.isHorizontal();
        Position firstPos = currentMove.getPositions().get(0);

        if (isHorizontal) {
            return position.getRow() == firstPos.getRow();
        } else {
            return position.getCol() == firstPos.getCol();
        }
    }

    public boolean validateCurrentMove(Board board, Dictionary dictionary) {
        if (currentMove.getTiles().isEmpty()) {
            return false;
        }

        // Validate move creates proper words
        List<String> words = findWordsFormed(currentMove, board);
        for (String word : words) {
            if (!dictionary.isValidWord(word)) {
                return false;
            }
        }

        // Validate connection to existing tiles (except first move)
        if (!board.isFirstMove() && !connectsToExistingTiles(currentMove, board)) {
            return false;
        }

        // Validate first move uses center square
        if (board.isFirstMove()) {
            boolean usesCenterSquare = false;
            for (Position pos : currentMove.getPositions()) {
                if (pos.getRow() == 7 && pos.getCol() == 7) {
                    usesCenterSquare = true;
                    break;
                }
            }
            if (!usesCenterSquare) {
                return false;
            }
        }

        return true;
    }

    private List<String> findWordsFormed(Move move, Board board) {
        List<String> words = new ArrayList<>();

        // Find main word
        String mainWord = findWordAt(move.getPositions().get(0),
                move.isHorizontal(), move, board);
        if (mainWord != null) {
            words.add(mainWord);
        }

        // Find crossing words
        for (Position pos : move.getPositions()) {
            String crossWord = findWordAt(pos, !move.isHorizontal(), move, board);
            if (crossWord != null && crossWord.length() > 1) {
                words.add(crossWord);
            }
        }

        return words;
    }

    private String findWordAt(Position start, boolean horizontal,
                              Move currentMove, Board board) {
        StringBuilder word = new StringBuilder();
        Position current = new Position(start.getRow(), start.getCol());

        // Find start of word
        while (true) {
            Position prev = horizontal ?
                    new Position(current.getRow(), current.getCol() - 1) :
                    new Position(current.getRow() - 1, current.getCol());

            Square square = board.getSquare(prev.getRow(), prev.getCol());
            if (square == null || square.isEmpty()) {
                break;
            }
            current = prev;
        }

        // Build word
        while (true) {
            Square square = board.getSquare(current.getRow(), current.getCol());
            if (square == null || square.isEmpty()) {
                // Check if there's a tile in the current move at this position
                Tile moveTile = currentMove.getTileAt(current);
                if (moveTile == null) {
                    break;
                }
                word.append(moveTile.getLetter());
            } else {
                word.append(square.getTile().getLetter());
            }

            current = horizontal ?
                    new Position(current.getRow(), current.getCol() + 1) :
                    new Position(current.getRow() + 1, current.getCol());
        }

        return word.length() > 0 ? word.toString() : null;
    }

    private boolean connectsToExistingTiles(Move move, Board board) {
        for (Position pos : move.getPositions()) {
            // Check all adjacent positions
            Position[] adjacent = {
                    new Position(pos.getRow() - 1, pos.getCol()),
                    new Position(pos.getRow() + 1, pos.getCol()),
                    new Position(pos.getRow(), pos.getCol() - 1),
                    new Position(pos.getRow(), pos.getCol() + 1)
            };

            for (Position adj : adjacent) {
                Square square = board.getSquare(adj.getRow(), adj.getCol());
                if (square != null && !square.isEmpty() &&
                        !move.getPositions().contains(adj)) {
                    return true;
                }
            }
        }
        return false;
    }
}
