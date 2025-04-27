package com.ohinteractive.minchessv2lib;

import com.ohinteractive.minchessv2lib.impl.Board;
import com.ohinteractive.minchessv2lib.util.Perft;

public class App {
    
    public static void main(String[] args) {
        Perft.runAll();
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
