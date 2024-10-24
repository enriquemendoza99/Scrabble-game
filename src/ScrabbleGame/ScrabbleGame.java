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

    public interface GameStateListener {
        void onGameStateChanged();
        void onMoveCompleted(Player player, Move move, int score);
        void onGameOver();
    }

    public ScrabbleGame(String dictionaryFile) throws IOException {
        // Initialize lists
        gameStateListeners = new ArrayList<>();
        gameLog = new ArrayList<>();

        // Load dictionary
        dictionary = new Dictionary();
        dictionary.loadDictionary(dictionaryFile);

        // Create board and tile bag
        board = new Board();
        tileBag = new TileBag();

        // Create players
        humanPlayer = new HumanPlayer("Human");
        computerPlayer = new ComputerPlayer("Computer", dictionary);

        // Initial tile draw
        humanPlayer.drawTiles(tileBag);
        computerPlayer.drawTiles(tileBag);

        // Game setup
        determineFirstPlayer();
        gameOver = false;
        consecutivePasses = 0;
    }

    public void addGameStateListener(GameStateListener listener) {
        if (listener != null) {
            gameStateListeners.add(listener);
        }
    }

    private void notifyGameStateChanged() {
        for (GameStateListener listener : gameStateListeners) {
            listener.onGameStateChanged();
        }
    }

    private void notifyMoveCompleted(Player player, Move move, int score) {
        for (GameStateListener listener : gameStateListeners) {
            listener.onMoveCompleted(player, move, score);
        }
    }

    private void notifyGameOver() {
        for (GameStateListener listener : gameStateListeners) {
            listener.onGameOver();
        }
    }

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

    public boolean makeMove(Move move) {
        if (gameOver || !isValidMove(move)) {
            return false;
        }

        // Calculate score before applying move
        int moveScore = calculateScore(move);
        move.setScore(moveScore);

        // Apply the move
        board.placeTiles(move);
        currentPlayer.updateScore(moveScore);

        // Log the move
        gameLog.add(String.format("%s played %s for %d points",
                currentPlayer.getName(),
                constructWordString(move),
                moveScore));

        // Draw new tiles
        currentPlayer.drawTiles(tileBag);

        // Reset consecutive passes
        consecutivePasses = 0;

        notifyMoveCompleted(currentPlayer, move, moveScore);

        // Check if game is over
        if (checkGameOver()) {
            handleGameOver();
        } else {
            switchPlayer();
        }

        return true;
    }

    public boolean passTurn() {
        if (gameOver) {
            return false;
        }

        consecutivePasses++;
        gameLog.add(currentPlayer.getName() + " passed their turn");
        System.out.println(currentPlayer.getName() + " passed their turn. Consecutive passes: " + consecutivePasses);

        if (consecutivePasses >= MAX_PASSES) {
            handleGameOver();
        } else {
            switchPlayer();
        }

        notifyGameStateChanged();
        return true;
    }

    public boolean exchangeTiles(List<Tile> tilesToExchange) {
        if (gameOver || tileBag.getRemainingTiles() < 7) {
            return false;
        }

        List<Tile> newTiles = currentPlayer.exchangeTiles(tilesToExchange, tileBag);
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

    private boolean isValidMove(Move move) {
        if (move == null || move.getTiles().isEmpty()) {
            return false;
        }

        // First move must use center square
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

        // Check if tiles are in a straight line
        if (!move.isValid()) {
            return false;
        }

        // Check connection to existing tiles (except first move)
        if (!board.isFirstMove() && !connectsToExistingTiles(move)) {
            return false;
        }

        // Validate all words formed
        List<String> wordsFormed = findWordsFormed(move);
        for (String word : wordsFormed) {
            if (!dictionary.isValidWord(word)) {
                return false;
            }
        }

        return true;
    }
    private List<String> findWordsFormed(Move move) {
        List<String> words = new ArrayList<>();

        // Find main word
        String mainWord = findWordInDirection(move, move.isHorizontal());
        if (mainWord != null && mainWord.length() > 1) {
            words.add(mainWord);
        }

        // Find crossing words
        for (Position pos : move.getPositions()) {
            String crossWord = findWordInDirection(move, !move.isHorizontal(), pos);
            if (crossWord != null && crossWord.length() > 1) {
                words.add(crossWord);
            }
        }

        return words;
    }
    private String findWordInDirection(Move move, boolean horizontal) {
        // Find start position of main word
        Position start = findWordStart(move, horizontal);
        return findWordInDirection(move, horizontal, start);
    }

    private String findWordInDirection(Move move, boolean horizontal, Position startPos) {
        StringBuilder word = new StringBuilder();
        Position current = new Position(startPos.getRow(), startPos.getCol());

        while (current != null) {
            Tile tile = null;

            // Check if there's a tile in the current move at this position
            tile = move.getTileAt(current);

            // If no tile in move, check board
            if (tile == null) {
                Square square = board.getSquare(current.getRow(), current.getCol());
                if (square != null && !square.isEmpty()) {
                    tile = square.getTile();
                }
            }

            if (tile == null) {
                break;
            }

            word.append(tile.getLetter());

            // Move to next position
            if (horizontal) {
                current = new Position(current.getRow(), current.getCol() + 1);
            } else {
                current = new Position(current.getRow() + 1, current.getCol());
            }

            // Check board boundaries
            if (current.getRow() >= 15 || current.getCol() >= 15) {
                break;
            }
        }

        return word.length() > 0 ? word.toString() : null;
    }
    private Position findWordStart(Move move, boolean horizontal) {
        Position start = move.getPositions().get(0);
        int row = start.getRow();
        int col = start.getCol();

        // Move backwards until we find the start of the word
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

    private boolean connectsToExistingTiles(Move move) {
        for (Position pos : move.getPositions()) {
            // Check adjacent positions
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

    private int calculateScore(Move move) {
        int totalScore = 0;
        int wordMultiplier = 1;

        // Calculate main word score
        for (Position pos : move.getPositions()) {
            Square square = board.getSquare(pos.getRow(), pos.getCol());
            Tile tile = move.getTileAt(pos);

            totalScore += tile.getValue() * square.getLetterMultiplier();
            wordMultiplier *= square.getWordMultiplier();
        }

        totalScore *= wordMultiplier;

        // Add bonus for using all tiles (50 points)
        if (move.getTiles().size() == 7) {
            totalScore += 50;
            gameLog.add(currentPlayer.getName() + " scored a BINGO! (+50 points)");
        }

        return totalScore;
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == humanPlayer) ? computerPlayer : humanPlayer;
        notifyGameStateChanged();
    }

    private boolean checkGameOver() {
        return tileBag.isEmpty() &&
                (humanPlayer.getRack().isEmpty() || computerPlayer.getRack().isEmpty()) ||
                consecutivePasses >= MAX_PASSES;
    }

    private void handleGameOver() {
        gameOver = true;
        calculateFinalScores();
        notifyGameOver();
    }

    private void calculateFinalScores() {
        // Deduct points for remaining tiles
        int humanDeduction = calculateRemainingTilePoints(humanPlayer.getRack());
        int computerDeduction = calculateRemainingTilePoints(computerPlayer.getRack());

        humanPlayer.updateScore(-humanDeduction);
        computerPlayer.updateScore(-computerDeduction);

        // If one player used all tiles, add opponent's remaining tile points
        if (humanPlayer.getRack().isEmpty()) {
            humanPlayer.updateScore(computerDeduction);
            gameLog.add("Human player used all tiles! Adding opponent's remaining points.");
        } else if (computerPlayer.getRack().isEmpty()) {
            computerPlayer.updateScore(humanDeduction);
            gameLog.add("Computer player used all tiles! Adding opponent's remaining points.");
        }

        gameLog.add("Game Over!");
        gameLog.add(String.format("Final Scores - %s: %d, %s: %d",
                humanPlayer.getName(), humanPlayer.getScore(),
                computerPlayer.getName(), computerPlayer.getScore()));
    }

    private int calculateRemainingTilePoints(TileRack rack) {
        int points = 0;
        for (Tile tile : rack.getTiles()) {
            points += tile.getValue();
        }
        return points;
    }

    private String constructWordString(Move move) {
        StringBuilder sb = new StringBuilder();
        for (Tile tile : move.getTiles()) {
            sb.append(tile.getLetter());
        }
        return sb.toString();
    }

    // Getters
    public Board getBoard() {
        return board;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Player getHumanPlayer() {
        return humanPlayer;
    }

    public Player getComputerPlayer() {
        return computerPlayer;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getRemainingTiles() {
        return tileBag.getRemainingTiles();
    }

    public List<String> getGameLog() {
        return new ArrayList<>(gameLog);
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public Move getComputerMove() {
        if (currentPlayer == computerPlayer && !gameOver) {
            return computerPlayer.makeMove(board);
        }
        return null;
    }
}