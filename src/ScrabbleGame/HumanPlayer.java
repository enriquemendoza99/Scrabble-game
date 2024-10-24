/**
 * Represents the human player
 */
package ScrabbleGame;
import java.util.ArrayList;
import java.util.List;
public class HumanPlayer extends Player {
    private Move currentMove;

    /**
     * Initialize the human player
     * @param name the name of the player
     */
    public HumanPlayer(String name) {
        super(name);
        this.isComputer = false;
        this.currentMove = new Move();
    }

    /**
     * Makes the current move and resets
     * @param board the current board
     * @return the completed move
     */
    @Override
    public Move makeMove(Board board) {
        Move completedMove = currentMove;
        currentMove = new Move();
        return completedMove;
    }

    /**
     * Place a tile on the board at the specified position
     * @param tile the tile to be placed
     * @param position the position to place the tile
     * @param board the game board
     * @return true if the tile was placed, false otherwise
     */
    public boolean placeTile(Tile tile, Position position, Board board) {

        if (!isValidTilePlacement(tile, position, board)) {
            return false;
        }

        currentMove.addTile(tile, position);

        if (currentMove.getTiles().size() == 2) {
            List<Position> positions = currentMove.getPositions();
            currentMove.setHorizontal(positions.get(0).getRow() == positions.get(1).getRow());
        }

        return true;
    }

    /**
     * Removes a tile from the current move at the specified position
     * @param position the position of the tile to be removed
     * @return true if the tile was removed
     */
    public boolean removeTile(Position position) {
        if (currentMove.removeTileAt(position)) {
            return true;
        }
        return false;
    }

    /**
     * Clears the current move
     */
    public void clearMove() {
        currentMove = new Move();
    }

    /**
     * Gets the current move the player is working on
     * @return the current move
     */
    public Move getCurrentMove() {
        return currentMove;
    }

    /**
     * Checks if a tile can be placed at the specified position on the board
     * @param tile the tile to be placed
     * @param position the position where the tile be placed
     * @param board the game board
     * @return true if the tile placement is valid
     */
    private boolean isValidTilePlacement(Tile tile, Position position, Board board) {
        if (position.getRow() < 0 || position.getRow() >= 15 ||
                position.getCol() < 0 || position.getCol() >= 15) {
            return false;
        }
        if (!board.getSquare(position.getRow(), position.getCol()).isEmpty()) {
            return false;
        }
        if (currentMove.getTiles().isEmpty()) {
            return true;
        }
        if (currentMove.getTiles().size() == 1) {
            return true;
        }
        boolean isHorizontal = currentMove.isHorizontal();
        Position firstPos = currentMove.getPositions().get(0);

        if (isHorizontal) {
            return position.getRow() == firstPos.getRow();
        } else {
            return position.getCol() == firstPos.getCol();
        }
    }

    /**
     * Validates the current move to ensure it follows the rules
     * @param board the game board
     * @param dictionary the dictionary to validate words
     * @return true if the current move is valid
     */
    public boolean validateCurrentMove(Board board, Dictionary dictionary) {
        if (currentMove.getTiles().isEmpty()) {
            return false;
        }
        List<String> words = findWordsFormed(currentMove, board);
        for (String word : words) {
            if (!dictionary.isValidWord(word)) {
                return false;
            }
        }
        if (!board.isFirstMove() && !connectsToExistingTiles(currentMove, board)) {
            return false;
        }
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

    /**
     * Finds all words formed by the current move
     * @param move the move made by the player
     * @param board the game board
     * @return a list of words formed by the move
     */
    private List<String> findWordsFormed(Move move, Board board) {
        List<String> words = new ArrayList<>();
        String mainWord = findWordAt(move.getPositions().get(0),
                move.isHorizontal(), move, board);
        if (mainWord != null) {
            words.add(mainWord);
        }
        for (Position pos : move.getPositions()) {
            String crossWord = findWordAt(pos, !move.isHorizontal(), move, board);
            if (crossWord != null && crossWord.length() > 1) {
                words.add(crossWord);
            }
        }

        return words;
    }

    /**
     * Finds the word at the specified position in the given direction
     * @param start the starting position of the word
     * @param horizontal true if checking horizontally, false if vertically
     * @param currentMove the current move made by the palyer
     * @param board the game board
     * @return the word formed at the specified position
     */
    private String findWordAt(Position start, boolean horizontal,
                              Move currentMove, Board board) {
        StringBuilder word = new StringBuilder();
        Position current = new Position(start.getRow(), start.getCol());
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
        while (true) {
            Square square = board.getSquare(current.getRow(), current.getCol());
            if (square == null || square.isEmpty()) {
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

    /**
     * Checks if the current move connects to existing tiles in the board
     * @param move the move made by the player
     * @param board the game board
     * @return true if the move connects to existing tiles, false otherwise
     */
    private boolean connectsToExistingTiles(Move move, Board board) {
        for (Position pos : move.getPositions()) {
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
