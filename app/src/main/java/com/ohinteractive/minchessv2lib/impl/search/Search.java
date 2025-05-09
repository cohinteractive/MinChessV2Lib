package com.ohinteractive.minchessv2lib.impl.search;

import com.ohinteractive.minchessv2lib.impl.Board;
import com.ohinteractive.minchessv2lib.impl.Eval;
import com.ohinteractive.minchessv2lib.impl.Gen;
import com.ohinteractive.minchessv2lib.util.Move;

public class Search {
    
    public static SearchResult bestMove(long[] rootPosition, int maxDepth, long maxTimeMillis) {
        int player = (int) rootPosition[Board.STATUS] & Board.PLAYER_BIT;
        long[] newPosition = new long[Board.MAX_BITBOARDS];
        long[] movesBuffer = new long[Gen.MAX_MOVELIST_SIZE];
        long[] moves = Gen.gen(rootPosition, false, false, movesBuffer);
        int moveCount = moves.length;
        int bestScore = -INF;
        long bestMove = 0L;
        for(int i = 0; i < moveCount; i ++) {
            long move = moves[i];
            System.arraycopy(rootPosition, 0, newPosition, 0, Board.MAX_BITBOARDS);
            Board.makeMoveWith(newPosition, move);
            if(Board.isPlayerInCheck(newPosition, player)) continue;
            System.out.print("Move " + Move.string(move) + ": ");
            int score = -searchFlattened(newPosition, maxDepth - 1, movesBuffer);
            if(score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            System.out.println(score);
        }
        return new SearchResult(bestMove, bestScore);
    }

    private static final int INF = 999999;

    private Search() {}

    private static int searchFlattened(long[] position, int depth, long[] movesBuffer) {
        long[] newPosition = new long[Board.MAX_BITBOARDS];
        SearchStack stack = new SearchStack(depth + 1);
        int score = -INF;
        int ply = 0;
        SearchFrame parent;
        SearchFrame child;
        SearchFrame frame = stack.push();
        frame.position = position;
        frame.player = (int) position[Board.STATUS] & Board.PLAYER_BIT;
        frame.depth = depth;
        frame.ply = ply;
        frame.alpha = -INF;
        frame.bestMove = 0L;
        frame.moveFromParent = 0L;
        frame.moves = Gen.gen(position, false, false, movesBuffer);
        frame.moveListLength = frame.moves.length;
        frame.moveIndex = 0;
        while(!stack.isEmpty()) {
            frame = stack.peek();
            int player = frame.player;
            if(frame.depth == 0 || frame.moveListLength == 0) {
                int eval = quiesceFlattened(frame.position, movesBuffer);
                stack.pop();
                if(stack.isEmpty()) {
                    return eval;
                }
                parent = stack.peek();
                eval = parent.player == player ? eval : -eval;
                if(eval > parent.alpha) {
                    parent.alpha = eval;
                    parent.bestMove = frame.moveFromParent;
                }
                continue;
            }
            if(frame.moveIndex < frame.moveListLength) {
                long move = frame.moves[frame.moveIndex ++];
                System.arraycopy(frame.position, 0, newPosition, 0, Board.MAX_BITBOARDS);
                Board.makeMoveWith(newPosition, move);
                if(Board.isPlayerInCheck(newPosition, player)) continue;
                child = stack.push();
                child.position = new long[Board.MAX_BITBOARDS];
                System.arraycopy(newPosition, 0, child.position, 0, Board.MAX_BITBOARDS);
                child.player = (int) newPosition[Board.STATUS] & Board.PLAYER_BIT;
                child.depth = frame.depth - 1;
                child.ply = frame.ply + 1;
                child.alpha = -INF;
                child.bestMove = 0L;
                child.moveFromParent = move;
                child.moves = Gen.gen(newPosition, false, false, movesBuffer);
                child.moveListLength = child.moves.length;
                child.moveIndex = 0;
                continue;
            }
            stack.pop();
            if(stack.isEmpty()) {
                return frame.alpha;
            }
            parent = stack.peek();
            int effectiveBestScore = parent.player == player ? frame.alpha : -frame.alpha;
            if(effectiveBestScore > parent.alpha) {
                parent.alpha = effectiveBestScore;
                parent.bestMove = frame.moveFromParent;
            }
        }
        return score;
    }

    private static int quiesceFlattened(long[] position, long[] movesBuffer) {
        long[] newPosition = new long[Board.MAX_BITBOARDS];
        SearchStack stack = new SearchStack(32);
        SearchFrame parent;
        SearchFrame child;
        SearchFrame frame = stack.push();
        frame.position = position;
        frame.player = (int) position[Board.STATUS] & Board.PLAYER_BIT;
        frame.ply = 0;
        frame.alpha = Eval.eval(position[0], position[1], position[2], position[3], position[Board.STATUS], position[Board.KEY]); // alpha = score
        frame.moves = Gen.gen(position, false, true, movesBuffer);
        frame.moveListLength = frame.moves.length;
        frame.moveIndex = 0;
        frame.beta = frame.alpha; // beta is best score
        frame.moveFromParent = 0L;
        while(!stack.isEmpty()) {
            frame = stack.peek();
            int player = frame.player;
            if(frame.moveIndex >= frame.moveListLength) {
                stack.pop();
                if(stack.isEmpty()) return frame.beta;
                parent = stack.peek();
                int effectiveBestScore = parent.player == player ? frame.beta : -frame.beta;
                if(effectiveBestScore > parent.beta) {
                    parent.beta = effectiveBestScore;
                    parent.moveFromParent = frame.moveFromParent;
                }
                continue;
            }
            long move = frame.moves[frame.moveIndex ++];
            System.arraycopy(frame.position, 0, newPosition, 0, Board.MAX_BITBOARDS);
            Board.makeMoveWith(newPosition, move);
            if(Board.isPlayerInCheck(newPosition, player)) continue;
            int eval = Eval.eval(newPosition[0], newPosition[1], newPosition[2], newPosition[3], newPosition[Board.STATUS], newPosition[Board.KEY]);
            child = stack.push();
            child.position = new long[Board.MAX_BITBOARDS];
            System.arraycopy(newPosition, 0, child.position, 0, Board.MAX_BITBOARDS);
            child.player = (int) newPosition[Board.STATUS] & Board.PLAYER_BIT;
            child.alpha = eval;
            child.moves = Gen.gen(newPosition, false, true, movesBuffer);
            child.moveListLength = child.moves.length;
            child.moveIndex = 0;
            child.beta = eval;
            child.ply = frame.ply + 1;
            child.moveFromParent = move;
            continue;
        }
        return 0;
    }

}
