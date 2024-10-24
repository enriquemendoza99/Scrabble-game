package ScrabbleGame;
public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Error: Please provide a dictionary file");
            System.out.println("Usage: java ScrabbleGame.Main <dictionary-file>");
            System.out.println("Example: java ScrabbleGame.Main sowpods.txt");
            System.exit(1);
        }

        // Store dictionary file path
        String dictionaryFile = args[0];

        // Pass dictionary file to ScrabbleUI
        ScrabbleUI.dictionaryFile = dictionaryFile;

        // Launch the JavaFX application
        ScrabbleUI.main(args);
    }
}