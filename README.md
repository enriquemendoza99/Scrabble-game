# Scrabble Game — Java

A fully playable Scrabble implementation in Java with three components:
a GUI-based game using JavaFX, a word solver, and a score checker.

## Project Structure
src/

ScrabbleGame/    — Full GUI Scrabble game (JavaFX)

Solver/          — Word solver (finds best move on a board)

scorechecker/    — Score checker (validates and scores plays)

example_input.txt

example_score_input.txt

solver.jar

scorechecker.jar

dictionaries_and_examples/

sowpods.txt      — Main dictionary (279,000+ words)

twl06.txt        — Tournament word list

enable.txt       — Alternative word list

## How to Run

**GUI Game:**
1. Open the project in IntelliJ
2. Go to Run → Edit Configurations
3. Set Main class to `ScrabbleGame.Main`
4. Set Program arguments to `dictionaries_and_examples/sowpods.txt`
5. Set Working directory to the project root
6. Click Run

**Word Solver:**
cd src

java -cp ../out/production/project-3-scrabble-enriquemendoza99 Solver.Solver sowpods.txt

**Score Checker:**
cd src

java -jar scorechecker.jar sowpods.txt

## How to Play

1. The game determines who goes first by drawing tiles — lowest letter goes first
2. Drag tiles from your rack onto the board
3. Click **Play Move** to submit your word
4. Words must connect to existing tiles and be valid dictionary words
5. Click **Pass** to skip your turn or **Exchange Tiles** to swap tiles
6. The game ends when the tile bag is empty and a player empties their rack,
   or after 6 consecutive passes

## Scoring Rules
- Letter values apply to new tiles placed on premium squares
- Double Letter (DL) and Triple Letter (TL) multiply the letter value
- Double Word (DW) and Triple Word (TW) multiply the entire word score
- Premium squares only apply when a new tile is placed on them
- All cross words formed by a move are scored
- Using all 7 rack tiles in one move awards a 50-point bingo bonus

## File Manifest

**ScrabbleGame/**
1. `Main.java` — Entry point, launches the JavaFX application
2. `ScrabbleUI.java` — GUI with board rendering and drag-and-drop
3. `ScrabbleGame.java` — Core game logic, turn management, scoring
4. `Board.java` — 15x15 board with premium square layout
5. `Square.java` — Individual board square with multipliers
6. `HumanPlayer.java` — Human player with tile placement logic
7. `ComputerPlayer.java` — Computer AI with move search
8. `Player.java` — Abstract base class for players
9. `TileRack.java` — Player tile rack (7 tiles)
10. `TileBag.java` — Standard Scrabble tile distribution (100 tiles)
11. `Tile.java` — Individual tile with letter and value
12. `Dictionary.java` — Word list loader and validator
13. `Move.java` — Represents a tile placement move
14. `Position.java` — Board position (row, col)
15. `SquareType.java` — Enum for premium square types

**Solver/**
1. `Solver.java` — Finds the highest-scoring legal move on a given board

**scorechecker/**
1. `ScoreChecker.java` — Validates plays and calculates scores

## src/ folder
This contains your source code, organized into one or more packages.

## doc/ folder
Includes the object desing diagram.

## dictionaries_and_examples
This folder contains example files you can use for testing your programs. See the readme in this folder for more details.
