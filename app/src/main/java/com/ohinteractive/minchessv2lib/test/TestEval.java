package com.ohinteractive.minchessv2lib.test;

import com.ohinteractive.minchessv2lib.impl.Board;
import com.ohinteractive.minchessv2lib.impl.Eval;

public class TestEval {
    
    public static void test() {
        String[] testStrings = TestPositions.ALL;
        for(int i = 0; i < testStrings.length; i ++) {
            String testString = testStrings[i];
            long[] board = Board.fromFen(testString);
            System.out.println((i + 1) + ". \"" + testString + "\"");
            System.out.println(Board.boardString(board));
            int eval = Eval.eval(board[0], board[1], board[2], board[3], board[Board.STATUS], board[Board.KEY]);
            System.out.println("Eval = " + eval + "\n");
        }
    }

    private TestEval() {}

}
