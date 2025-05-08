package com.ohinteractive.minchessv2lib.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import com.ohinteractive.minchessv2lib.impl.Board;
import com.ohinteractive.minchessv2lib.impl.Gen;

public class Perft {

    public static void runAll() {
        runRange(0, POSITION_NAMES.length - 1);
    }

    public static void runRange(int start, int end) {
        long totalElapsed = 0;
        for(int i = start; i <= end; i ++) {
            System.out.println(POSITION_NAMES[i]);
            long elapsed = runSinglePosition(POSITION_FENS[i], DEPTHS[i], EXPECTED_NODES[i]);
            totalElapsed += elapsed;
            if(WAIT_BETWEEN_POSITIONS && i < end) {
                sleep(WAIT_TIME_MS);
            }
        }
        System.out.println("All positions complete.");
        System.out.printf("Total elapsed: %,d ms%n", totalElapsed);
    }

    public static void runFen(String fen, int depth) {
        long elapsed = runSinglePosition(fen, depth, 0);
        System.out.printf("Total elapsed: %,d ms%n", elapsed);
    }

    public static long runSinglePosition(String fen, int depth, long expectedNodes) {
        System.out.println("\"" + fen + "\"");
        long[] board = Board.fromFen(fen);
        System.out.println(Board.boardString(board));
        long startTime = System.nanoTime();
        long nodes = perft(board, depth, PARALLEL, !RECURSIVE);
        long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;
        if(nodes == expectedNodes) {
            System.out.println("Result: PASSED\n");
        } else {
            long diff = expectedNodes - nodes;
            System.out.printf("Result: FAILED (expected %,d, got %,d, diff %,d)m%n", expectedNodes, nodes, diff);
        }
        return elapsedMs;
    }

    public static void runDebug(String fen, int depth) {
        long[] board = Board.fromFen(fen);
        int player = (int) board[Board.STATUS] & 1;
        long nodes = searchDebug(board, player, depth, depth, new ArrayList<>());
        System.out.printf("Debug perft finished: %,d nodes%n", nodes);
    }
    
    private static final class Frame {
        long[] board;
        int player;
        int depth;
        int moveIndex;
        long[] moves;
        int moveCount;
    }

    private static final class FrameStack {
        final Frame[] frames;
        int top;
        FrameStack(int capacity) {
            frames = new Frame[capacity];
            for(int i = 0; i < capacity; i ++) frames[i] = new Frame();
            top = 0;
        }

        Frame push(long[] board, int player, int depth) {
            Frame f = frames[top ++];
            f.board = board;
            f.player = player;
            f.depth = depth;
            f.moves = Gen.gen(board, false, false);
            f.moveCount = (int) f.moves[Gen.MOVELIST_SIZE];
            f.moveIndex = 0;
            return f;
        }

        Frame peek() { return frames[top - 1]; }

        void pop() { top --; }

        boolean isEmpty() { return top == 0; }
    }

    private static final String WATCH_MOVE = "g2h1B";
    private static final int WATCH_DEPTH = 2;
    private static final boolean PRINT_MOVE_PATH = true;
    
    private static final boolean WAIT_BETWEEN_POSITIONS = true;
    private static final int WAIT_TIME_MS = 4000;
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private static final boolean PARALLEL = true;
    private static final boolean RECURSIVE = true;

    private static final String[] POSITION_NAMES = {
        "1. Initial position",
        "2.", "3.", "4.", "5.", "6.", "7.",
        "8. En passant capture gives check",
        "9. Short castling gives check",
        "10. Long castling gives check",
        "11. Castling", "12. Castling prevented",
        "13. Promote out of check", "14. Discovered check",
        "15. Promotion gives check", "16. Underpromotion gives check",
        "17. Self stalemate", "18. Stalemate/Checkmate",
        "19. Double check"
    };
    private static final String[] POSITION_FENS = {
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
        "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
        "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1",
        "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1",
        "rnbqkb1r/pp1p1ppp/2p5/4P3/2B5/8/PPP1NnPP/RNBQK2R w KQkq - 0 6",
        "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10",
        "8/5bk1/8/2Pp4/8/1K6/8/8 w - d6 0 1",
        "8/8/1k6/2b5/2pP4/8/5K2/8 b - d3 0 1",
        "5k2/8/8/8/8/8/8/4K2R w K - 0 1",
        "3k4/8/8/8/8/8/8/R3K3 w Q - 0 1",
        "r3k2r/1b4bq/8/8/8/8/7B/R3K2R w KQkq - 0 1",
        "r3k2r/8/3Q4/8/8/5q2/8/R3K2R b KQkq - 0 1",
        "2K2r2/4P3/8/8/8/8/8/3k4 w - - 0 1",
        "8/8/1P2K3/8/2n5/1q6/8/5k2 b - - 0 1",
        "4k3/1P6/8/8/8/8/K7/8 w - - 0 1",
        "8/P1k5/K7/8/8/8/8/8 w - - 0 1",
        "K1k5/8/P7/8/8/8/8/8 w - - 0 1",
        "8/k1P5/8/1K6/8/8/8/8 w - - 0 1",
        "8/8/2k5/5q2/5n2/8/5K2/8 b - - 0 1"
    };
    private static final long[] EXPECTED_NODES = {
        119060324, 193690690, 178633661, 706045033, 53392, 6923051137L, 824064,
        1440467, 661072, 803711, 1274206, 1720476, 3821001, 1004658, 217342,
        92683, 2217, 567584, 23527
    };
    private static final int[] DEPTHS = {
        6, 5, 7, 6, 3, 6, 6, 6, 6, 6, 4, 4, 6, 5, 6, 6, 6, 7, 4
    };

    private Perft() {}

    private static long perft(long[] board, int maxDepth, boolean parallel, boolean recursive) {
        long totalNodes = 0;
        int firstMoveCount = 0;
        int player = (int) board[Board.STATUS] & 0x1;
        for(int depth = 1; depth <= maxDepth; depth ++) {
            long start = System.nanoTime();
            long nodes;
            if(depth == maxDepth && parallel) {
                try {
                    nodes = searchParallel(board, player, depth, recursive);
                } catch(InterruptedException | ExecutionException e) {
                    throw new RuntimeException("Parallel perft failed", e);
                }
            } else {
                if(recursive) {
                    nodes = search(board, player, depth, maxDepth, firstMoveCount);
                } else {
                    nodes = searchFlattened(board, player, depth, maxDepth, firstMoveCount);
                }
            }
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            System.out.printf("Depth %d/%d: %,d nodes in %d ms%n", depth, maxDepth, nodes, elapsedMs);
            if(depth == 1) firstMoveCount = (int) nodes;
            if(depth == maxDepth) {
                double nodesPerSecond = elapsedMs == 0 ? nodes : (nodes * 1000.0) / elapsedMs;
                System.out.printf("Nodes per second: %,.2f%n", nodesPerSecond);
                totalNodes = nodes;
            }
        }
        return totalNodes;
    }

    private static long searchParallel(long[] board, int player, int depth, boolean recursive) throws InterruptedException, ExecutionException {
        long[] moves = Gen.gen(board, false, false);
        int moveCount = (int) moves[Gen.MOVELIST_SIZE];
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<Long>> futures = new ArrayList<>();
        for(int i = 0; i < moveCount; i ++) {
            final long move = moves[i];
            futures.add(executor.submit(() -> {
                long[] nextBoard = Board.makeMove(board, move);
                if(Board.isPlayerInCheck(nextBoard, player)) return 0L;
                if(recursive) {
                    return search(nextBoard, 1 ^ player, depth - 1, depth - 1, 0);
                } else {
                    return searchFlattened(nextBoard, 1 ^ player, depth - 1, depth - 1, 0);
                }
            }));
        }
        long totalNodes = 0;
        for(Future<Long> future : futures) {
            totalNodes += future.get();
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);
        return totalNodes;
    }

    private static long search(long[] board, int player, int depth, int maxDepth, int firstMoveCount) {
        if(depth == 0) return 1;
        long nodes = 0;
        long[] moves = Gen.gen(board, false, false);
        int moveCount = (int) moves[Gen.MOVELIST_SIZE];
        for(int i = 0; i < moveCount; i ++) {
            long move = moves[i];
            long[] nextBoard = Board.makeMove(board, move);
            if(Board.isPlayerInCheck(nextBoard, player)) continue;
            /*
            if(depth == maxDepth) {
                long start = System.currentTimeMillis();
                long childNodes = search(nextBoard, 1 ^ player, depth - 1, maxDepth, firstMoveCount);
                long elapsed = System.currentTimeMillis() - start;
                System.out.printf("%d/%d  %s: %d  Elapsed: %d ms%n", i + 1, moveCount, Move.string(move), childNodes, elapsed);
                nodes += childNodes;
            } else */
            if(depth == 1) {
                nodes ++;
            } else {
                nodes += search(nextBoard, 1 ^ player, depth - 1, maxDepth, firstMoveCount);
            }
        }
        return nodes;
    }

    private static long searchDebug(long[] board, int player, int depth, int maxDepth, List<Long> movePath) {
        if(depth == 0) return 1;
        long nodes = 0;
        long[] moves = Gen.gen(board, true, false);
        int moveCount = (int) moves[Gen.MOVELIST_SIZE];

        for(int i = 0; i < moveCount; i ++) {
            long move = moves[i];
            long[] nextBoard = Board.makeMove(board, move);
            if(Board.isPlayerInCheck(nextBoard, player)) continue;
            movePath.add(move);
            if(depth == maxDepth && WATCH_MOVE != null && Move.string(move).equals(WATCH_MOVE)) {
                System.out.println("=== WATCH MOVE TRIGGERED ===");
                System.out.println("Move: " + Move.string(move));
                System.out.println(Board.boardString(nextBoard));
                System.out.println("Move list:");
                System.out.println(Move.moveListString(Gen.gen(nextBoard, true, false)));
                if(PRINT_MOVE_PATH) printMovePath(movePath);
                System.out.println("FEN: " + Board.toFen(nextBoard));
                System.out.println("Board to String:\n" + Board.toString(nextBoard));
            }
            if(depth == maxDepth) {
                long start = System.currentTimeMillis();
                long childNodes = searchDebug(nextBoard, 1 ^ player, depth - 1, maxDepth, movePath);
                long elapsed = System.currentTimeMillis() - start;
                System.out.printf("%d/%d  %s: %d  Elapsed: %d ms%n", i + 1, moveCount, Move.string(move), childNodes, elapsed);
                nodes += childNodes;
            } else if(depth == 1) {
                nodes ++;
            } else {
                nodes += searchDebug(nextBoard, 1 ^ player, depth - 1, maxDepth, movePath);
            }
            movePath.remove(movePath.size() - 1);
        }
        return nodes;
    }

    private static long searchFlattened(long[] rootBoard, int rootPlayer, int depth, int maxDepth, int firstMoveCount) {
        
        long totalNodes = 0;
        FrameStack stack = new FrameStack(32);
        stack.push(rootBoard, rootPlayer, depth);

        while(!stack.isEmpty()) {
            Frame frame = stack.peek();
            if(frame.moveIndex >= frame.moveCount) {
                stack.pop();
                continue;
            }
            long move = frame.moves[frame.moveIndex ++];
            long[] nextBoard = Board.makeMove(frame.board, move);
            if(Board.isPlayerInCheck(nextBoard, frame.player)) continue;
            if(frame.depth == 1) {
                totalNodes ++;
            } else {
                stack.push(nextBoard, frame.player ^ 1, frame.depth - 1);
            }
        }
        return totalNodes;
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch(InterruptedException ignored) {}
    }

    private static void printMovePath(List<Long> movePath) {
        System.out.println("Move path: ");
        for(long m : movePath) {
            System.out.print(Move.string(m) + " ");
        }
        System.out.println();
    }

}
