package ScrabbleGame;

import java.util.*;
public class ComputerPlayer extends Player {
    private Dictionary dictionary;
    private static final int MAX_COMPUTE_TIME = 2000; // 2 seconds in milliseconds
    private Random random;

    public ComputerPlayer(String name, Dictionary dictionary) {
        super(name);
        this.dictionary = dictionary;
        this.isComputer = true;
        this.random = new Random();
    }

    @Override
    public Move makeMove(Board board) {
        System.out.println("Computer trying to make a move");

        // First try to find a valid move
        Move bestMove = findBestMove(board);

        if (bestMove == null) {
            System.out.println("Computer couldn't find any valid moves");
            return null; // This will trigger a pass
        }

        System.out.println("Computer found move with score: " + bestMove.getScore());
        return bestMove;
    }
    private Move findBestMove(Board board) {
        // For first move
        if (board.isFirstMove()) {
            return findFirstMove(board);
        }

        // Find best move
        long startTime = System.currentTimeMillis();
        Move bestMove = null;
        int bestScore = 0;

        // Find all anchor points
        List<Position> anchors = findAnchors(board);
        System.out.println("Found " + anchors.size() + " anchor points");

        // Try moves at each anchor point
        for (Position anchor : anchors) {
            if (System.currentTimeMillis() - startTime > MAX_COMPUTE_TIME) {
                break;
            }

            // Try horizontal moves
            List<Move> horizontalMoves = findMovesFromAnchor(board, anchor, true);
            for (Move move : horizontalMoves) {
                int score = calculateScore(board, move);
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            }

            // Try vertical moves
            List<Move> verticalMoves = findMovesFromAnchor(board, anchor, false);
            for (Move move : verticalMoves) {
                int score = calculateScore(board, move);
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            }
        }

        return bestMove; // Will be null if no valid move found
    }

    private Move findFirstMove(Board board) {
        List<String> possibleWords = findPossibleWords(rack.getTiles());
        Move bestMove = null;
        int bestScore = 0;

        for (String word : possibleWords) {
            if (word.length() >= 2) {  // First word must be at least 2 letters
                Move move = createMove(word, new Position(7, 7), true);
                if (move != null) {
                    int score = calculateScore(board, move);
                    if (score > bestScore) {
                        bestScore = score;
                        bestMove = move;
                    }
                }
            }
        }

        if (bestMove != null) {
            bestMove.setScore(bestScore);
        }
        return bestMove;
    }

    private List<Position> findAnchors(Board board) {
        List<Position> anchors = new ArrayList<>();
        boolean[][] visited = new boolean[15][15];

        // Find all squares adjacent to existing tiles
        for (int row = 0; row < 15; row++) {
            for (int col = 0; col < 15; col++) {
                if (!board.getSquare(row, col).isEmpty()) {
                    // Check adjacent squares
                    checkAndAddAnchor(board, row-1, col, anchors, visited);
                    checkAndAddAnchor(board, row+1, col, anchors, visited);
                    checkAndAddAnchor(board, row, col-1, anchors, visited);
                    checkAndAddAnchor(board, row, col+1, anchors, visited);
                }
            }
        }
        return anchors;
    }

    private void checkAndAddAnchor(Board board, int row, int col,
                                   List<Position> anchors, boolean[][] visited) {
        if (row >= 0 && row < 15 && col >= 0 && col < 15 &&
                !visited[row][col] && board.getSquare(row, col).isEmpty()) {
            anchors.add(new Position(row, col));
            visited[row][col] = true;
        }
    }

    private List<Move> findMovesFromAnchor(Board board, Position anchor, boolean horizontal) {
        List<Move> moves = new ArrayList<>();
        List<String> possibleWords = findPossibleWords(rack.getTiles());

        for (String word : possibleWords) {
            // Try placing the word in different positions relative to the anchor
            for (int i = 0; i < word.length(); i++) {
                Position start;
                if (horizontal) {
                    // Check if word would extend beyond board left edge
                    if (anchor.getCol() - i < 0) continue;
                    // Check if word would extend beyond board right edge
                    if (anchor.getCol() - i + word.length() > 15) continue;
                    start = new Position(anchor.getRow(), anchor.getCol() - i);
                } else {
                    // Check if word would extend beyond board top edge
                    if (anchor.getRow() - i < 0) continue;
                    // Check if word would extend beyond board bottom edge
                    if (anchor.getRow() - i + word.length() > 15) continue;
                    start = new Position(anchor.getRow() - i, anchor.getCol());
                }

                Move move = createMove(word, start, horizontal);
                if (move != null && isValidMove(board, move)) {
                    moves.add(move);
                }
            }
        }

        return moves;
    }

    private List<String> findPossibleWords(List<Tile> tiles) {
        List<String> words = new ArrayList<>();
        StringBuilder rackLetters = new StringBuilder();
        int blankCount = 0;

        // Build rack letters string and count blanks
        for (Tile tile : tiles) {
            if (tile.isBlank()) {
                blankCount++;
            } else {
                rackLetters.append(tile.getLetter());
            }
        }

        // Check each dictionary word
        for (String word : dictionary.getWords()) {
            if (canMakeWord(word, rackLetters.toString(), blankCount)) {
                words.add(word);
            }
        }
        return words;
    }

    private boolean canMakeWord(String word, String rackLetters, int blanks) {
        Map<Character, Integer> letterCount = new HashMap<>();

        // Count letters needed for word
        for (char c : word.toUpperCase().toCharArray()) {
            letterCount.merge(c, 1, Integer::sum);
        }

        // Subtract available letters
        for (char c : rackLetters.toUpperCase().toCharArray()) {
            letterCount.merge(c, -1, Integer::sum);
        }

        // Count how many letters we need blanks for
        int neededBlanks = 0;
        for (int count : letterCount.values()) {
            if (count > 0) {
                neededBlanks += count;
            }
        }

        return neededBlanks <= blanks;
    }

    private Move createMove(String word, Position start, boolean horizontal) {
        Move move = new Move();
        move.setHorizontal(horizontal);

        List<Tile> availableTiles = new ArrayList<>(rack.getTiles());

        // Try to create the word using available tiles
        for (int i = 0; i < word.length(); i++) {
            char letter = word.charAt(i);
            Position pos = horizontal ?
                    new Position(start.getRow(), start.getCol() + i) :
                    new Position(start.getRow() + i, start.getCol());

            // Try to find a regular tile first
            Tile tile = null;
            for (Tile t : availableTiles) {
                if (!t.isBlank() && t.getLetter() == letter) {
                    tile = t;
                    break;
                }
            }

            // If no regular tile found, try to use a blank
            if (tile == null) {
                for (Tile t : availableTiles) {
                    if (t.isBlank()) {
                        tile = t;
                        tile.setLetter(letter);
                        break;
                    }
                }
            }

            // If we couldn't find a tile at all, the move is impossible
            if (tile == null) {
                return null;
            }

            availableTiles.remove(tile);
            move.addTile(tile, pos);
        }

        return move;
    }

    private boolean isValidMove(Board board, Move move) {
        // First check basic move validity
        if (!move.isValid()) {
            return false;
        }

        // For first move
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
        } else {
            // Check connection to existing tiles
            boolean connected = false;
            for (Position pos : move.getPositions()) {
                if (hasAdjacentTile(board, pos, move)) {
                    connected = true;
                    break;
                }
            }
            if (!connected) {
                return false;
            }
        }

        // Check all words formed are valid
        List<String> wordsFormed = findWordsFormed(board, move);
        for (String word : wordsFormed) {
            if (!dictionary.isValidWord(word)) {
                return false;
            }
        }

        return true;
    }

    private boolean hasAdjacentTile(Board board, Position pos, Move move) {
        int[][] directions = {{-1,0}, {1,0}, {0,-1}, {0,1}};
        for (int[] dir : directions) {
            int newRow = pos.getRow() + dir[0];
            int newCol = pos.getCol() + dir[1];
            if (newRow >= 0 && newRow < 15 && newCol >= 0 && newCol < 15) {
                if (!board.getSquare(newRow, newCol).isEmpty() &&
                        !containsPosition(move.getPositions(), newRow, newCol)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsPosition(List<Position> positions, int row, int col) {
        for (Position pos : positions) {
            if (pos.getRow() == row && pos.getCol() == col) {
                return true;
            }
        }
        return false;
    }

    private List<String> findWordsFormed(Board board, Move move) {
        List<String> words = new ArrayList<>();

        // Find main word
        StringBuilder mainWord = new StringBuilder();
        List<Position> positions = move.getPositions();
        positions.sort((p1, p2) -> move.isHorizontal() ?
                p1.getCol() - p2.getCol() : p1.getRow() - p2.getRow());

        for (Position pos : positions) {
            mainWord.append(move.getTileAt(pos).getLetter());
        }
        words.add(mainWord.toString());

        // Find crossing words
        for (Position pos : positions) {
            String crossWord = findCrossWord(board, move, pos);
            if (crossWord != null && crossWord.length() > 1) {
                words.add(crossWord);
            }
        }

        return words;
    }

    private String findCrossWord(Board board, Move move, Position pos) {
        if (move.isHorizontal()) {
            return findWordInDirection(board, move, pos, false);
        } else {
            return findWordInDirection(board, move, pos, true);
        }
    }
    private List<String> findCrossWords(Board board, Move move) {
        List<String> crossWords = new ArrayList<>();
        boolean isHorizontal = move.isHorizontal();

        for (Position pos : move.getPositions()) {
            StringBuilder crossWord = new StringBuilder();


            int row = pos.getRow();
            int col = pos.getCol();

            if (isHorizontal) {

                while (row > 0 && !board.getSquare(row - 1, col).isEmpty()) {
                    row--;
                }
            } else {
                while (col > 0 && !board.getSquare(row, col - 1).isEmpty()) {
                    col--;
                }
            }

            Position current = new Position(row, col);
            while (current != null &&
                    (board.getSquare(current.getRow(), current.getCol()) != null) &&
                    (!board.getSquare(current.getRow(), current.getCol()).isEmpty() ||
                            containsPosition(move.getPositions(), current))) {

                Square square = board.getSquare(current.getRow(), current.getCol());
                if (!square.isEmpty()) {
                    crossWord.append(square.getTile().getLetter());
                } else {
                    Tile moveTile = getTileAtPosition(move, current);
                    if (moveTile != null) {
                        crossWord.append(moveTile.getLetter());
                    }
                }

                if (isHorizontal) {
                    current = new Position(current.getRow() + 1, current.getCol());
                } else {
                    current = new Position(current.getRow(), current.getCol() + 1);
                }

                if (current.getRow() >= 15 || current.getCol() >= 15) {
                    break;
                }
            }

            String word = crossWord.toString();
            if (word.length() > 1) {
                crossWords.add(word);
            }
        }

        return crossWords;
    }
    private String findWordInDirection(Board board, Move move, Position pos, boolean horizontal) {
        StringBuilder word = new StringBuilder();
        Position current = pos;

        // Go backwards
        while (true) {
            Position prev = horizontal ?
                    new Position(current.getRow(), current.getCol() - 1) :
                    new Position(current.getRow() - 1, current.getCol());

            if (prev.getRow() < 0 || prev.getCol() < 0) break;

            Square square = board.getSquare(prev.getRow(), prev.getCol());
            if (square.isEmpty()) break;

            word.insert(0, square.getTile().getLetter());
            current = prev;
        }

        // Add current letter
        word.append(move.getTileAt(pos).getLetter());

        // Go forwards
        current = pos;
        while (true) {
            Position next = horizontal ?
                    new Position(current.getRow(), current.getCol() + 1) :
                    new Position(current.getRow() + 1, current.getCol());

            if (next.getRow() >= 15 || next.getCol() >= 15) break;

            Square square = board.getSquare(next.getRow(), next.getCol());
            if (square.isEmpty()) break;

            word.append(square.getTile().getLetter());
            current = next;
        }

        return word.length() > 1 ? word.toString() : null;
    }

    private int calculateScore(Board board, Move move) {
        int totalScore = 0;
        int wordMultiplier = 1;

        for (int i = 0; i < move.getTiles().size(); i++) {
            Tile tile = move.getTiles().get(i);
            Position pos = move.getPositions().get(i);
            Square square = board.getSquare(pos.getRow(), pos.getCol());

            int letterScore = tile.getValue() * square.getLetterMultiplier();
            totalScore += letterScore;


            wordMultiplier *= square.getWordMultiplier();
        }
        totalScore *= wordMultiplier;
        List<String> crossWords = findCrossWords(board, move);
        for (String word : crossWords) {
            totalScore += calculateCrossWordScore(board, move, word);
        }
        if (move.getTiles().size() == 7) {
            totalScore += 50;
        }

        return totalScore;
    }
    private int calculateCrossWordScore(Board board, Move move, String word) {
        int score = 0;
        int wordMultiplier = 1;

        for (char c : word.toCharArray()) {
            Tile tile = new Tile(c, getTileValue(c));

            Position pos = getPositionForLetter(move, c);
            if (pos != null) {
                Square square = board.getSquare(pos.getRow(), pos.getCol());
                score += tile.getValue() * square.getLetterMultiplier();
                wordMultiplier *= square.getWordMultiplier();
            } else {
                score += tile.getValue();
            }
        }

        return score * wordMultiplier;
    }
    private boolean containsPosition(List<Position> positions, Position pos) {
        for (Position p : positions) {
            if (p.getRow() == pos.getRow() && p.getCol() == pos.getCol()) {
                return true;
            }
        }
        return false;
    }
    private Tile getTileAtPosition(Move move, Position pos) {
        for (int i = 0; i < move.getPositions().size(); i++) {
            Position movePos = move.getPositions().get(i);
            if (movePos.getRow() == pos.getRow() && movePos.getCol() == pos.getCol()) {
                return move.getTiles().get(i);
            }
        }
        return null;
    }

    private Position getPositionForLetter(Move move, char letter) {
        for (int i = 0; i < move.getTiles().size(); i++) {
            if (move.getTiles().get(i).getLetter() == letter) {
                return move.getPositions().get(i);
            }
        }
        return null;
    }

    private int getTileValue(char letter) {
        switch (Character.toUpperCase(letter)) {
            case 'A': case 'E': case 'I': case 'O': case 'U': case 'N': case 'R': case 'S': case 'T': case 'L':
                return 1;
            case 'D': case 'G':
                return 2;
            case 'B': case 'C': case 'M': case 'P':
                return 3;
            case 'F': case 'H': case 'V': case 'W': case 'Y':
                return 4;
            case 'K':
                return 5;
            case 'J': case 'X':
                return 8;
            case 'Q': case 'Z':
                return 10;
            default:
                return 0;
        }
    }
    @Override
    public List<Tile> exchangeTiles(List<Tile> tilesToExchange, TileBag bag) {
        // If no specific tiles were provided, choose tiles to exchange
        if (tilesToExchange == null || tilesToExchange.isEmpty()) {
            tilesToExchange = selectTilesToExchange();
        }
        return super.exchangeTiles(tilesToExchange, bag);
    }
    private List<Tile> selectTilesToExchange() {
        List<Tile> tilesToExchange = new ArrayList<>();
        int vowelCount = 0;
        int consonantCount = 0;

        // Count vowels and consonants
        for (Tile tile : rack.getTiles()) {
            char letter = tile.getLetter();
            if ("AEIOU".indexOf(letter) >= 0) {
                vowelCount++;
            } else if (letter != '*') {
                consonantCount++;
            }
        }

        // Exchange if rack is very unbalanced
        for (Tile tile : rack.getTiles()) {
            if (tilesToExchange.size() >= 3) break; // Exchange up to 3 tiles
            char letter = tile.getLetter();
            if (vowelCount >= 5 && "AEIOU".indexOf(letter) >= 0) {
                tilesToExchange.add(tile);
            } else if (consonantCount >= 6 && "AEIOU".indexOf(letter) < 0) {
                tilesToExchange.add(tile);
            }
        }

        return tilesToExchange;
    }
}
