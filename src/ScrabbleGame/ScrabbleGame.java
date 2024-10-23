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
    private static final int MAX_PASSES = 6;
    private List<GameStateListener> gameStateListeners;

    public interface GameStateListener {
        void onGameStateChanged();
        void onMoveCompleted(Player player, Move move, int score);
        void onGameOver();
    }

    public ScrabbleGame(String dictionaryFile) {
        try {
            initialize(dictionaryFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize game: " + e.getMessage());
        }
    }

    private void initialize(String dictionaryFile) throws IOException {
        // Initialize components
        dictionary = new Dictionary();
        dictionary.loadDictionary(dictionaryFile);

        board = new Board();
        tileBag = new TileBag();
        gameLog = new ArrayList<>();
        gameStateListeners = new ArrayList<>();

        humanPlayer = new HumanPlayer("Human");
        computerPlayer = new ComputerPlayer("Computer", dictionary);

        // Initial tile draw
        humanPlayer.drawTiles(tileBag);
        computerPlayer.drawTiles(tileBag);

        // Determine who goes first
        determineFirstPlayer();

        gameOver = false;
        consecutivePasses = 0;
    }

    public void addGameStateListener(GameStateListener listener) {
        gameStateListeners.add(listener);
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
        // Find the player with the letter closest to 'A'
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
        String moveLog = String.format("%s played %s for %d points",
                currentPlayer.getName(),
                constructWordString(move),
                moveScore);
        gameLog.add(moveLog);

        // Draw new tiles
        currentPlayer.drawTiles(tileBag);

        // Reset consecutive passes since a move was made
        consecutivePasses = 0;

        notifyMoveCompleted(currentPlayer, move, moveScore);

        // Check if game is over
        if (checkGameOver()) {
            handleGameOver();
        } else {
            switchPlayer();
        }

        notifyGameStateChanged();
        return true;
    }

    public boolean passTurn() {
        if (gameOver) {
            return false;
        }

        consecutivePasses++;
        gameLog.add(currentPlayer.getName() + " passed their turn");

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

    public Move getComputerMove() {
        if (currentPlayer == computerPlayer && !gameOver) {
            return computerPlayer.makeMove(board);
        }
        return null;
    }

    private boolean isValidMove(Move move) {
        if (move == null || move.getTiles().isEmpty()) {
            return false;
        }

        // Validate tile ownership
        if (!hasRequiredTiles(currentPlayer, move)) {
            return false;
        }

        // Check if tiles are in a straight line
        if (!move.isValid()) {
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

        // Validate all words formed
        List<String> wordsFormed = findWordsFormed(move);
        for (String word : wordsFormed) {
            if (!dictionary.isValidWord(word)) {
                return false;
            }
        }

        // Check connection to existing tiles (except first move)
        if (!board.isFirstMove() && !connectsToExistingTiles(move)) {
            return false;
        }

        return true;
    }

    private boolean hasRequiredTiles(Player player, Move move) {
        Map<Character, Integer> rackTiles = new HashMap<>();
        int blanks = 0;

        // Count tiles in rack
        for (Tile tile : player.getRack().getTiles()) {
            if (tile.isBlank()) {
                blanks++;
            } else {
                rackTiles.merge(tile.getLetter(), 1, Integer::sum);
            }
        }

        // Check if player has all required tiles
        for (Tile tile : move.getTiles()) {
            if (tile.isBlank()) {
                if (blanks > 0) {
                    blanks--;
                } else {
                    return false;
                }
            } else {
                int count = rackTiles.getOrDefault(tile.getLetter(), 0);
                if (count > 0) {
                    rackTiles.put(tile.getLetter(), count - 1);
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    private List<String> findWordsFormed(Move move) {
        List<String> words = new ArrayList<>();

        // Find main word
        String mainWord = findWordAt(move.getPositions().get(0), move.isHorizontal());
        if (mainWord != null) {
            words.add(mainWord);
        }

        // Find crossing words
        for (Position pos : move.getPositions()) {
            String crossWord = findWordAt(pos, !move.isHorizontal());
            if (crossWord != null && crossWord.length() > 1) {
                words.add(crossWord);
            }
        }

        return words;
    }

    private String findWordAt(Position pos, boolean horizontal) {
        StringBuilder word = new StringBuilder();
        Position current = findWordStart(pos, horizontal);

        // Build word
        while (true) {
            Square square = board.getSquare(current.getRow(), current.getCol());
            if (square == null || square.isEmpty()) {
                break;
            }
            word.append(square.getTile().getLetter());

            current = horizontal ?
                    new Position(current.getRow(), current.getCol() + 1) :
                    new Position(current.getRow() + 1, current.getCol());
        }

        return word.length() > 0 ? word.toString() : null;
    }

    private Position findWordStart(Position pos, boolean horizontal) {
        Position current = pos;

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

        return current;
    }

    private boolean connectsToExistingTiles(Move move) {
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

    private int calculateScore(Move move) {
        int totalScore = 0;
        Set<String> wordsFormed = new HashSet<>(findWordsFormed(move));

        for (String word : wordsFormed) {
            totalScore += calculateWordScore(word, move);
        }

        // Add bingo bonus (50 points for using all 7 tiles)
        if (move.getTiles().size() == 7) {
            totalScore += 50;
            gameLog.add(currentPlayer.getName() + " scored a BINGO! (+50 points)");
        }

        return totalScore;
    }

    private int calculateWordScore(String word, Move move) {
        int score = 0;
        int wordMultiplier = 1;
        Position current = findWordStart(move.getPositions().get(0), move.isHorizontal());

        for (char c : word.toCharArray()) {
            Square square = board.getSquare(current.getRow(), current.getCol());
            Tile tile = square.getTile();

            score += tile.getValue() * square.getLetterMultiplier();
            wordMultiplier *= square.getWordMultiplier();

            current = move.isHorizontal() ?
                    new Position(current.getRow(), current.getCol() + 1) :
                    new Position(current.getRow() + 1, current.getCol());
        }

        return score * wordMultiplier;
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

        // Log final scores
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

    private void switchPlayer() {
        currentPlayer = (currentPlayer == humanPlayer) ? computerPlayer : humanPlayer;
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
}