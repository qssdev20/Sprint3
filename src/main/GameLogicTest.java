package main;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class GameLogicTest {
    @Test
    void lastMoveEndsManualGame() {
        GameLogic game = new EnglishGame(5);
        game.setBoard(new char[][] {{'x', 'x', 'e'}});
        game.startGame(true);

        assertTrue(game.selectPeg(0, 0));
        assertTrue(game.makeMoveTo(0, 2));

        assertFalse(game.isStarted(), "The last move should have ended the game.");
        assertTrue(game.isGameOver());
    }

    @Test
    void lastMoveEndsAutomatedGame() {
        GameLogic game = new EnglishGame(5);
        game.setBoard(new char[][] {{'x', 'x', 'e'}});
        game.startGame(false);

        assertTrue(game.randomMove());

        assertFalse(game.isStarted(), "The last move should have ended the game.");
        assertTrue(game.isGameOver());
    }

    @Test
    void automaticMoveDecreasesPegCount() {
        GameLogic game = new EnglishGame(7);
        game.startGame(false);
        int pegsBeforeMove = game.countPegs();

        assertTrue(game.randomMove(), "A legal automatic move should be available.");

        assertEquals(pegsBeforeMove - 1, game.countPegs(),
                "A successful move should decrease the peg count by one.");
    }

    @Test
    void validMoveMovesPegAndRemovesJumpedPeg() {
        GameLogic game = new EnglishGame(5);
        game.setBoard(new char[][] {
            {'x', 'x', 'e', '?', '?'},
            {'?', 'e', 'e', 'e', '?'},
            {'x', 'x', 'e', 'e', 'e'},
            {'?', 'e', 'e', 'e', '?'},
            {'?', '?', 'e', '?', '?'}
        });
        game.startGame(true);

        assertTrue(game.selectPeg(2, 0));
        assertTrue(game.makeMoveTo(2, 2));

        char[][] result = game.getBoard();
        assertEquals('e', result[2][0], "The original position should be empty.");
        assertEquals('e', result[2][1], "The jumped peg should be removed.");
        assertEquals('x', result[2][2], "The destination should contain the moved peg.");
    }

    @Test
    void noLegalMovesReturnsFalse() {
        GameLogic game = new EnglishGame(7);
        game.setBoard(new char[][] {
            {'?', '?', '?', 'e', '?', '?', '?'},
            {'?', '?', 'e', 'e', 'x', '?', '?'},
            {'?', 'e', 'e', 'e', 'e', 'e', '?'},
            {'e', 'e', 'e', 'x', 'e', 'x', 'e'},
            {'?', 'e', 'e', 'e', 'e', 'e', '?'},
            {'?', '?', 'e', 'e', 'e', '?', '?'},
            {'?', '?', 'x', 'e', '?', '?', '?'}
        });

        assertFalse(game.hasLegalMoves(),
                "The game should have no legal moves remaining.");
    }

    @Test
    void startGameSetsCorrectMode() {
        GameLogic game = new EnglishGame(7);

        game.startGame(true);
        assertTrue(game.isManual(), "Game should start in manual mode.");

        game.startGame(false);
        assertFalse(game.isManual(), "Game should start in automatic mode.");
    }
}
