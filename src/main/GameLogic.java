package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Shared game state and rules for every Peg Solitaire board shape.
 * Concrete subclasses are responsible only for creating their board layout.
 */
public abstract class GameLogic {
    private static final int[][] DIRECTIONS = {
        {0, -1},  // left
        {-1, 0},  // up
        {0, 1},   // right
        {1, 0},   // down
        {-1, -1}, // up-left
        {-1, 1},  // up-right
        {1, -1},  // down-left
        {1, 1}    // down-right
    };

    //Used for storing position of a given peg
    public static final class Peg {
        public final int row;
        public final int col;

        public Peg(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    //Used for handline movements within the game.
    private static final class Move {
    	//The position of peg to move
        private final int sourceRow;
        private final int sourceCol;
        //The peg to be jumped over
        private final int middleRow;
        private final int middleCol;
        //The location for the peg to move to
        private final int destinationRow;
        private final int destinationCol;

        private Move(int sourceRow, int sourceCol, int middleRow, int middleCol,
                     int destinationRow, int destinationCol) {
            this.sourceRow = sourceRow;
            this.sourceCol = sourceCol;
            this.middleRow = middleRow;
            this.middleCol = middleCol;
            this.destinationRow = destinationRow;
            this.destinationCol = destinationCol;
        }
    }

    // The board belongs to this game instance, not to the Swing Board panel.
    private char[][] board;
    private int boardSize;
    private Peg selectedPeg;
    private boolean gameStarted;
    private boolean manual = true;
    private boolean gameOver;

    /** Each subclass supplies the layout for the requested size. */
    public abstract void initializeBoard(int size);

    /**
     * Installs a defensive copy of a board layout. Subclasses call this from
     * initializeBoard so one game cannot accidentally mutate another game's
     * template or state.
     */
    protected final void setBoard(char[][] layout) {
        if (layout == null || layout.length == 0) {
            throw new IllegalArgumentException("Board layout cannot be empty.");
        }

        int columns = layout[0].length;
        if (columns == 0) {
            throw new IllegalArgumentException("Board rows cannot be empty.");
        }

        char[][] copy = new char[layout.length][columns];
        for (int row = 0; row < layout.length; row++) {
            if (layout[row] == null || layout[row].length != columns) {
                throw new IllegalArgumentException("Board layout must be rectangular.");
            }
            System.arraycopy(layout[row], 0, copy[row], 0, columns);
        }

        board = copy;
        boardSize = layout.length;
        selectedPeg = null;
        gameStarted = false;
        gameOver = false;
    }

    /** Returns a copy so callers cannot change the model behind its back. */
    public final char[][] getBoard() {
        char[][] copy = new char[board.length][];
        for (int row = 0; row < board.length; row++) {
            copy[row] = board[row].clone();
        }
        return copy;
    }

/**
 * Restores a previously saved board state.
 * The game remains unstarted until the user clicks Start Game.
 */
public final void restoreBoardState(char[][] savedBoard) {

    if (savedBoard == null || savedBoard.length != board.length) {
        throw new IllegalArgumentException("Invalid saved board.");
    }

    char[][] restoredBoard = new char[board.length][];

    for (int row = 0; row < board.length; row++) {

        if (savedBoard[row] == null
                || savedBoard[row].length != board[row].length) {
            throw new IllegalArgumentException("Invalid saved board dimensions.");
        }

        restoredBoard[row] = savedBoard[row].clone();

        for (int col = 0; col < board[row].length; col++) {

            char savedCell = savedBoard[row][col];

            // Ensure the saved board has the correct shape.
            if (board[row][col] == '?') {
                if (savedCell != '?') {
                    throw new IllegalArgumentException("Invalid board layout.");
                }
            } else if (savedCell != 'x' && savedCell != 'e') {
                throw new IllegalArgumentException("Invalid board cell.");
            }
        }
    }

    board = restoredBoard;
    selectedPeg = null;
    gameStarted = false;
    gameOver = false;
}

    public final char getCell(int row, int col) {
        if (!isInBounds(row, col)) {
            return '?';
        }
        return board[row][col];
    }

    public final int getBoardSize() {
        return boardSize;
    }

    public final int getRowCount() {
        return board.length;
    }

    public final int getColumnCount(int row) {
        if (row < 0 || row >= board.length) {
            return 0;
        }
        return board[row].length;
    }

    public final int countPegs() {
        int count = 0;
        for (char[] row : board) {
            for (char cell : row) {
                if (cell == 'x') {
                    count++;
                }
            }
        }
        return count;
    }

    public final void startGame(boolean manualMode) {
        manual = manualMode;
        selectedPeg = null;
        gameOver = false;
        gameStarted = true;
        checkGameOver();
    }

    public final boolean isStarted() {
        return gameStarted;
    }

    public final boolean isManual() {
        return manual;
    }

    public final boolean isGameOver() {
        return gameOver;
    }

    /** Selects a peg for a manual move. */
    public final boolean selectPeg(int row, int col) {
        if (!gameStarted || !manual || !isInBounds(row, col) || board[row][col] != 'x') {
            selectedPeg = null;
            return false;
        }
        selectedPeg = new Peg(row, col);
        return true;
    }

    public final void clearSelection() {
        selectedPeg = null;
    }

    /**
     * Moves the selected peg to the requested destination. Returns true only
     * when a legal move was applied. A successful final move ends the game.
     */
    public final boolean makeMoveTo(int destinationRow, int destinationCol) {
        if (!gameStarted || !manual || selectedPeg == null) {
            selectedPeg = null;
            return false;
        }

        Peg source = selectedPeg;
        selectedPeg = null;

        if (!isInBounds(destinationRow, destinationCol)
                || board[destinationRow][destinationCol] != 'e') {
            return false;
        }

        int rowDifference = destinationRow - source.row;
        int colDifference = destinationCol - source.col;
        boolean validDirection =
                (Math.abs(rowDifference) == 2 && colDifference == 0)
                || (Math.abs(colDifference) == 2 && rowDifference == 0)
                || (Math.abs(rowDifference) == 2 && Math.abs(colDifference) == 2);

        if (!validDirection) {
            return false;
        }

        int middleRow = (source.row + destinationRow) / 2;
        int middleCol = (source.col + destinationCol) / 2;
        if (board[middleRow][middleCol] != 'x') {
            return false;
        }

        applyMove(new Move(source.row, source.col, middleRow, middleCol,
                destinationRow, destinationCol));
        checkGameOver();
        return true;
    }

    /** Performs one randomly selected legal move in automatic mode. */
    public final boolean randomMove() {
        if (!gameStarted || manual) {
            return false;
        }

        List<Move> validMoves = findLegalMoves();
        if (validMoves.isEmpty()) {
            checkGameOver();
            return false;
        }

        Move move = validMoves.get(new Random().nextInt(validMoves.size()));
        applyMove(move);
        checkGameOver();
        return true;
    }

    /** Returns true only when this call transitions the game into game-over. */
    public final boolean checkGameOver() {
        if (gameStarted && !hasLegalMoves()) {
            gameStarted = false;
            gameOver = true;
            selectedPeg = null;
            return true;
        }
        return false;
    }

    public final boolean hasLegalMoves() {
        return !findLegalMoves().isEmpty();
    }

    /** Randomly redistributes the current pegs without changing their count. */
    public final void randomize() {
        int pegCount = countPegs();
        List<int[]> playablePositions = new ArrayList<>();

        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col] != '?') {
                    playablePositions.add(new int[] {row, col});
                }
            }
        }

        if (pegCount > playablePositions.size()) {
            throw new IllegalStateException("There are more pegs than playable board positions.");
        }

        Collections.shuffle(playablePositions);
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col] != '?') {
                    board[row][col] = 'e';
                }
            }
        }

        for (int index = 0; index < pegCount; index++) {
            int[] position = playablePositions.get(index);
            board[position[0]][position[1]] = 'x';
        }

        selectedPeg = null;
        checkGameOver();
    }

    private List<Move> findLegalMoves() {
        List<Move> moves = new ArrayList<>();

        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col] != 'x') {
                    continue;
                }

                for (int[] direction : DIRECTIONS) {
                    int middleRow = row + direction[0];
                    int middleCol = col + direction[1];
                    int destinationRow = row + 2 * direction[0];
                    int destinationCol = col + 2 * direction[1];

                    if (isInBounds(destinationRow, destinationCol)
                            && board[middleRow][middleCol] == 'x'
                            && board[destinationRow][destinationCol] == 'e') {
                        moves.add(new Move(row, col, middleRow, middleCol,
                                destinationRow, destinationCol));
                    }
                }
            }
        }
        return moves;
    }

    private void applyMove(Move move) {
        board[move.sourceRow][move.sourceCol] = 'e';
        board[move.middleRow][move.middleCol] = 'e';
        board[move.destinationRow][move.destinationCol] = 'x';
    }

    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < board.length
                && col >= 0 && col < board[row].length;
    }
}
