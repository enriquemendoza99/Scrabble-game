package ScrabbleGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ComputerPlayer extends Player {
    private Dictionary dictionary;
    private Random random;

    public ComputerPlayer(String name, Dictionary dictionary) {
        super(name);
        this.dictionary = dictionary;
        this.random = new Random();
    }

    @Override
    public void playTurn(Board board, TileBag tileBag, Dictionary dictionary) {
        Move bestMove = findBestMove(board);

        if (bestMove != null) {
            playMove(board, bestMove);
            System.out.println(name + " played: " + bestMove);
            addToScore(bestMove.getScore());
        } else {
            exchangeTiles(tileBag);
        }
    }

    private Move findBestMove(Board board) {
        List<Move> possibleMoves = new ArrayList<>();

        // Find all possible moves
        for (int row = 0; row < Board.BOARD_SIZE; row++) {
            for (int col = 0; col < Board.BOARD_SIZE; col++) {
                possibleMoves.addAll(findMovesStartingAt(board, row, col, true));
                possibleMoves.addAll(findMovesStartingAt(board, row, col, false));
            }
        }

        // Find the move with the highest score
        Move bestMove = null;
        int bestScore = 0;
        for (Move move : possibleMoves) {
            if (move.getScore() > bestScore) {
                bestMove = move;
                bestScore = move.getScore();
            }
        }

        return bestMove;
    }

    private List<Move> findMovesStartingAt(Board board, int startRow, int startCol, boolean horizontal) {
        List<Move> moves = new ArrayList<>();
        StringBuilder wordBuilder = new StringBuilder();
        List<Tile> usedTiles = new ArrayList<>();
        int row = startRow, col = startCol;
        int score = 0;
        int wordMultiplier = 1;

        while (board.isValidPosition(row, col)) {
            Square square = board.getSquare(row, col);
            if (square.isEmpty()) {
                for (Tile tile : rack) {
                    wordBuilder.append(tile.getLetter());
                    usedTiles.add(tile);
                    score += calculateLetterScore(tile, square);
                    wordMultiplier *= getWordMultiplier(square);

                    String word = wordBuilder.toString();
                    if (dictionary.isValidWord(word)) {
                        moves.add(new Move(word, startRow, startCol, horizontal, score * wordMultiplier, new ArrayList<>(usedTiles)));
                    }

                    // Backtrack
                    wordBuilder.setLength(wordBuilder.length() - 1);
                    usedTiles.remove(usedTiles.size() - 1);
                    score -= calculateLetterScore(tile, square);
                    wordMultiplier /= getWordMultiplier(square);
                }
                break;
            } else {
                Tile existingTile = square.getTile();
                wordBuilder.append(existingTile.getLetter());
                score += existingTile.getValue();
            }

            if (horizontal) {
                col++;
            } else {
                row++;
            }
        }

        return moves;
    }

    private int calculateLetterScore(Tile tile, Square square) {
        int score = tile.getValue();
        if (square.getType() == SquareType.DOUBLE_LETTER) {
            score *= 2;
        } else if (square.getType() == SquareType.TRIPLE_LETTER) {
            score *= 3;
        }
        return score;
    }

    private int getWordMultiplier(Square square) {
        if (square.getType() == SquareType.DOUBLE_WORD) {
            return 2;
        } else if (square.getType() == SquareType.TRIPLE_WORD) {
            return 3;
        }
        return 1;
    }

    private void playMove(Board board, Move move) {
        int row = move.getStartRow();
        int col = move.getStartCol();
        for (Tile tile : move.getUsedTiles()) {
            if (board.getSquare(row, col).isEmpty()) {
                board.placeTile(tile, row, col);
                rack.remove(tile);
            }
            if (move.isHorizontal()) {
                col++;
            } else {
                row++;
            }
        }
    }

    private void exchangeTiles(TileBag tileBag) {
        int exchangeCount = Math.min(rack.size(), tileBag.getRemainingTileCount());
        List<Tile> tilesToExchange = new ArrayList<>(rack.subList(0, exchangeCount));

        for (Tile tile : tilesToExchange) {
            rack.remove(tile);
            tileBag.addTile(tile);
        }

        drawTiles(tileBag, exchangeCount);
        System.out.println(name + " exchanged " + exchangeCount + " tiles.");
    }
}

class Move {
    private String word;
    private int startRow;
    private int startCol;
    private boolean horizontal;
    private int score;
    private List<Tile> usedTiles;

    public Move(String word, int startRow, int startCol, boolean horizontal, int score, List<Tile> usedTiles) {
        this.word = word;
        this.startRow = startRow;
        this.startCol = startCol;
        this.horizontal = horizontal;
        this.score = score;
        this.usedTiles = usedTiles;
    }

    // Getters
    public String getWord() { return word; }
    public int getStartRow() { return startRow; }
    public int getStartCol() { return startCol; }
    public boolean isHorizontal() { return horizontal; }
    public int getScore() { return score; }
    public List<Tile> getUsedTiles() { return usedTiles; }

    @Override
    public String toString() {
        return word + " at (" + startRow + "," + startCol + ") " + (horizontal ? "horizontally" : "vertically") + " for " + score + " points";
    }
}
