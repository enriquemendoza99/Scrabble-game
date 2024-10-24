/**
 * Represents a dictionary of valid words for the game
 */
package ScrabbleGame;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public class Dictionary {
    private Set<String> words;

    /**
     * Creates an empty dictionary
     */
    public Dictionary() {
        words = new HashSet<>();
    }

    /**
     * Loads words from a text file into the dictionary
     * @param filename the path to the dictionary file to load
     * @throws IOException if there is an error reading the file
     */
    public void loadDictionary(String filename) throws IOException {
        try (BufferedReader reader =
                     new BufferedReader(new FileReader(filename))) {
            String word;
            while ((word = reader.readLine()) != null) {
                words.add(word.trim().toUpperCase());
            }
        }
    }

    /**
     * Checks if a word exists in the dictionary
     * @param word the word to check
     * @return true if the word exists in the dictionary, false otherwise
     */
    public boolean isValidWord(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        String normalizedWord = word.trim().toUpperCase();
        boolean isValid = words.contains(normalizedWord);
        System.out.println("Checking word: " + normalizedWord +
                " - Valid: " + isValid);
        return isValid;

    }

    /**
     * Returns a list of all words in the dictionary
     * @return a new array list containing all words in the dictionary
     */
    public List<String> getWords() {
        return new ArrayList<>(words);
    }

    /**
     * Gets the total number of words in the dictionary
     * @return the number of words stored int he dictionary
     */
    public int getSize() {
        return words.size();
    }
}