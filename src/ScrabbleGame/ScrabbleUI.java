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
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.input.*;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
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
    public static String dictionaryFile;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Verify dictionary file exists
            File dict = new File(dictionaryFile);
            if (!dict.exists()) {
                showError("Dictionary Error",
                        "Dictionary file"+ dictionaryFile+ "not found!\n");
                Platform.exit();
                return;
            }

            // Initialize game
            game = new ScrabbleGame(dictionaryFile);
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
                            "Please make sure the dictionary is valid.");
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
        controls.setPadding(new Insets(5));

        playButton = new Button("Play Move");
        playButton.setOnAction(e -> handlePlayMove());
        playButton.setStyle("-fx-font-weight: bold;");

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

            background = new Rectangle(SQUARE_SIZE - 1, SQUARE_SIZE - 1);
            background.setStroke(Color.BLACK);
            background.setStrokeWidth(0.5);

            letter = new Text();
            letter.setFont(Font.font("Arial", FontWeight.BOLD, 20));

            score = new Text();
            score.setFont(Font.font("Arial", 10));

            getChildren().addAll(background, letter, score);

            // Drag and drop handlers
            setOnDragEntered(e -> {
                if (e.getGestureSource() instanceof TileView && isEmpty()) {
                    background.setStroke(Color.YELLOW);
                    background.setStrokeWidth(2);
                }
                e.consume();
            });

            setOnDragExited(e -> {
                background.setStroke(Color.BLACK);
                background.setStrokeWidth(0.5);
                e.consume();
            });

            setOnDragOver(e -> {
                if (e.getGestureSource() instanceof TileView &&
                        !game.getCurrentPlayer().isComputer() &&
                        !game.isGameOver() &&
                        isEmpty()) {
                    e.acceptTransferModes(TransferMode.MOVE);
                }
                e.consume();
            });

            setOnDragDropped(e -> {
                Dragboard db = e.getDragboard();
                boolean success = false;

                if (db.hasString() && isEmpty()) {
                    TileView source = (TileView)e.getGestureSource();
                    success = handleTilePlacement(source.getTile(), row, col);
                }

                e.setDropCompleted(success);
                e.consume();
            });
        }

        private boolean isEmpty() {
            return game.getBoard().getSquare(row, col).isEmpty();
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
            if (game.getBoard().isFirstMove() && row == 7 && col == 7) {
                background.setFill(Color.GOLD);
                background.setStroke(Color.ORANGE);
                background.setStrokeWidth(2);
            } else if (square.getWordMultiplier() == 3) {
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
            setStyle("-fx-background-color: burlywood; -fx-border-color: brown; -fx-border-width: 2;");
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
        private boolean isDragging = false;
        private boolean hasBeenUsed = false;

        public TileView(Tile tile) {
            this.tile = tile;

            setPrefSize(SQUARE_SIZE, SQUARE_SIZE);
            setCursor(Cursor.HAND);

            Rectangle background = new Rectangle(SQUARE_SIZE - 2, SQUARE_SIZE - 2);
            background.setFill(Color.BEIGE);
            background.setStroke(Color.BLACK);
            background.setStrokeWidth(1);

            Text letter = new Text(String.valueOf(tile.getLetter()));
            letter.setFont(Font.font("Arial", FontWeight.BOLD, 20));

            Text value = new Text(String.valueOf(tile.getValue()));
            value.setFont(Font.font("Arial", 10));
            value.setTranslateY(10);

            getChildren().addAll(background, letter, value);

            setOnMouseEntered(e -> {
                if (!isDragging) {
                    background.setFill(Color.LIGHTYELLOW);
                }
            });

            setOnMouseExited(e -> {
                if (!isDragging) {
                    background.setFill(Color.BEIGE);
                }
            });

            setOnDragDetected(e -> {
                if (!game.getCurrentPlayer().isComputer() && !game.isGameOver()) {
                    isDragging = true;
                    Dragboard db = startDragAndDrop(TransferMode.MOVE);

                    SnapshotParameters sp = new SnapshotParameters();
                    sp.setFill(Color.TRANSPARENT);
                    WritableImage snapshot = snapshot(sp, null);
                    db.setDragView(snapshot, snapshot.getWidth() / 2, snapshot.getHeight() / 2);

                    ClipboardContent content = new ClipboardContent();
                    content.putString(tile.getLetter() + "," + tile.getValue() + "," + tile.isBlank());
                    db.setContent(content);

                    setOpacity(0.5);
                }
                e.consume();
            });

            setOnDragDone(e -> {
                isDragging = false;
                setOpacity(1.0);
                if (e.getTransferMode() == TransferMode.MOVE) {
                    hasBeenUsed = true;  // Actualizado para usar el nuevo nombre
                    setVisible(false);
                }
                e.consume();
            });
        }

        public Tile getTile() {
            return tile;
        }

        public boolean isUsed() {
            return hasBeenUsed;
        }

        public void setUsed(boolean used) {
            hasBeenUsed = used;
            setVisible(!used);
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
        if (game.getBoard().isFirstMove()) {
            if (!isValidFirstMove(currentMove)) {
                showMessage("Invalid first move! Please ensure:\n" +
                        "- At least two letters are played\n" +
                        "- One letter is on the center square (row 7, column 7)\n" +
                        "- Letters form a valid word");
                return;
            }
        }

        if (!player.validateCurrentMove(game.getBoard(), game.getDictionary())) {
            showMessage("Invalid move! Please check:\n" +
                    "- Words must be valid\n" +
                    "- Tiles must connect to existing ones\n" +
                    "- Letters must form a complete word");
            return;
        }
        for (Tile tile : currentMove.getTiles()) {
            player.getRack().removeTile(tile.getLetter());
        }

        if (game.makeMove(currentMove)) {
            handleComputerTurn();
        }
    }
    private boolean isValidFirstMove(Move move) {
        if (move.getTiles().size() < 2) {
            return false;
        }
        boolean usesCenterSquare = false;
        for (Position pos : move.getPositions()) {
            if (pos.getRow() == 7 && pos.getCol() == 7) {
                usesCenterSquare = true;
                break;
            }
        }
        if (!usesCenterSquare) {
            return false;
        }
        List<Position> positions = move.getPositions();
        if (positions.size() > 1) {
            boolean isHorizontal = true;
            boolean isVertical = true;
            int row = positions.get(0).getRow();
            int col = positions.get(0).getCol();

            for (int i = 1; i < positions.size(); i++) {
                Position pos = positions.get(i);
                if (pos.getRow() != row) {
                    isHorizontal = false;
                }
                if (pos.getCol() != col) {
                    isVertical = false;
                }
            }
            if (!isHorizontal && !isVertical) {
                return false;
            }
        }

        return true;
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
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
                Move computerMove = game.getComputerMove();
                if (computerMove != null) {
                    System.out.println("Computer making move: " + constructWordString(computerMove));
                    if (game.makeMove(computerMove)) {
                        System.out.println("Computer move successful");
                    } else {
                        System.out.println("Computer move failed");
                        game.passTurn(); // Pass turn if move fails
                    }
                } else {
                    System.out.println("Computer passing turn - no valid moves found");
                    game.passTurn();
                    showMessage("Computer passes turn - no valid moves available");
                }
                updateDisplay();
            }));
            timeline.play();
        }
    }
    private String constructWordString(Move move) {
        StringBuilder sb = new StringBuilder();
        for (Tile tile : move.getTiles()) {
            sb.append(tile.getLetter());
        }
        return sb.toString();
    }

    private boolean handleTilePlacement(Tile tile, int row, int col) {
        HumanPlayer player = (HumanPlayer)game.getHumanPlayer();
        Position position = new Position(row, col);
        boolean success = player.placeTile(tile, position, game.getBoard());

        if (success) {
            updateDisplay();
        }

        return success;
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

    private void showGameOverDialog(String winner) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(winner + " Wins!");
        alert.setContentText(String.format("Final Scores:\nHuman: %d\nComputer: %d",
                game.getHumanPlayer().getScore(),
                game.getComputerPlayer().getScore()));
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}