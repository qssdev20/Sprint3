package main;

import javax.swing.JPanel;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

/** Swing view/controller for displaying a GameLogic instance. */
public final class Board extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final int CELL_SIZE = 40;
    private static final int PEG_SIZE = 10;
    
    //Variables used for saving/loading board state from game exited mid-play
    private Runnable gameOverHandler = () -> {};

    private transient GameLogic game;
    private int hoveredRow = -1;
    private int hoveredCol = -1;
    private boolean gameOverMessageShown;
    
    
    public void setGameOverHandler(Runnable handler) {
        this.gameOverHandler = Objects.requireNonNull(handler);
    }
    public Board(GameLogic game) {
        this.game = Objects.requireNonNull(game, "game cannot be null");
        setOpaque(true);
        setBackground(Color.WHITE);

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent event) {
                updateHoveredCell(event.getX(), event.getY());
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                handleBoardClick(event.getX(), event.getY());
            }
        });
    }

    public void setGame(GameLogic game) {
        this.game = Objects.requireNonNull(game, "game cannot be null");
        hoveredRow = -1;
        hoveredCol = -1;
        gameOverMessageShown = false;
        repaint();
    }

    public void resetGameOverMessage() {
        gameOverMessageShown = false;
    }

    private void updateHoveredCell(int x, int y) {
        if (game == null || !game.isStarted()) {
            hoveredRow = -1;
            hoveredCol = -1;
        } else {
            int row = y / CELL_SIZE;
            int col = x / CELL_SIZE;
            if (game.getCell(row, col) != '?') {
                hoveredRow = row;
                hoveredCol = col;
            } else {
                hoveredRow = -1;
                hoveredCol = -1;
            }
        }
        repaint();
    }

    private void handleBoardClick(int x, int y) {
        if (game == null || !game.isStarted() || !game.isManual()) {
            return;
        }

        int row = y / CELL_SIZE;
        int col = x / CELL_SIZE;
        char clickedCell = game.getCell(row, col);

        if (clickedCell == '?') {
            game.clearSelection();
            return;
        }

        if (clickedCell == 'x') {
            game.selectPeg(row, col);
        } else if (clickedCell == 'e') {
            game.makeMoveTo(row, col);
            showGameOverMessageIfNeeded();
        }

        repaint();
    }

    public void showGameOverMessageIfNeeded() {
        if (game != null && game.isGameOver() && !gameOverMessageShown) {

            gameOverMessageShown = true;

            gameOverHandler.run();

            JOptionPane.showMessageDialog(
                    this,
                    "Game over! No legal moves remain.",
                    "Game Over",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void drawSquare(Graphics graphics, int x, int y) {
        graphics.drawRect(x, y, CELL_SIZE, CELL_SIZE);
    }

    private void drawPeg(Graphics graphics, int x, int y) {
        graphics.fillOval(x, y, PEG_SIZE, PEG_SIZE);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (game == null) {
            return;
        }

        char[][] cells = game.getBoard();
        for (int row = 0; row < cells.length; row++) {
            for (int col = 0; col < cells[row].length; col++) {
                if (cells[row][col] == '?') {
                    continue;
                }

                int x = CELL_SIZE * col;
                int y = CELL_SIZE * row;

                if (row == hoveredRow && col == hoveredCol && game.isStarted()) {
                    graphics.setColor(Color.GREEN);
                    graphics.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                    graphics.setColor(Color.BLACK);
                }

                drawSquare(graphics, x, y);
                if (cells[row][col] == 'x') {
                    drawPeg(graphics, x + 15, y + 15);
                }
            }
        }
    }
}
