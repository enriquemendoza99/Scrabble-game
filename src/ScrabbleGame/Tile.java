package ScrabbleGame;

public class Tile {
    private char letter;
    private int value;
    private boolean isBlank;

    public Tile(char letter, int value) {
        this.letter = letter;
        this.value = value;
        this.isBlank = false;
    }

    public Tile(char letter, int value, boolean isBlank) {
        this.letter = letter;
        this.value = value;
        this.isBlank = isBlank;
    }

    public char getLetter() {
        return letter;
    }

    public void setLetter(char letter) {
        if (isBlank) {
            this.letter = letter;
        }
    }

    public int getValue() {
        return isBlank ? 0 : value;
    }

    public boolean isBlank() {
        return isBlank;
    }

    @Override
    public String toString() {
        return String.valueOf(isBlank ? Character.toUpperCase(letter) : letter);
    }
}