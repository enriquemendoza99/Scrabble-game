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

    public Dictionary() {
        words = new HashSet<>();
    }

    public void loadDictionary(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String word;
            while ((word = reader.readLine()) != null) {
                words.add(word.trim().toUpperCase());
            }
        }
    }

    public boolean isValidWord(String word) {
        return words.contains(word.toUpperCase());
    }

    public List<String> getWords() {
        return new ArrayList<>(words);
    }

    public int getSize() {
        return words.size();
    }
}