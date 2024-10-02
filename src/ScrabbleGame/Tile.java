package ScrabbleGame;

public class Tile {
    private char letter;
    private int value;
    private boolean isBlank;

    public Tile(char letter, int value) {
        this.letter = Character.toUpperCase(letter);
        this.value = value;
        this.isBlank = (letter == '*');
    }

    public char getLetter() {
        return letter;
    }

    public int getValue() {
        return value;
    }

    public boolean isBlank() {
        return isBlank;
    }

    public void setBlankLetter(char letter) {
        if (isBlank) {
            this.letter = Character.toUpperCase(letter);
        }
    }

    @Override
    public String toString() {
        return String.valueOf(letter);
    }
}
