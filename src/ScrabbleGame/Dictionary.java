package ScrabbleGame;

import java.io.*;
import java.util.*;

public class Dictionary {
    private Set<String> words;

    public Dictionary(String dictionaryFile) throws IOException {
        words = new HashSet<>();
        loadDictionary(dictionaryFile);
    }

    private void loadDictionary(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                words.add(line.trim().toUpperCase());
            }
        }
    }

    public boolean isValidWord(String word) {
        return words.contains(word.toUpperCase());
    }

    public Set<String> getWords() {
        return new HashSet<>(words);
    }
}
