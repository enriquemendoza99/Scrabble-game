# Scrabble Game
This is a Java implementation of the classic Scrabble board game. 
It provides a console-based interface for two players to play Scrabble.
## At the top level:
### README.md
Project Description

This project implements a two-player Scrabble game where a human player 
competes against a computer opponent. The game follows standard Scrabble 
rules and uses a graphical user interface built with JavaFX.:

- Complete Scrabble board implementation with premium squares
- Drag-and-drop interface for tile placement
- Computer opponent with word-finding algorithm
- Word validation against a dictionary file
- Score calculation with letter and word multipliers
- Support for blank tiles
- Move validation and turn

Game Rules:

First word must:
Use at least two letters
Cover the center square
Form a valid word from the dictionary

Subsequent moves must:
Connect to existing words
Form valid words in all directions
Use only available tiles from player's rack

Scoring:
Letter points based on tile values
Premium squares (2x and 3x) for letters and words
50-point bonus for using all 7 tiles (Bingo)

Project Structure

- ScrabbleGame.ScrabbleGame.java: Main class to run the game.
- ScrabbleGame.Game.java: Manages the game state and rules.
- ScrabbleGame.ScrabbleGame.Board.java: Represents the Scrabble board.
- ScrabbleGame.Player.java: Manages player information, including rack and score.
- ScrabbleGame.Tile.java: Represents a single letter tile.
- ScrabbleGame.Square.java: Represents a single square on the board.
- ScrabbleGame.ScrabbleGame.Dictionary.java: Handles word validation.
- ScrabbleGame.TileBag.java: Manages the pool of tiles.

Scrabble Solver Algorithm and Data Structure Design

Data Structures

1. Board Representation

- 15x15 2D array of Square objects
- Each Square contains:

  - Current tile (or null if empty)
  - Letter multiplier (1x, 2x, 3x)
  - Word multiplier (1x, 2x, 3x)
  - Premium square usage status

2. Move Class
- List of tiles being played
- List of corresponding board positions
- Direction (horizontal/vertical)
- Score value
- Validity checking methods

3. Dictionary Storage
- HashSet for O(1) word lookup
- All words stored in uppercase for case-insensitive matching

4. Tile Management
- TileBag: ArrayList with random access for drawing
- TileRack: ArrayList limited to 7 tiles
- Tile objects with letter, value, and blank status

Algorithms
1. Computer Move Generation Algorithm
   - The computer move generation algorithm starts by checking if it's the first
   move, which requires special handling to ensure placement through the center 
   square. For regular moves, it first identifies anchor points that means empty 
   squares adjacent to existing tiles where new words can be placed. 
   For each anchor point, the algorithm tries to place words both horizontally 
   and vertically, checking each possible placement against the dictionary and 
   game rules. It calculates the score for each valid placement, keeping track 
   of the highest-scoring move found so far. The algorithm considers both the 
   main word being formed and any cross-words created, ensuring all are valid 
   dictionary words. It works within a time limit to maintain game flow, and 
   if no valid moves are found, it will pass its turn.
2. Word Finding Algorithm
   - The word finding algorithm examines the computer player's available tiles 
   to determine what words can be formed. It starts by counting the regular 
   letters in the rack and keeping track of any blank tiles separately. For each 
   word in the dictionary, it checks if that word can be constructed using the 
   available letters, considering blank tiles that can represent any letter. 
   The algorithm uses a letter-counting approach, where it verifies if the 
   player has enough of each letter needed to form the word. This process 
   creates a list of all possible words that could be played, which is then used 
   by the move generation algorithm to find the best placement on the board.
3. Move Validation Algorithm
   - The move validation algorithm ensures that all moves follow Scrabble rules. 
   First, it verifies basic requirements: tiles must be placed in a straight 
   line, must connect to existing words, and the first move must use the center 
   square. Then it checks word formation by examining the main word being 
   created and all cross-words formed where the new tiles intersect with 
   existing ones. All words formed must exist in the dictionary. 
   The algorithm also verifies that the player has all the required 
   tiles in their rack, properly handles blank tiles, and ensures the move 
   connects appropriately to existing words on the board. Only if all these 
   conditions are met is the move considered valid.
4. Score Calculation Algorithm
   - The score calculation algorithm determines the points earned for a move by 
   following official Scrabble scoring rules. It starts with the main word, 
   adding up the base value of each tile while applying any letter multipliers 
   from premium squares. Word multipliers (2x or 3x) are accumulated 
   and applied to the total word score. The algorithm then identifies and scores 
   any cross-words formed by the placement. Premium squares are only counted 
   when first used. If all seven tiles from the rack are used in one move 
   and a 50-point bonus is added. The final score is the sum of the 
   main word score, all cross-word scores, and any bonus points.

### .gitignore
This file tells git which files to you should not track with version control
### Jar file(s)
Executable jar file(s) with all resources needed to run.

## src/ folder
This contains your source code, organized into one or more packages.

## doc/ folder
Includes all documentation other than this README

### Object design diagram

The object design document should be in PDF format.
First page/slide is object diagram, with description of objects on the next page(s).

On more complicated projects, you may need additional diagrams to
clearly describe subcomponents.

### Other documentation

If you found it useful to document your projects in other ways (class
diagrams, algorithm description, tables of events, etc.) put the
documents here.

## resources/ folder

This is an optional folder that you'll include if you are using any
resource files (sounds, images, etc.)

## .github/ folder
This folder may be automatically generated by the Github Classroom bot and updated when we change the autograder settings.
You should not change anything here, but you may need to pull the changes from the server before you will be able to push your own code.

## dictionaries_and_examples
For the scrabble project, this folder contains example files you can use for testing your programs. See the readme in this folder for more details.
We may update the folder with additional files, so please leave this folder alone to avoid conflicts.
