package ScrabbleGame;
public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Error: Provide a dictionary file");
            System.out.println("Usage: java ScrabbleGame.Main " +
                    "<dictionary-file>");
            System.exit(1);
        }
        String dictionaryFile = args[0];

        ScrabbleUI.dictionaryFile = dictionaryFile;

        ScrabbleUI.main(args);
    }
}