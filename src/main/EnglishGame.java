package main;

/** Peg Solitaire using the English cross-shaped board. */
public final class EnglishGame extends GameLogic {
    private static final char[][] BOARD_5 = {
        {'?', '?', 'x', '?', '?'},
        {'?', '?', 'x', '?', '?'},
        {'x', 'x', 'e', 'x', 'x'},
        {'?', '?', 'x', '?', '?'},
        {'?', '?', 'x', '?', '?'}
    };

    private static final char[][] BOARD_7 = {
        {'?', '?', 'x', 'x', 'x', '?', '?'},
        {'?', '?', 'x', 'x', 'x', '?', '?'},
        {'x', 'x', 'x', 'x', 'x', 'x', 'x'},
        {'x', 'x', 'x', 'e', 'x', 'x', 'x'},
        {'x', 'x', 'x', 'x', 'x', 'x', 'x'},
        {'?', '?', 'x', 'x', 'x', '?', '?'},
        {'?', '?', 'x', 'x', 'x', '?', '?'}
    };

    private static final char[][] BOARD_9 = {
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

    public EnglishGame() {
        this(7);
    }

    public EnglishGame(int size) {
        setBoard(layoutForSize(size));
    }

    @Override
    public void initializeBoard(int size) {
        setBoard(layoutForSize(size));
    }

    private static char[][] layoutForSize(int size) {
        switch (size) {
            case 5:
                return BOARD_5;
            case 7:
                return BOARD_7;
            case 9:
                return BOARD_9;
            default:
                throw new IllegalArgumentException("English board size must be 5, 7, or 9.");
        }
    }
}
