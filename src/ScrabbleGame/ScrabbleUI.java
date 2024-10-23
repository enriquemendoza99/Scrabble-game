package ScrabbleGame;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.input.*;
import java.io.File;
import java.util.*;

public class ScrabbleUI extends Application implements ScrabbleGame.GameStateListener {
    private ScrabbleGame game;
    private BoardPane boardPane;
    private RackPane rackPane;
    private Label scoreLabel;
    private Label messageLabel;
    private Button playButton;
    private Button exchangeButton;
    private Button passButton;

    private static final int SQUARE_SIZE = 40;
    private static final String DICTIONARY_FILE = "sowpods.txt";

    @Override
    public void start(Stage primaryStage) {
        try {
            // Verify dictionary file exists
            File dict = new File(DICTIONARY_FILE);
            if (!dict.exists()) {
                showError("Dictionary Error",
                        "Dictionary file 'sowpods.txt' not found!\n" +
                                "Please make sure 'sowpods.txt' is in the same directory as the program.");
                Platform.exit();
                return;
            }

            // Initialize game
            game = new ScrabbleGame(DICTIONARY_FILE);
            game.addGameStateListener(this);

            // Create main layout
            VBox root = new VBox(10);
            root.setPadding(new Insets(10));

            // Create main game area
            HBox mainArea = new HBox(20);
            mainArea.setAlignment(Pos.CENTER);

            // Create board
            boardPane = new BoardPane();

            // Create game info panel
            VBox gameInfo = createGameInfoPanel();

            mainArea.getChildren().addAll(boardPane, gameInfo);
            root.getChildren().add(mainArea);

            // Create player rack
            rackPane = new RackPane();
            root.getChildren().add(rackPane);

            // Create game controls
            HBox controls = createControlButtons();
            root.getChildren().add(controls);

            // Create scene
            Scene scene = new Scene(root);
            primaryStage.setTitle("Scrabble");
            primaryStage.setScene(scene);
            primaryStage.show();

            // Initial update
            updateDisplay();

            // Start computer move if computer goes first
            if (game.getCurrentPlayer().isComputer()) {
                Platform.runLater(this::handleComputerTurn);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Game Initialization Error",
                    "Failed to start the game: " + e.getMessage() + "\n" +
                            "Please make sure 'sowpods.txt' is present and valid.");
            Platform.exit();
        }
    }

    private VBox createGameInfoPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setAlignment(Pos.TOP_CENTER);

        scoreLabel = new Label();
        scoreLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        messageLabel = new Label();
        messageLabel.setWrapText(true);

        panel.getChildren().addAll(
                new Label("Game Status:"),
                scoreLabel,
                messageLabel
        );

        return panel;
    }

    private HBox createControlButtons() {
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER);

        playButton = new Button("Play Move");
        playButton.setOnAction(e -> handlePlayMove());

        exchangeButton = new Button("Exchange Tiles");
        exchangeButton.setOnAction(e -> handleExchangeTiles());

        passButton = new Button("Pass");
        passButton.setOnAction(e -> handlePass());

        controls.getChildren().addAll(playButton, exchangeButton, passButton);
        return controls;
    }

    private class BoardPane extends GridPane {
        private SquarePane[][] squares;

        public BoardPane() {
            squares = new SquarePane[15][15];
            setHgap(1);
            setVgap(1);
            setPadding(new Insets(10));
            setStyle("-fx-background-color: black;");

            for (int row = 0; row < 15; row++) {
                for (int col = 0; col < 15; col++) {
                    squares[row][col] = new SquarePane(row, col);
                    add(squares[row][col], col, row);
                }
            }
        }

        public void update() {
            Board gameBoard = game.getBoard();
            for (int row = 0; row < 15; row++) {
                for (int col = 0; col < 15; col++) {
                    squares[row][col].update(gameBoard.getSquare(row, col));
                }
            }
        }
    }
    private class SquarePane extends StackPane {
        private Rectangle background;
        private Text letter;
        private Text score;
        private int row;
        private int col;

        public SquarePane(int row, int col) {
            this.row = row;
            this.col = col;

            setPrefSize(SQUARE_SIZE, SQUARE_SIZE);

            background = new Rectangle(SQUARE_SIZE, SQUARE_SIZE);
            letter = new Text();
            score = new Text();

            letter.setFont(Font.font(20));
            score.setFont(Font.font(10));

            getChildren().addAll(background, letter, score);

            setOnDragOver(e -> {
                if (e.getGestureSource() instanceof TileView &&
                        !game.getCurrentPlayer().isComputer() &&
                        !game.isGameOver()) {
                    e.acceptTransferModes(TransferMode.MOVE);
                }
                e.consume();
            });

            setOnDragDropped(e -> {
                TileView source = (TileView)e.getGestureSource();
                if (source != null) {
                    handleTilePlacement(source.getTile(), row, col);
                }
                e.consume();
            });
        }

        public void update(Square square) {
            if (square.isEmpty()) {
                setSquareColor(square);
                letter.setText("");
                score.setText("");
            } else {
                background.setFill(Color.BEIGE);
                Tile tile = square.getTile();
                letter.setText(String.valueOf(tile.getLetter()));
                score.setText(String.valueOf(tile.getValue()));
            }
        }

        private void setSquareColor(Square square) {
            if (square.getWordMultiplier() == 3) {
                background.setFill(Color.RED);
            } else if (square.getWordMultiplier() == 2) {
                background.setFill(Color.PINK);
            } else if (square.getLetterMultiplier() == 3) {
                background.setFill(Color.BLUE);
            } else if (square.getLetterMultiplier() == 2) {
                background.setFill(Color.LIGHTBLUE);
            } else {
                background.setFill(Color.WHITE);
            }
        }
    }

    private class RackPane extends HBox {
        private List<TileView> tileViews;

        public RackPane() {
            setSpacing(5);
            setPadding(new Insets(10));
            setAlignment(Pos.CENTER);
            setStyle("-fx-background-color: burlywood;");
            tileViews = new ArrayList<>();
        }

        public void update() {
            getChildren().clear();
            tileViews.clear();

            TileRack rack = game.getHumanPlayer().getRack();
            for (Tile tile : rack.getTiles()) {
                TileView tileView = new TileView(tile);
                tileViews.add(tileView);
                getChildren().add(tileView);
            }
        }
    }

    private class TileView extends StackPane {
        private Tile tile;

        public TileView(Tile tile) {
            this.tile = tile;

            setPrefSize(SQUARE_SIZE, SQUARE_SIZE);

            Rectangle background = new Rectangle(SQUARE_SIZE - 2, SQUARE_SIZE - 2);
            background.setFill(Color.BEIGE);
            background.setStroke(Color.BLACK);

            Text letter = new Text(String.valueOf(tile.getLetter()));
            letter.setFont(Font.font(20));

            Text value = new Text(String.valueOf(tile.getValue()));
            value.setFont(Font.font(10));
            value.setTranslateY(10);

            getChildren().addAll(background, letter, value);

            setOnDragDetected(e -> {
                if (!game.getCurrentPlayer().isComputer() && !game.isGameOver()) {
                    Dragboard db = startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(String.valueOf(tile.getLetter()));
                    db.setContent(content);
                    e.consume();
                }
            });
        }

        public Tile getTile() {
            return tile;
        }
    }

    @Override
    public void onGameStateChanged() {
        Platform.runLater(this::updateDisplay);
    }

    @Override
    public void onMoveCompleted(Player player, Move move, int score) {
        Platform.runLater(() -> {
            updateDisplay();
            if (player.isComputer()) {
                showMessage(String.format("Computer scored %d points", score));
            }
        });
    }

    @Override
    public void onGameOver() {
        Platform.runLater(() -> {
            updateDisplay();
            String winner = game.getHumanPlayer().getScore() > game.getComputerPlayer().getScore() ?
                    "Human" : "Computer";
            showMessage("Game Over! " + winner + " wins!");
            playButton.setDisable(true);
            exchangeButton.setDisable(true);
            passButton.setDisable(true);

            showGameOverDialog(winner);
        });
    }

    private void showGameOverDialog(String winner) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(winner + " Wins!");
        alert.setContentText(String.format("Final Scores:\nHuman: %d\nComputer: %d",
                game.getHumanPlayer().getScore(),
                game.getComputerPlayer().getScore()));
        alert.showAndWait();
    }

    private void updateDisplay() {
        boardPane.update();
        rackPane.update();
        updateScoreLabel();
        updateControls();
    }

    private void updateScoreLabel() {
        scoreLabel.setText(String.format("Human: %d  Computer: %d\nTiles Remaining: %d",
                game.getHumanPlayer().getScore(),
                game.getComputerPlayer().getScore(),
                game.getRemainingTiles()));
    }

    private void updateControls() {
        boolean isHumanTurn = !game.getCurrentPlayer().isComputer() && !game.isGameOver();
        playButton.setDisable(!isHumanTurn);
        exchangeButton.setDisable(!isHumanTurn || game.getRemainingTiles() < 7);
        passButton.setDisable(!isHumanTurn);
    }

    private void handlePlayMove() {
        HumanPlayer player = (HumanPlayer)game.getHumanPlayer();
        Move currentMove = player.getCurrentMove();

        if (!player.validateCurrentMove(game.getBoard(), game.getDictionary())) {
            showMessage("Invalid move! Please check:\n" +
                    "- Words must be valid\n" +
                    "- Tiles must connect to existing ones\n" +
                    "- First move must use center square");
            return;
        }

        if (game.makeMove(currentMove)) {
            handleComputerTurn();
        }
    }

    private void handleExchangeTiles() {
        List<Tile> selectedTiles = new ArrayList<>(game.getHumanPlayer().getRack().getTiles());
        if (selectedTiles.isEmpty()) {
            showMessage("No tiles to exchange");
            return;
        }

        if (game.exchangeTiles(selectedTiles)) {
            handleComputerTurn();
        } else {
            showMessage("Cannot exchange tiles - not enough tiles in bag");
        }
    }

    private void handlePass() {
        if (game.passTurn()) {
            handleComputerTurn();
        }
    }

    private void handleComputerTurn() {
        if (game.getCurrentPlayer().isComputer() && !game.isGameOver()) {
            Move move = game.getComputerMove();
            if (move != null) {
                game.makeMove(move);
            } else {
                game.passTurn();
            }
        }
    }

    private void handleTilePlacement(Tile tile, int row, int col) {
        HumanPlayer player = (HumanPlayer)game.getHumanPlayer();
        Position position = new Position(row, col);
        if (player.placeTile(tile, position, game.getBoard())) {
            updateDisplay();
        } else {
            showMessage("Cannot place tile here");
        }
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}