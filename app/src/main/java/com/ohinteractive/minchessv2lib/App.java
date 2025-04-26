package com.ohinteractive.minchessv2lib;

import com.ohinteractive.minchessv2lib.impl.Board;

public class App {
    
    public static void main(String[] args) {
        //String fen =
        //"k7/8/8/8/8/8/8/K7 w - - 0 1";
        long[] board = Board.startingPosition();
        Board.drawText(board);
    }

}
