package com.ohinteractive.minchessv2lib.impl.search;

import com.ohinteractive.minchessv2lib.impl.Board;
import com.ohinteractive.minchessv2lib.impl.Eval;
import com.ohinteractive.minchessv2lib.impl.Gen;

public class Search {
    
    public static SearchResult search(SearchConfig config) {
        SearchStack stack = new SearchStack(config.maxDepth());
        long bestMove = 0L;
        int bestScore = -INF;
        long[] board = config.rootPosition();
        long[] moves = Gen.gen(config.rootPosition(), true, false);
        for(long move : moves) {
            long[] next = Board.makeMove(board, move);
            int score = -negamax(next, Board.player(next), config.maxDepth() - 1, -INF, INF, stack);
            if(score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        return new SearchResult(bestMove, bestScore);
    }

    private static final int INF = 999999;

    private static int negamax(long[] board, int player, int depth, int alpha, int beta, SearchStack stack) {
        if(depth == 0) return Eval.eval(board[0], board[1], board[2], board[3], board[4], board[5]);
        SearchFrame frame = stack.push();
        frame.position = board;
        frame.player = player;
        frame.depth = depth;
        frame.alpha = alpha;
        frame.beta = beta;
        frame.moves = Gen.gen(board, true, false);
        frame.moveIndex = 0;
        frame.bestMove = 0L;
        int score = -INF;
        while(frame.moveIndex < frame.moves.length) {
            long move = frame.moves[frame.moveIndex ++];
            long[] next = Board.makeMove(board, move);
            int value = -negamax(next, 1 ^ player, depth - 1, -beta, -alpha, stack);
            if(value > score) {
                score = value;
                frame.bestMove = move;
            }
            alpha = Math.max(alpha, value);
            if(alpha >= beta) {
                break;
            }
        }
        stack.pop();
        return score;
    }

}
