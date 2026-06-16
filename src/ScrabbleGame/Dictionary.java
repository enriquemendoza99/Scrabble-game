/**
 * Represents a dictionary of valid words for the game
 */
package ScrabbleGame;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a dictionary of valid Scrabble words loaded from a text file.
 */
public class Dictionary {
    private Set<String> words;

    public Dictionary() { words = new HashSet<>(); }

    /**
     * Loads words from a text file. Each line is treated as one word.
     */
    public void loadDictionary(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String word;
            while ((word = reader.readLine()) != null)
                words.add(word.trim().toUpperCase());
        }
    }

    /**
     * Returns true if the word exists in the dictionary (case-insensitive).
     */
    public boolean isValidWord(String word) {
        if (word == null || word.isEmpty()) return false;
        return words.contains(word.trim().toUpperCase());
    }

    public List<String> getWords() { return new ArrayList<>(words); }
    public int          getSize()  { return words.size(); }
}