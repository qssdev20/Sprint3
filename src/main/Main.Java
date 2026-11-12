package main;

import javax.swing.*;
import java.io.IOException;
import java.awt.event.*;
import javax.swing.event.*;

import java.awt.Color;
import java.awt.Font;

public class Main {
	//interval used for randomizing the board setup during gameplay.
    private static final int RANDOMIZE_INTERVAL_MS = 10_000;
    //interval used for a move to be made in the automated gamemode after 500 ms
    private static final int AUTOMATIC_MOVE_INTERVAL_MS = 500;

    //timer objects for automated gamemode and randomize board option selected.
    private static Timer autoTimer;
    private static Timer randomizeTimer;
    
    private static BoardType boardType = BoardType.ENGLISH;
    private static boolean manualMode = true;
    private static GameLogic game;
    private static Board board;
    
    //Called when starting a new game. The method instantiates and returns an object
    //of the GameLogic subclass corresponding to the specified BoardType and board 
    //size.    
    private static GameLogic createGame(BoardType type, int size) {
        switch (type) {
            case ENGLISH:
                return new EnglishGame(size);
            case DIAMOND:
                return new DiamondGame(size);
            case HEXAGON:
                return new HexagonGame(size);
            default:
                throw new IllegalArgumentException("Unsupported board type: " + type);
        }
    }

    //Stop timers for automated moves and board randomization if they exist.
    private static void stopTimers() {
        if (autoTimer != null) {
            autoTimer.stop();
            autoTimer = null;
        }
        if (randomizeTimer != null) {
            randomizeTimer.stop();
            randomizeTimer = null;
        }
    }
    //This method lauches the game with randomization activated or not.
    //This method also takes in the different visual components from the start menu
    //in order to make them invisible during gameplay.
    private static void launchGame(boolean randomizeDuringGame, JComponent... setupControls) {
        stopTimers();
        game.startGame(manualMode);
        board.resetGameOverMessage();

        //If randomizeDuringGame = true, then start a timer to randomize the board
        //every RANDOMIZE_INTERVAL_MS.
        if (randomizeDuringGame) {
            randomizeTimer = new Timer(RANDOMIZE_INTERVAL_MS, event -> {
                if (!game.isStarted()) {
                    stopTimers();
                    return;
                }
                game.randomize();
                board.repaint();
                //If the game has ended, then stop timer for board randomization.
                if (game.isGameOver()) {
                    stopTimers();
                    board.showGameOverMessageIfNeeded();
                }
            });
            randomizeTimer.start();
        }
        //If user had selected "Start" or "Random" in automatic mode, then set the timer
        //To invoke automated moves every AUTOMATIC_MOVE_INTERVAL_MS.
        if (!manualMode) {
            autoTimer = new Timer(AUTOMATIC_MOVE_INTERVAL_MS, event -> {
            	//If game ended, then stop automated moves.
                if (!game.isStarted()) {
                    stopTimers();
                    return;
                }

                game.randomMove();
                board.repaint();
                if (game.isGameOver()) {
                    stopTimers();
                    board.showGameOverMessageIfNeeded();
                }
            });
            autoTimer.start();
        }
        //Make main menu UI invisible
        for (JComponent control : setupControls) {
            control.setVisible(false);
        }
        board.repaint();
    }
//See if a saved board state is present in peg-solitaire-save.txt.
//If so, then load the saved board; else generate the 7x7 English board as a default.
private static GameLogic tryAndLoadGame() {

    try {
        GameSaveManager.SavedGame savedGame =
                GameSaveManager.loadGame();

        // No saved game: use default settings.
        if (savedGame == null) {
            boardType = BoardType.ENGLISH;
            manualMode = true;
            return createGame(boardType, 7);
        }

        //if there is a saved board state, then Restore the saved settings.
        boardType = savedGame.getBoardType();
        manualMode = savedGame.isManualMode();

        // Create the appropriate GameLogic subclass.
        GameLogic loadedGame =
                createGame(boardType, savedGame.getBoardSize());

        // Restore the saved peg positions.
        loadedGame.restoreBoardState(savedGame.getBoard());

        return loadedGame;

    } catch (IOException | IllegalArgumentException exception) {
    	//If the file doesn't exist, this error will be thrown.
        System.err.println("Unable to load saved game: "
                + exception.getMessage());

        // Discard an invalid save file.
        try {
            GameSaveManager.clearSave();
        } catch (IOException clearException) {
            clearException.printStackTrace();
        }

        // Fall back to default settings.
        boardType = BoardType.ENGLISH;
        manualMode = true;

        return createGame(boardType, 7);
    }
}

    
public static void main(String[] args) {

    // Load the previous game, or create a default game.
    game = tryAndLoadGame();

    int initialSize = game.getBoardSize();

    JFrame window = new JFrame("Peg Solitaire");
    window.setSize(800, 600);
    window.setLayout(null);
    window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    board = new Board(game);
    board.setBounds(200, 90, 390, 390);
    window.add(board);
    board.setGameOverHandler(() -> {
        try {
            GameSaveManager.clearSave();
        } catch (IOException exception) {
            exception.printStackTrace();

            JOptionPane.showMessageDialog(
                    window,
                    "Unable to clear the save file.",
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    });
    // Clear the save file when the game ends.
    board.setGameOverHandler(() -> {
        try {
            GameSaveManager.clearSave();
        } catch (IOException exception) {
            exception.printStackTrace();

            JOptionPane.showMessageDialog(
                    window,
                    "Unable to clear the save file.",
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    });

    // Board type selection.
    JLabel boardTypeLabel = new JLabel("Board Type");
    boardTypeLabel.setFont(new Font("Arial", Font.BOLD, 18));
    boardTypeLabel.setBounds(20, 20, 200, 25);
    window.add(boardTypeLabel);

    JRadioButton english = new JRadioButton(
            "English", boardType == BoardType.ENGLISH);

    JRadioButton hexagon = new JRadioButton(
            "Hexagon", boardType == BoardType.HEXAGON);

    JRadioButton diamond = new JRadioButton(
            "Diamond", boardType == BoardType.DIAMOND);

    ButtonGroup boardTypeGroup = new ButtonGroup();
    boardTypeGroup.add(english);
    boardTypeGroup.add(hexagon);
    boardTypeGroup.add(diamond);

    english.setBounds(10, 70, 125, 26);
    hexagon.setBounds(10, 110, 125, 26);
    diamond.setBounds(10, 150, 125, 26);

    Font optionFont = new Font("Arial", Font.PLAIN, 18);

    english.setFont(optionFont);
    hexagon.setFont(optionFont);
    diamond.setFont(optionFont);

    window.add(english);
    window.add(hexagon);
    window.add(diamond);

    // Board size selection.
    JLabel boardSizeLabel = new JLabel("Board Size:");
    boardSizeLabel.setBounds(600, 20, 80, 25);
    window.add(boardSizeLabel);

    JTextField boardSizeField =
            new JTextField(String.valueOf(initialSize));

    boardSizeField.setBounds(680, 20, 80, 25);
    window.add(boardSizeField);

    JLabel errorLabel = new JLabel("");
    errorLabel.setBounds(560, 55, 220, 25);
    errorLabel.setForeground(Color.RED);
    window.add(errorLabel);
    //Create a Runnable to be executed by thread
    Runnable refreshSelectedGame = () -> {
        try {
            int size = Integer.parseInt(
                    boardSizeField.getText().trim());

            if (size != 5 && size != 7 && size != 9) {
                errorLabel.setText(
                        "Board size must be 5, 7, or 9.");
                return;
            }

            GameLogic replacement =
                    createGame(boardType, size);

            game = replacement;
            board.setGame(game);
            errorLabel.setText("");

        } catch (NumberFormatException exception) {
            errorLabel.setText("Enter an integer.");
        }
    };

    boardSizeField.getDocument().addDocumentListener(
            new DocumentListener() {
        //This method is invoked when text is removed/inserted/updated
        private void validateAndRefresh() {
            refreshSelectedGame.run();
        }

        @Override
        public void insertUpdate(DocumentEvent event) {
            validateAndRefresh();
        }

        @Override
        public void removeUpdate(DocumentEvent event) {
            validateAndRefresh();
        }

        @Override
        public void changedUpdate(DocumentEvent event) {
            validateAndRefresh();
        }
    });

    english.addActionListener(event -> {
        boardType = BoardType.ENGLISH;
        refreshSelectedGame.run();
    });

    hexagon.addActionListener(event -> {
        boardType = BoardType.HEXAGON;
        refreshSelectedGame.run();
    });

    diamond.addActionListener(event -> {
        boardType = BoardType.DIAMOND;
        refreshSelectedGame.run();
    });

    // Game mode selection.
    JLabel gameModeLabel = new JLabel("Game Mode");
    gameModeLabel.setFont(new Font("Arial", Font.BOLD, 18));
    gameModeLabel.setBounds(630, 250, 125, 26);
    window.add(gameModeLabel);

    JRadioButton manual =
            new JRadioButton("Manual", manualMode);

    JRadioButton automatic =
            new JRadioButton("Automatic", !manualMode);

    ButtonGroup modeGroup = new ButtonGroup();
    modeGroup.add(manual);
    modeGroup.add(automatic);

    manual.setBounds(630, 295, 125, 26);
    automatic.setBounds(630, 335, 125, 26);

    manual.setFont(optionFont);
    automatic.setFont(optionFont);

    window.add(manual);
    window.add(automatic);

    manual.addActionListener(event -> manualMode = true);
    automatic.addActionListener(event -> manualMode = false);

    // Game controls.
    JButton randomizeButton = new JButton("Randomize");
    randomizeButton.setBounds(600, 460, 120, 35);
    window.add(randomizeButton);

    JButton startButton = new JButton("Start Game");
    startButton.setBounds(600, 500, 120, 35);
    window.add(startButton);
    //NOTE: the values english/hexagon/diamond are the buttons used to select the 
    //board type. they are NOT the actual boards to display
    JComponent[] setupControls = {
        english, hexagon, diamond, boardTypeLabel,
        boardSizeLabel, boardSizeField, errorLabel,
        gameModeLabel, manual, automatic,
        randomizeButton, startButton
    };

    startButton.addActionListener(
            event -> launchGame(false, setupControls));

    randomizeButton.addActionListener(
            event -> launchGame(true, setupControls));

    // Handle application exit. If a game is in session; save current board state into file
    //If the game was completed before exiting game;don't save file.
    window.addWindowListener(new WindowAdapter() {

    @Override
    public void windowClosing(WindowEvent event) {

        stopTimers();

        // Only save an active game.
        if (game != null && game.isStarted()) {

            try {
                GameSaveManager.saveGame(
                        boardType,
                        manualMode,
                        game
                );

            } catch (IOException exception) {

                JOptionPane.showMessageDialog(
                        window,
                        "Unable to save the game: "
                                + exception.getMessage(),
                        "Save Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }

        window.dispose();
    }
});
    window.setLocationRelativeTo(null);
    window.setVisible(true);
}
}
