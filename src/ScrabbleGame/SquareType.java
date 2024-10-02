package ScrabbleGame;

public enum SquareType {
    NORMAL('.'),
    DOUBLE_LETTER('D'),
    TRIPLE_LETTER('T'),
    DOUBLE_WORD('d'),
    TRIPLE_WORD('t');

    private final char symbol;

    SquareType(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }
}
