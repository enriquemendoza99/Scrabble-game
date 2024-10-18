package ScrabbleGame;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class ScrabbleGame extends Application {
    private Board board;
    private HumanPlayer humanPlayer;
    private ComputerPlayer computerPlayer;
    private TileBag tileBag;
    private Dictionary dictionary;
    private Player currentPlayer;

    private GridPane boardGrid;
    private HBox humanRack;
    private Label messageLabel;
    private Label scoreLabel;
    private Button playButton;
    private Button passButton;

    @Override
    public void start(Stage primaryStage) {
        initializeGame();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Create board grid
        boardGrid = createBoardGrid();
        root.setCenter(boardGrid);

        // Create player rack
        humanRack = new HBox(10);
        updateRackDisplay();

        // Create controls
        playButton = new Button("Play Word");
        playButton.setOnAction(e -> playTurn());
        passButton = new Button("Pass");
        passButton.setOnAction(e -> passTurn());

        HBox controls = new HBox(10, playButton, passButton);

        // Create message and score labels
        messageLabel = new Label("Human player's turn");
        scoreLabel = new Label("Human: 0 | Computer: 0");

        VBox bottomPane = new VBox(10, humanRack, controls, messageLabel, scoreLabel);
        root.setBottom(bottomPane);

        Scene scene = new Scene(root, 800, 700);
        primaryStage.setTitle("Scrabble Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeGame() {
        board = new Board();
        tileBag = new TileBag();
        dictionary = new Dictionary("path/to/dictionary/file.txt");
        humanPlayer = new HumanPlayer("Human");
        computerPlayer = new ComputerPlayer("Computer", dictionary);

        humanPlayer.drawTiles(tileBag, 7);
        computerPlayer.drawTiles(tileBag, 7);

        currentPlayer = humanPlayer;
    }

    private GridPane createBoardGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(1);
        grid.setVgap(1);

        for (int row = 0; row < Board.BOARD_SIZE; row++) {
            for (int col = 0; col < Board.BOARD_SIZE; col++) {
                Button square = createSquareButton(row, col);
                grid.add(square, col, row);
            }
        }

        return grid;
    }

    private Button createSquareButton(int row, int col) {
        Button square = new Button();
        square.setPrefSize(40, 40);
        square.setStyle("-fx-background-color: white; -fx-border-color: black;");

        Square boardSquare = board.getSquare(row, col);
        if (boardSquare.getTile() != null) {
            square.setText(String.valueOf(boardSquare.getTile().getLetter()));
        } else {
            switch (boardSquare.getType()) {
                case TRIPLE_WORD:
                    square.setStyle("-fx-background-color: red;");
                    break;
                case DOUBLE_WORD:
                    square.setStyle("-fx-background-color: pink;");
                    break;
                case TRIPLE_LETTER:
                    square.setStyle("-fx-background-color: darkblue;");
                    break;
                case DOUBLE_LETTER:
                    square.setStyle("-fx-background-color: lightblue;");
                    break;
            }
        }

        square.setOnAction(e -> handleSquareClick(row, col));
        return square;
    }

    private void handleSquareClick(int row, int col) {
        if (currentPlayer == humanPlayer) {
            humanPlayer.selectPosition(row, col);
            updateBoardDisplay();
        }
    }

    private void updateRackDisplay() {
        humanRack.getChildren().clear();
        for (Tile tile : humanPlayer.getRack()) {
            Button tileButton = new Button(String.valueOf(tile.getLetter()));
            tileButton.setPrefSize(30, 30);
            tileButton.setOnAction(e -> handleTileClick(tile));
            humanRack.getChildren().add(tileButton);
        }
    }

    private void handleTileClick(Tile tile) {
        if (currentPlayer == humanPlayer) {
            humanPlayer.selectTile(tile);
            updateRackDisplay();
        }
    }

    private void playTurn() {
        if (currentPlayer == humanPlayer) {
            if (humanPlayer.playWord(board, dictionary)) {
                updateBoardDisplay();
                updateScores();
                replenishTiles();
                switchToComputerTurn();
            } else {
                messageLabel.setText("Invalid word. Try again.");
            }
        }
    }

    private void passTurn() {
        if (currentPlayer == humanPlayer) {
            switchToComputerTurn();
        }
    }

    private void switchToComputerTurn() {
        currentPlayer = computerPlayer;
        messageLabel.setText("Computer player's turn");
        playButton.setDisable(true);
        passButton.setDisable(true);

        // Use a separate thread for computer's turn to keep UI responsive
        new Thread(() -> {
            computerPlayer.playTurn(board, tileBag, dictionary);
            javafx.application.Platform.runLater(() -> {
                updateBoardDisplay();
                updateScores();
                replenishTiles();
                switchToHumanTurn();
            });
        }).start();
    }

    private void switchToHumanTurn() {
        currentPlayer = humanPlayer;
        messageLabel.setText("Human player's turn");
        playButton.setDisable(false);
        passButton.setDisable(false);
        updateRackDisplay();
    }

    private void updateBoardDisplay() {
        for (int row = 0; row < Board.BOARD_SIZE; row++) {
            for (int col = 0; col < Board.BOARD_SIZE; col++) {
                Button square = (Button) boardGrid.getChildren().get(row * Board.BOARD_SIZE + col);
                Tile tile = board.getSquare(row, col).getTile();
                if (tile != null) {
                    square.setText(String.valueOf(tile.getLetter()));
                    square.setStyle("-fx-background-color: yellow;");
                }
            }
        }
    }

    private void updateScores() {
        scoreLabel.setText("Human: " + humanPlayer.getScore() + " | Computer: " + computerPlayer.getScore());
    }

    private void replenishTiles() {
        humanPlayer.drawTiles(tileBag, 7 - humanPlayer.getRack().size());
        computerPlayer.drawTiles(tileBag, 7 - computerPlayer.getRack().size());
    }

    private boolean isGameOver() {
        return tileBag.getRemainingTileCount() == 0 &&
                (humanPlayer.getRack().isEmpty() || computerPlayer.getRack().isEmpty());
    }

    private void endGame() {
        messageLabel.setText("Game Over!");
        playButton.setDisable(true);
        passButton.setDisable(true);

        int humanFinalScore = humanPlayer.getScore();
        int computerFinalScore = computerPlayer.getScore();

        // Subtract remaining tile values
        for (Tile tile : humanPlayer.getRack()) {
            humanFinalScore -= tile.getValue();
        }
        for (Tile tile : computerPlayer.getRack()) {
            computerFinalScore -= tile.getValue();
        }

        String winner = humanFinalScore > computerFinalScore ? "Human" : "Computer";
        scoreLabel.setText("Final Scores - Human: " + humanFinalScore + " | Computer: " + computerFinalScore +
                "\nWinner: " + winner);
    }

    public static void main(String[] args) {
        launch(args);
    }
}