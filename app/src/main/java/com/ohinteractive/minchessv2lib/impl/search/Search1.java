package com.ohinteractive.minchessv2lib.impl.search;

import com.ohinteractive.minchessv2lib.impl.Board;
import com.ohinteractive.minchessv2lib.impl.Eval;
import com.ohinteractive.minchessv2lib.impl.Gen;
import com.ohinteractive.minchessv2lib.util.Move;

public class Search1 {
    
    public static SearchResult search(SearchConfig config) {
        long bestMove = 0L;
        int bestScore = -INF;
        long[] board = config.rootPosition();
        long[] movesBuffer = new long[Gen.MAX_MOVELIST_SIZE];
        long[] moves = Gen.gen(config.rootPosition(), true, false, movesBuffer);
        int moveCount = moves.length;
        for(int i = 0; i < moveCount; i ++) {
            long move = moves[i];
            System.out.println("Move " + Move.string(move));
            long[] next = Board.makeMove(board, move);
            int score = -negamax(next, Board.player(next), config.maxDepth() - 1, -INF, INF, movesBuffer);
            System.out.print(" " + score);
            if(score > bestScore) {
                bestScore = score;
                bestMove = move;
                System.out.print(" (Best)");
            }
            System.out.println();
        }
        return new SearchResult(bestMove, bestScore);
    }

    private static final int INF = 999999;

    private static int negamax(long[] board, int player, int depth, int alpha, int beta, long[] movesBuffer) {
        //System.out.println("Entered negamax");
    
        SearchStack stack = new SearchStack(depth + 1);
        int score = -INF;
        int ply = 0;
    
        SearchFrame frame = stack.push();
        frame.position = board;
        frame.player = player;
        frame.depth = depth;
        frame.alpha = alpha;
        frame.beta = beta;
        frame.moves = Gen.gen(board, true, false, movesBuffer);
        frame.moveIndex = 0;
        frame.ply = ply;
    
        //System.out.printf("Pushed root frame (%04x)%n", board[Board.KEY] & 0xffff);
    
        while (!stack.isEmpty()) {
            frame = stack.peek();
            //System.out.printf("Peeked top frame (%04x) at depth %d, ply %d%n", frame.position[Board.KEY] & 0xffff, frame.depth, frame.ply);
    
            if (frame.depth == 0 || frame.moves.length == 0) {
                long[] current = frame.position;
                int eval = Eval.eval(current[0], current[1], current[2], current[3], current[Board.STATUS], current[Board.KEY]) * (frame.player == 0 ? 1 : -1);
                //System.out.printf("Leaf node reached (%04x), evaluated score %d%n", board[Board.KEY] & 0xffff, eval);
                stack.pop();
                //System.out.printf("Popped leaf frame (%04x)%n", board[Board.KEY] & 0xffff);
    
                if (stack.isEmpty()) {
                    //System.out.println("Returning from root with final eval: " + eval);
                    return eval;
                }
    
                SearchFrame parent = stack.peek();
                int val = -eval;
                if (val > score) {
                    score = val;
                    parent.bestMove = frame.ply == 0 ? frame.bestMove : parent.moves[parent.moveIndex - 1];
                    //System.out.printf("Updated best score to %d for parent (%04x)%n", score, parent.position[Board.KEY] & 0xffff);
                }
                parent.alpha = Math.max(parent.alpha, val);
                //System.out.printf("Updated parent alpha to %d%n", parent.alpha);
    
                if (parent.alpha >= parent.beta) {
                    //System.out.println("Beta cutoff in parent frame");
                    stack.pop();
                }
                continue;
            }
    
            if (frame.moveIndex < frame.moves.length) {
                long move = frame.moves[frame.moveIndex++];
                long[] newPosition = Board.makeMove(frame.position, move);
    
                //System.out.printf("Made move %s from frame (%04x)%n", Move.string(move), frame.position[Board.KEY] & 0xffff);
                //System.out.println(Board.boardString(newPosition));
                SearchFrame child = stack.push();
                child.position = newPosition;
                child.player = 1 ^ frame.player;
                child.depth = frame.depth - 1;
                child.alpha = -frame.beta;
                child.beta = -frame.alpha;
                child.moves = Gen.gen(newPosition, true, false, movesBuffer);
                child.moveIndex = 0;
                child.ply = frame.ply + 1;
                child.bestMove = move;
    
                //System.out.printf("Pushed child frame (%04x) at depth %d%n", newPosition[Board.KEY] & 0xffff, child.depth);
            } else {
                int val = score;
                stack.pop();
                //System.out.printf("Popped frame (%04x) after exhausting moves%n", frame.position[Board.KEY] & 0xffff);
    
                if (stack.isEmpty()) {
                    //System.out.println("Returning from root with final eval: " + val);
                    return val;
                }
    
                SearchFrame parent = stack.peek();
                val = -val;
                if (val > score) {
                    score = val;
                    parent.bestMove = frame.ply == 0 ? frame.bestMove : parent.moves[parent.moveIndex - 1];
                    //System.out.printf("Updated best score to %d for parent (%04x)%n", score, parent.position[Board.KEY] & 0xffff);
                }
                parent.alpha = Math.max(parent.alpha, val);
                //System.out.printf("Updated parent alpha to %d%n", parent.alpha);
    
                if (parent.alpha >= parent.beta) {
                    //System.out.println("Beta cutoff in parent frame");
                    stack.pop();
                }
            }
        }
    
        //System.out.println("Exited negamax normally, final score: " + score);
        return score;
    }
    
    public static int quiesce(long[] board, int player, int alpha, int beta, long[] movesBuffer) {
        SearchStack stack = new SearchStack(32);

        SearchFrame frame = stack.push();
        frame.position = board;
        frame.player = player;
        frame.alpha = alpha;
        frame.beta = beta;
        frame.moves = Gen.gen(board, true, true, movesBuffer);
        frame.moveIndex = 0;
        frame.standPatEvaluated = false;

        while(!stack.isEmpty()) {
            frame = stack.peek();

            if(!frame.standPatEvaluated) {
                int standPat = Eval.eval(
                    frame.position[0], frame.position[1], frame.position[2], frame.position[3],
                    frame.position[Board.STATUS], frame.position[Board.KEY]
                ) * (frame.player == 0 ? 1 : -1);
                
                if (standPat >= frame.beta) {
                    stack.pop();
                    if (stack.isEmpty()) return frame.beta;
                    frame = stack.peek();
                    int val = -frame.beta;
                    frame.alpha = Math.max(frame.alpha, val);
                    if (frame.alpha >= frame.beta) stack.pop();
                    continue;
                }

                frame.alpha = Math.max(frame.alpha, standPat);
                frame.standPatEvaluated = true;
            }

            if (frame.moveIndex < frame.moves.length) {
                long move = frame.moves[frame.moveIndex ++];
                long[] next = Board.makeMove(frame.position, move);
    
                SearchFrame child = stack.push();
                child.position = next;
                child.player = 1 ^ frame.player;
                child.alpha = -frame.beta;
                child.beta = -frame.alpha;
                child.moves = Gen.gen(next, true, true, movesBuffer);
                child.moveIndex = 0;
                child.standPatEvaluated = false;
            } else {
                int val = frame.alpha;
                stack.pop();
                if (stack.isEmpty()) return val;
    
                SearchFrame parent = stack.peek();
                val = -val;
    
                parent.alpha = Math.max(parent.alpha, val);
                if (parent.alpha >= parent.beta) stack.pop(); // beta cutoff
            }

        }
        return alpha;
    }

}
