package main;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class BoardTest {
    @Test
    void randomizePreservesPegCount() {
        GameLogic game = new EnglishGame(7);
        int initialPegCount = game.countPegs();

        game.randomize();

        assertEquals(initialPegCount, game.countPegs(),
                "Randomization should preserve the number of pegs.");
    }

    @Test
    void englishBoardSize9LoadsCorrectly() {
        GameLogic game = new EnglishGame(9);
        char[][] expectedBoard = {
            {'?', '?', '?', 'x', 'x', 'x', '?', '?', '?'},
            {'?', '?', '?', 'x', 'x', 'x', '?', '?', '?'},
            {'?', '?', '?', 'x', 'x', 'x', '?', '?', '?'},
            {'x', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 'x'},
            {'x', 'x', 'x', 'x', 'e', 'x', 'x', 'x', 'x'},
            {'x', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 'x'},
            {'?', '?', '?', 'x', 'x', 'x', '?', '?', '?'},
            {'?', '?', '?', 'x', 'x', 'x', '?', '?', '?'},
            {'?', '?', '?', 'x', 'x', 'x', '?', '?', '?'}
        };

        assertArrayEquals(expectedBoard, game.getBoard(),
                "English board size 9 should load correctly.");
    }

    @Test
    void eachSubclassInitializesItsRequestedSize() {
        assertEquals(5, new EnglishGame(5).getBoardSize());
        assertEquals(7, new DiamondGame(7).getBoardSize());
        assertEquals(9, new HexagonGame(9).getBoardSize());
    }
}
