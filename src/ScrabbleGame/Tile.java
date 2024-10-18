package ScrabbleGame;
public class Tile {
    private char letter;
    private int value;

    public Tile(char letter, int value) {
        this.letter = Character.toUpperCase(letter);
        this.value = value;
    }

    public char getLetter() {
        return letter;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(letter);
    }
}
