package com.ohinteractive.minchessv2lib.impl.search;

public class SearchFrame {
    public int ply;
    public int depth;
    public int alpha;
    public int beta;
    public long[] position;
    public int player;
    public long[] moves;
    public int moveListLength;
    public int moveIndex;
    public long bestMove;
    public long moveFromParent;
    public boolean standPatEvaluated;
}
