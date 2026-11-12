
package main;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public final class GameSaveManager {

    private static final Path SAVE_FILE =
            Paths.get("peg-solitaire-save.txt");

    private static final String FILE_HEADER = "PEG_SOLITAIRE_SAVE_V1";

    private GameSaveManager() {
        // Prevent instantiation.
    }

    public static void saveGame(BoardType type,
                                boolean manualMode,
                                GameLogic game) throws IOException {

        if (type == null || game == null || !game.isStarted()) {
            throw new IllegalArgumentException("Invalid game to save.");
        }

        StringBuilder contents = new StringBuilder();

        contents.append(FILE_HEADER).append("\n");
        contents.append(type.name()).append("\n");
        contents.append(game.getBoardSize()).append("\n");
        contents.append(manualMode).append("\n");

        char[][] currentBoard = game.getBoard();

        contents.append(currentBoard.length).append("\n");

        for (char[] row : currentBoard) {
            contents.append(new String(row)).append("\n");
        }

        Files.write(
                SAVE_FILE,
                contents.toString().getBytes(StandardCharsets.UTF_8)
        );
    }

    public static SavedGame loadGame() throws IOException {

        if (!Files.exists(SAVE_FILE) || Files.size(SAVE_FILE) == 0) {
            return null;
        }

        List<String> lines =
                Files.readAllLines(SAVE_FILE, StandardCharsets.UTF_8);

            BoardType type = BoardType.valueOf(lines.get(1));

            int size = Integer.parseInt(lines.get(2));

            boolean manualMode = Boolean.parseBoolean(lines.get(3));

            int rowCount = Integer.parseInt(lines.get(4));

            char[][] savedBoard = new char[rowCount][];

            for (int row = 0; row < rowCount; row++) {
                savedBoard[row] = lines.get(row + 5).toCharArray();
            }

            return new SavedGame(type, size, manualMode, savedBoard);

        
    }

    public static void clearSave() throws IOException {
        Files.write(SAVE_FILE, new byte[0]);
    }

    public static final class SavedGame {

        private final BoardType boardType;
        private final int boardSize;
        private final boolean manualMode;
        private final char[][] board;

        private SavedGame(BoardType boardType, int boardSize,
                          boolean manualMode, char[][] board) {

            this.boardType = boardType;
            this.boardSize = boardSize;
            this.manualMode = manualMode;
            this.board = board;
        }

        public BoardType getBoardType() {
            return boardType;
        }

        public int getBoardSize() {
            return boardSize;
        }

        public boolean isManualMode() {
            return manualMode;
        }

        public char[][] getBoard() {
            char[][] copy = new char[board.length][];

            for (int row = 0; row < board.length; row++) {
                copy[row] = board[row].clone();
            }

            return copy;
        }
    }
}
