package com.ohinteractive.minchessv2lib;

import com.ohinteractive.minchessv2lib.impl.Board;
import com.ohinteractive.minchessv2lib.impl.Gen;
import com.ohinteractive.minchessv2lib.impl.search.Search;
import com.ohinteractive.minchessv2lib.impl.search.SearchResult;
import com.ohinteractive.minchessv2lib.test.TestEval;
import com.ohinteractive.minchessv2lib.util.Move;
import com.ohinteractive.minchessv2lib.util.Perft;

public class App {
    
    public static void main(String[] args) {
        perft();
    }

    public static void perft() {
        Perft.runAll();
    }

    public static void testSearch() {
        long[] board = Board.fromFen("8/8/8/8/4k3/3p4/4P3/4K3 w - - 0 1");
        board = Board.startingPosition();
        System.out.println(Board.boardString(board));
        SearchResult result = Search.bestMove(
            board,
            4,
            5000
        );
        System.out.println("Best Move: " + Move.string(result.bestMove()));
        System.out.println("Score: " + result.score());
    }

    public static void testEval() {
        TestEval.test();
    }

    public static void testGen() {
        long time = System.nanoTime();
        long[] testBoard = Board.startingPosition();
        long[] movesBuffer = new long[100];
        for(int i = 0; i < 1000000; i ++) {
            long[] moves = Gen.gen(testBoard, false, false, movesBuffer);
            long[] newBoard = Board.makeMove(testBoard, moves[0]);
            newBoard[5] = 0L;
        }
        time = System.nanoTime() - time;
        System.out.println("Elapsed: " + time);
    }

    public static void draw(long[] board) {
        System.out.println(Board.boardString(board) + "\n");
        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

}
