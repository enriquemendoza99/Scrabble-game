/**
 * Manages the game play, player turns, tile management, score calculation.
 */
package ScrabbleGame;

import java.util.*;
import java.io.IOException;

public class ScrabbleGame {
    private Board board;
    private Player humanPlayer;
    private ComputerPlayer computerPlayer;
    private TileBag tileBag;
    private Dictionary dictionary;
    private Player currentPlayer;
    private boolean gameOver;
    private List<String> gameLog;
    private int consecutivePasses;
    private List<GameStateListener> gameStateListeners;
    private static final int MAX_PASSES = 6;

    /**
     * Interface for game state change listener
     */
    public interface GameStateListener {
        void onGameStateChanged();
        void onMoveCompleted(Player player, Move move, int score);
        void onGameOver();
    }

    /**
     * Initialize the game components
     * @param dictionaryFile the file path of the dictionary
     * @throws IOException exception if there is an error
     */
    public ScrabbleGame(String dictionaryFile) throws IOException {
        gameStateListeners = new ArrayList<>();
        gameLog = new ArrayList<>();
        dictionary = new Dictionary();
        dictionary.loadDictionary(dictionaryFile);
        board = new Board();
        tileBag = new TileBag();
        humanPlayer = new HumanPlayer("Human");
        computerPlayer = new ComputerPlayer("Computer", dictionary);
        humanPlayer.drawTiles(tileBag);
        computerPlayer.drawTiles(tileBag);
        determineFirstPlayer();
        gameOver = false;
        consecutivePasses = 0;
    }

    /**
     * Add a game state listener
     * @param listener listens for game state changes
     */
    public void addGameStateListener(GameStateListener listener) {
        if (listener != null) {
            gameStateListeners.add(listener);
        }
    }

    /**
     * Notify all listener that the game state has changed
     */
    private void notifyGameStateChanged() {
        for (GameStateListener listener : gameStateListeners) {
            listener.onGameStateChanged();
        }
    }

    /**
     * notify when a moved is completed
     * @param player
     * @param move
     * @param score
     */
    private void notifyMoveCompleted(Player player, Move move, int score) {
        for (GameStateListener listener : gameStateListeners) {
            listener.onMoveCompleted(player, move, score);
        }
    }

    /**
     * Notify when the game is over
     */
    private void notifyGameOver() {
        for (GameStateListener listener : gameStateListeners) {
            listener.onGameOver();
        }
    }

    /**
     * Determines the first player
     */
    private void determineFirstPlayer() {
        char humanBest = 'Z';
        char computerBest = 'Z';
        for (Tile tile : humanPlayer.getRack().getTiles()) {
            if (tile.getLetter() < humanBest && !tile.isBlank()) {
                humanBest = tile.getLetter();
            }
        }
        for (Tile tile : computerPlayer.getRack().getTiles()) {
            if (tile.getLetter() < computerBest && !tile.isBlank()) {
                computerBest = tile.getLetter();
            }
        }
        currentPlayer = (humanBest <= computerBest) ? humanPlayer : computerPlayer;
        notifyGameStateChanged();
    }

    /**
     * Executes a player's move
     * @param move the move made by the player
     * @return true if the move was successful, false otherwise
     */
    public boolean makeMove(Move move) {
        if (gameOver || !isValidMove(move)) {
            return false;
        }
        int moveScore = calculateScore(move);
        move.setScore(moveScore);
        board.placeTiles(move);
        currentPlayer.updateScore(moveScore);
        gameLog.add(String.format("%s played %s for %d points",
                currentPlayer.getName(),
                constructWordString(move),
                moveScore));
        currentPlayer.drawTiles(tileBag);
        consecutivePasses = 0;
        notifyMoveCompleted(currentPlayer, move, moveScore);
        if (checkGameOver()) {
            handleGameOver();
        } else {
            switchPlayer();
        }

        return true;
    }

    /**
     * Pass the player's turn
     * @return true if the turn was passed
     */
    public boolean passTurn() {
        if (gameOver) {
            return false;
        }
        consecutivePasses++;
        gameLog.add(currentPlayer.getName() + " passed their turn");
        System.out.println(currentPlayer.getName() + " passed their turn. " +
                "Consecutive passes: " + consecutivePasses);
        if (consecutivePasses >= MAX_PASSES) {
            handleGameOver();
        } else {
            switchPlayer();
        }
        notifyGameStateChanged();
        return true;
    }

    /**
     * Allow the player to exchange tiles
     * @param tilesToExchange the tiles to be exchanged
     * @return true if the tiles were successfully exchanged
     */
    public boolean exchangeTiles(List<Tile> tilesToExchange) {
        if (gameOver || tileBag.getRemainingTiles() < 7) {
            return false;
        }

        List<Tile> newTiles = currentPlayer.exchangeTiles(tilesToExchange,
                tileBag);
        if (newTiles != null) {
            gameLog.add(String.format("%s exchanged %d tiles",
                    currentPlayer.getName(),
                    tilesToExchange.size()));

            consecutivePasses = 0;
            switchPlayer();
            notifyGameStateChanged();
            return true;
        }

        return false;
    }

    /**
     * Validate if the move is legal
     * @param move the move to validate
     * @return true if the move is valid, false otherwise
     */
    private boolean isValidMove(Move move) {
        if (move == null || move.getTiles().isEmpty()) {
            return false;
        }
        if (board.isFirstMove()) {
            boolean usesCenterSquare = false;
            for (Position pos : move.getPositions()) {
                if (pos.getRow() == 7 && pos.getCol() == 7) {
                    usesCenterSquare = true;
                    break;
                }
            }
            if (!usesCenterSquare) {
                return false;
            }
        }
        if (!move.isValid()) {
            return false;
        }
        if (!board.isFirstMove() && !connectsToExistingTiles(move)) {
            return false;
        }
        List<String> wordsFormed = findWordsFormed(move);
        for (String word : wordsFormed) {
            if (!dictionary.isValidWord(word)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Find all words formed by the move
     * @param move the move made by the player
     * @return list of words formed by the move
     */
    private List<String> findWordsFormed(Move move) {
        List<String> words = new ArrayList<>();
        String mainWord = findWordInDirection(move, move.isHorizontal());
        if (mainWord != null && mainWord.length() > 1) {
            words.add(mainWord);
        }
        for (Position pos : move.getPositions()) {
            String crossWord = findWordInDirection(move, !move.isHorizontal(),
                    pos);
            if (crossWord != null && crossWord.length() > 1) {
                words.add(crossWord);
            }
        }
        return words;
    }

    /**
     * Find the word in the direction of the move
     * @param move the move being made
     * @param horizontal true if the word id placed horizontally, false if vertically
     * @return the word found in the specified direction
     */
    private String findWordInDirection(Move move, boolean horizontal) {
        Position start = findWordStart(move, horizontal);
        return findWordInDirection(move, horizontal, start);
    }

    /**
     * Finds the start of the word and the full word in the given direction
     * @param move the move made by the player
     * @param horizontal true if checking horizontally, false if vertically
     * @param startPos the starting position of the word
     * @return the full word found in the given direction
     */
    private String findWordInDirection(Move move, boolean horizontal,
                                       Position startPos) {
        StringBuilder word = new StringBuilder();
        Position current = new Position(startPos.getRow(), startPos.getCol());

        while (current != null) {
            Tile tile = null;
            tile = move.getTileAt(current);
            if (tile == null) {
                Square square = board.getSquare(current.getRow(),
                        current.getCol());
                if (square != null && !square.isEmpty()) {
                    tile = square.getTile();
                }
            }
            if (tile == null) {
                break;
            }
            word.append(tile.getLetter());
            if (horizontal) {
                current = new Position(current.getRow(),
                        current.getCol() + 1);
            } else {
                current = new Position(current.getRow() + 1,
                        current.getCol());
            }
            if (current.getRow() >= 15 || current.getCol() >= 15) {
                break;
            }
        }

        return word.length() > 0 ? word.toString() : null;
    }

    /**
     * Finds the start position of a word in the move
     * @param move the move being made
     * @param horizontal true if checking horizontally, false if vetically
     * @return the starting position of the word
     */
    private Position findWordStart(Move move, boolean horizontal) {
        Position start = move.getPositions().get(0);
        int row = start.getRow();
        int col = start.getCol();
        while (true) {
            Position prev;
            if (horizontal) {
                prev = new Position(row, col - 1);
            } else {
                prev = new Position(row - 1, col);
            }

            Square square = board.getSquare(prev.getRow(), prev.getCol());
            if (square == null || square.isEmpty()) {
                break;
            }

            row = prev.getRow();
            col = prev.getCol();
        }
        return new Position(row, col);
    }

    /**
     * Check if the move connects to existing tiles
     * @param move the move made by the player
     * @return true if the move connect to existing tiles, false otherwise
     */
    private boolean connectsToExistingTiles(Move move) {
        for (Position pos : move.getPositions()) {
            Position[] adjacent = {
                    new Position(pos.getRow() - 1, pos.getCol()),
                    new Position(pos.getRow() + 1, pos.getCol()),
                    new Position(pos.getRow(), pos.getCol() - 1),
                    new Position(pos.getRow(), pos.getCol() + 1)
            };

            for (Position adj : adjacent) {
                Square square = board.getSquare(adj.getRow(), adj.getCol());
                if (square != null && !square.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * Calculates the score of the move
     * @param move the move made by the player
     * @return the total score of the move
     */
    private int calculateScore(Move move) {
        int totalScore = 0;
        int wordMultiplier = 1;
        for (Position pos : move.getPositions()) {
            Square square = board.getSquare(pos.getRow(), pos.getCol());
            Tile tile = move.getTileAt(pos);

            totalScore += tile.getValue() * square.getLetterMultiplier();
            wordMultiplier *= square.getWordMultiplier();
        }

        totalScore *= wordMultiplier;
        if (move.getTiles().size() == 7) {
            totalScore += 50;
            gameLog.add(currentPlayer.getName() +
                    " scored a BINGO! (+50 points)");
        }

        return totalScore;
    }

    /**
     * Switches the turn to the next player
     */
    private void switchPlayer() {
        currentPlayer = (currentPlayer == humanPlayer) ?
                computerPlayer : humanPlayer;
        notifyGameStateChanged();
    }

    /**
     * Checks if the game is over
     * @return true if the game is over, false otherwise
     */
    private boolean checkGameOver() {
        return tileBag.isEmpty() &&
                (humanPlayer.getRack().isEmpty() ||
                        computerPlayer.getRack().isEmpty()) ||
                consecutivePasses >= MAX_PASSES;
    }

    /**
     * Handles the game over state
     */
    private void handleGameOver() {
        gameOver = true;
        calculateFinalScores();
        notifyGameOver();
    }

    /**
     * Calculates the final scores for both players
     */
    private void calculateFinalScores() {
        int humanDeduction = calculateRemainingTilePoints(humanPlayer.getRack());
        int computerDeduction = calculateRemainingTilePoints(computerPlayer.getRack());

        humanPlayer.updateScore(-humanDeduction);
        computerPlayer.updateScore(-computerDeduction);

        if (humanPlayer.getRack().isEmpty()) {
            humanPlayer.updateScore(computerDeduction);
            gameLog.add("Human player used all tiles! " +
                    "Adding opponent's remaining points.");
        } else if (computerPlayer.getRack().isEmpty()) {
            computerPlayer.updateScore(humanDeduction);
            gameLog.add("Computer player used all tiles! " +
                    "Adding opponent's remaining points.");
        }
        gameLog.add("Game Over!");
        gameLog.add(String.format("Final Scores - %s: %d, %s: %d",
                humanPlayer.getName(), humanPlayer.getScore(),
                computerPlayer.getName(), computerPlayer.getScore()));
    }

    /**
     * Calculates the point value of the remaining tiles
     * @param rack the player's tile rack
     * @return the total point value of the remaining tiles
     */
    private int calculateRemainingTilePoints(TileRack rack) {
        int points = 0;
        for (Tile tile : rack.getTiles()) {
            points += tile.getValue();
        }
        return points;
    }

    /**
     * Creates a string representing the word formed
     * @param move the move made by the player
     * @return the string representing the word formed
     */
    private String constructWordString(Move move) {
        StringBuilder sb = new StringBuilder();
        for (Tile tile : move.getTiles()) {
            sb.append(tile.getLetter());
        }
        return sb.toString();
    }

    /**
     * Gets the current state of the board
     * @return the game board
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Gets the player of the current turn
     * @return the current player
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Gets the human player
     * @return the human player
     */
    public Player getHumanPlayer() {
        return humanPlayer;
    }

    /**
     * Gets the computer player
     * @return the computer player
     */
    public Player getComputerPlayer() {
        return computerPlayer;
    }

    /**
     * Checks if the game is over
     * @return true if the game is over, false otherwise
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Gets the number of remaining tiles in the tile bag
     * @return the number of remaining tiles
     */
    public int getRemainingTiles() {
        return tileBag.getRemainingTiles();
    }

    /**
     * Retrieves the game logic
     * @return a list of strings representing the game
     */
    public List<String> getGameLog() {
        return new ArrayList<>(gameLog);
    }

    /**
     * Gets the dictionary used for word validation
     * @return the dictionary
     */
    public Dictionary getDictionary() {
        return dictionary;
    }

    /**
     * Retrieves the next move from the computer player if it's their turn
     * @return the move made by the computer
     */
    public Move getComputerMove() {
        if (currentPlayer == computerPlayer && !gameOver) {
            return computerPlayer.makeMove(board);
        }
        return null;
    }
}