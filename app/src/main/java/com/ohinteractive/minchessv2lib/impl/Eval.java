package com.ohinteractive.minchessv2lib.impl;

import com.ohinteractive.minchessv2lib.util.BitOps;
import com.ohinteractive.minchessv2lib.util.Bitboard;
import com.ohinteractive.minchessv2lib.util.Crit;
import com.ohinteractive.minchessv2lib.util.Magic;
import com.ohinteractive.minchessv2lib.util.Piece;
import com.ohinteractive.minchessv2lib.util.TTable;
import com.ohinteractive.minchessv2lib.util.Value;

public class Eval {
    
    public static int eval(long board0, long board1, long board2, long board3, long status, long key) {
        final long t = table.probe(key).data();
        if(t != TTable.TYPE_INVALID) return (int) t;
        final int thisPlayer = (int) status & Board.PLAYER_BIT;
        final long whitePieceMask = ~board3;
        final long blackPieceMask = board3;
        final long whiteKing = board0 & ~board1 & ~board2 & whitePieceMask;
        final long whiteQueen = ~board0 & board1 & ~board2 & whitePieceMask;
        final long whiteRook = board0 & board1 & ~board2 & whitePieceMask;
        final long whiteBishop = ~board0 & ~board1 & board2 & whitePieceMask;
        final long whiteKnight = board0 & ~board1 & board2 & whitePieceMask;
        final long whitePawn = ~board0 & board1 & board2 & whitePieceMask;
        final int whiteQueenCount = Long.bitCount(whiteQueen);
        final int whiteRookCount = Long.bitCount(whiteRook);
        final int whiteBishopCount = Long.bitCount(whiteBishop);
        final int whiteKnightCount = Long.bitCount(whiteKnight);
        final long blackKing = board0 & ~board1 & ~board2 & blackPieceMask;
        final long blackQueen = ~board0 & board1 & ~board2 & blackPieceMask;
        final long blackRook = board0 & board1 & ~board2 & blackPieceMask;
        final long blackBishop = ~board0 & ~board1 & board2 & blackPieceMask;
        final long blackKnight = board0 & ~board1 & board2 & blackPieceMask;
        final long blackPawn = ~board0 & board1 & board2 & blackPieceMask;
        final int blackQueenCount = Long.bitCount(blackQueen);
        final int blackRookCount = Long.bitCount(blackRook);
        final int blackBishopCount = Long.bitCount(blackBishop);
        final int blackKnightCount = Long.bitCount(blackKnight);
        final int p = MAX_PHASE - (
            PHASE_VALUE[Piece.QUEEN ][whiteQueenCount  + blackQueenCount ] +
            PHASE_VALUE[Piece.ROOK  ][whiteRookCount   + blackRookCount  ] +
            PHASE_VALUE[Piece.BISHOP][whiteBishopCount + blackBishopCount] +
            PHASE_VALUE[Piece.KNIGHT][whiteKnightCount + blackKnightCount]);
        // Clamp phase to [0, MAX_PHASE] branchlessly for faster eval
        final int phase = (((p & ~(p >> 31)) - MAX_PHASE) >> 31 & (p & ~(p >> 31))) | (MAX_PHASE & ~(((p & ~(p >> 31)) - MAX_PHASE) >> 31));
        final int whiteKingSquare = BitOps.lsb(whiteKing);
        final int whiteKingRank = whiteKingSquare >>> Value.RANK_SHIFT;
        final int whiteKingFile = whiteKingSquare & Value.FILE;
        final int[][] QUEEN_VALUES  = PIECE_VALUE[Piece.QUEEN];
        final int[][] ROOK_VALUES   = PIECE_VALUE[Piece.ROOK];
        final int[][] BISHOP_VALUES = PIECE_VALUE[Piece.BISHOP];
        final int[][] KNIGHT_VALUES = PIECE_VALUE[Piece.KNIGHT];
        final int whitePieceMaterial =
            QUEEN_VALUES [whiteQueenCount ][phase] +
            ROOK_VALUES  [whiteRookCount  ][phase] +
            BISHOP_VALUES[whiteBishopCount][phase] +
            KNIGHT_VALUES[whiteKnightCount][phase];
        final long allOccupancy = board0 | board1 | board2;
        final long whiteOccupancy = allOccupancy & whitePieceMask;
        final long whiteBishopsKnights = whiteBishop | whiteKnight;
        final int blackKingSquare = BitOps.lsb(blackKing);
        final int blackKingRank = blackKingSquare >>> Value.RANK_SHIFT;
        final int blackKingFile = blackKingSquare & Value.FILE;
        final int blackPieceMaterial =
            QUEEN_VALUES [blackQueenCount ][phase] +
            ROOK_VALUES  [blackRookCount  ][phase] +
            BISHOP_VALUES[blackBishopCount][phase] +
            KNIGHT_VALUES[blackKnightCount][phase];
        final long blackOccupancy = allOccupancy & blackPieceMask;
        final long blackBishopsKnights = blackBishop | blackKnight;
        final long lightSquares = LIGHT_SQUARES_BITBOARD;
        final int whiteQueenEvalAndSafety = queenEval(whiteQueen, phase, Value.WHITE, whiteBishop, whiteKnight, allOccupancy, whiteOccupancy, blackKingRank, blackKingFile, blackKingSquare);
        final int whiteRookEvalAndSafety = rookEval(whiteRook, phase, Value.WHITE, whiteKing, whitePawn, allOccupancy, whiteOccupancy, blackPawn, blackQueen, blackKingRank, blackKingFile, blackKingSquare);
        final int whiteBishopEvalAndSafety = bishopEval(whiteBishop, phase, Value.WHITE, allOccupancy, whiteOccupancy, whitePawn, blackPawn, lightSquares, whiteKingRank, whiteKingFile, blackKingRank, blackKingFile, blackKingSquare);
        final int whiteKnightEvalAndSafety = knightEval(whiteKnight, phase, whitePawn, Value.WHITE, whiteOccupancy, blackPawn, whiteKingRank, whiteKingFile, blackKingRank, blackKingFile, blackKingSquare);
        int whiteEval =
        kingEval(Value.WHITE, whiteKingSquare, phase, whiteKingRank, whiteKingFile, whiteRook, whitePawn, blackPawn, whitePieceMaterial, blackPieceMaterial, blackKingFile, blackKingRank) +
        (whiteQueenEvalAndSafety >> EVAL_SHIFT) +
        (whiteRookEvalAndSafety >> EVAL_SHIFT) +
        (whiteBishopEvalAndSafety >> EVAL_SHIFT) +
        (whiteKnightEvalAndSafety >> EVAL_SHIFT) +
        pawnEval(whitePawn, phase, Value.WHITE, whiteBishopsKnights, blackPawn, whitePieceMaterial, whiteKingRank, whiteKingFile, blackKingRank, blackKingFile, blackPieceMaterial, thisPlayer, whiteKing);
        final int blackQueenEvalAndSafety = queenEval(blackQueen, phase, Value.BLACK, blackBishop, blackKnight, allOccupancy, blackOccupancy, whiteKingRank, whiteKingFile, whiteKingSquare);
        final int blackRookEvalAndSafety = rookEval(blackRook, phase, Value.BLACK, blackKing, blackPawn, allOccupancy, blackOccupancy, whitePawn, whiteQueen, whiteKingRank, whiteKingFile, whiteKingSquare);
        final int blackBishopEvalAndSafety = bishopEval(blackBishop, phase, Value.BLACK, allOccupancy, blackOccupancy, blackPawn, whitePawn, lightSquares, blackKingRank, blackKingFile, whiteKingRank, whiteKingFile, whiteKingSquare);
        final int blackKnightEvalAndSafety = knightEval(blackKnight, phase, blackPawn, Value.BLACK, blackOccupancy, whitePawn, blackKingRank, blackKingFile, whiteKingRank, whiteKingFile, whiteKingSquare);
        int blackEval =
        kingEval(Value.BLACK, blackKingSquare, phase, blackKingRank, blackKingFile, blackRook, blackPawn, whitePawn, blackPieceMaterial, whitePieceMaterial, whiteKingFile, whiteKingRank) +
        (blackQueenEvalAndSafety >> EVAL_SHIFT) +
        (blackRookEvalAndSafety >> EVAL_SHIFT) +
        (blackBishopEvalAndSafety >> EVAL_SHIFT) +
        (blackKnightEvalAndSafety >> EVAL_SHIFT) +
        pawnEval(blackPawn, phase, Value.BLACK, blackBishopsKnights, whitePawn, blackPieceMaterial, blackKingRank, blackKingFile, whiteKingRank, whiteKingFile, whitePieceMaterial, thisPlayer, blackKing);
        final int whiteSafety = (blackQueenEvalAndSafety & 0xff) + (blackRookEvalAndSafety & 0xff) + (blackBishopEvalAndSafety & 0xff) + (blackKnightEvalAndSafety & 0xff);
        whiteEval -= SAFETY_VALUE[whiteSafety];
        final int blackSafety = (whiteQueenEvalAndSafety & 0xff) + (whiteRookEvalAndSafety & 0xff) + (whiteBishopEvalAndSafety & 0xff) + (whiteKnightEvalAndSafety & 0xff);
        blackEval -= SAFETY_VALUE[blackSafety];
        int eval = (thisPlayer == Value.WHITE ? whiteEval - blackEval : blackEval - whiteEval);
        eval = drawEval(eval, (int) status >>> Board.HALF_MOVE_CLOCK_SHIFT & Board.HALF_MOVE_CLOCK_BITS, Long.bitCount(allOccupancy), whiteBishopsKnights, blackBishopsKnights, whiteBishop, blackBishop, Long.bitCount(whiteBishop), Long.bitCount(blackBishop));
        table.save(key, 0, TTable.TYPE_EVAL, eval, 0L);
        return eval;
    }

    private static int drawEval(int eval, int halfMoveClock, int allOccupancyCount, long whiteBishopsKnights, long blackBishopsKnights, long whiteBishop, long blackBishop, int whiteBishopsCount, int blackBishopsCount) {
        if(halfMoveClock >= 100) return 0;
        if(allOccupancyCount < 3) return 0;
        if(allOccupancyCount == 3) {
            if(whiteBishopsKnights != 0L || blackBishopsKnights != 0L) return 0;
        }
        if(allOccupancyCount == 4) {
            if(whiteBishopsCount == 1 && blackBishopsCount == 1) {
                final int whiteSquare = BitOps.lsb(whiteBishop);
                final int blackSquare = BitOps.lsb(blackBishop);
                if(((whiteSquare >>> 3 & 1) == (whiteSquare & 1)) == ((blackSquare >>> 3 & 1) == (blackSquare & 1))) return 0;
            }
        }
        return eval;
    }

    private static TTable table = new TTable();

    private static final int CENTRE_FILE = 3;
    private static final int CENTRE_RANK = 3;
    private static final int EDGE_WEIGHT = 10;
    private static final int PROXIMITY_WEIGHT = 20;
    private static final int MAX_PHASE = 24;
    private static final int EVAL_SHIFT = 8;
    private static final int WHITE_BACK_RANK = 0;
    private static final int BLACK_BACK_RANK = 7;
    
    private static final int[] SAFETY_VALUE = {
        0,  0,   1,   2,   3,   5,   7,   9,  12,  15,
       18,  22,  26,  30,  35,  39,  44,  50,  56,  62,
       68,  75,  82,  85,  89,  97, 105, 113, 122, 131,
      140, 150, 169, 180, 191, 202, 213, 225, 237, 248,
      260, 272, 283, 295, 307, 319, 330, 342, 354, 366,
      377, 389, 401, 412, 424, 436, 448, 459, 471, 483,
      494, 500, 500, 500, 500, 500, 500, 500, 500, 500,
      500, 500, 500, 500, 500, 500, 500, 500, 500, 500,
      500, 500, 500, 500, 500, 500, 500, 500, 500, 500,
      500, 500, 500, 500, 500, 500, 500, 500, 500, 500
    };
    private static final long LIGHT_SQUARES_BITBOARD;
    static {
        LIGHT_SQUARES_BITBOARD = Bitboard.BB[Bitboard.SQUARE_COLOR_LIGHT][0];
    }

    private static final long[] LEAP_ATTACKS_BITBOARD = new long[64];
    static {
        for(int square = 0; square < 64; square ++) {
            LEAP_ATTACKS_BITBOARD[square] = Bitboard.BB[Bitboard.LEAP_ATTACKS][square];
        }
    }

    private static final long[][] PAWN_ATTACKS = new long[2][64];
    private static final long[][] KING_RING = new long[2][64];
    private static final long[][] PASSED_PAWNS_FILES = new long[2][8];
    private static final long[][] FORWARD_RANKS = new long[2][8];
    static {
        for(int player = 0; player < 2; player ++) {
            for(int square = 0; square < 64; square ++) {
                PAWN_ATTACKS[player][square] = Bitboard.BB[Bitboard.PAWN_ATTACKS_PLAYER0 + player][square];
                KING_RING[player][square] = Bitboard.BB[Bitboard.KING_RING_PLAYER0 + player][square];
            }
            for(int fileOrRank = 0; fileOrRank < 8; fileOrRank ++) {
                PASSED_PAWNS_FILES[player][fileOrRank] = Bitboard.BB[Bitboard.PASSED_PAWNS_FILES_PLAYER0 + player][fileOrRank];
                FORWARD_RANKS[player][fileOrRank] = Bitboard.BB[Bitboard.FORWARD_RANKS_PLAYER0 + player][fileOrRank];
            }
        }
    }

    private static final long[] FILE_BITBOARD = new long[8];
    private static final long[] RANK_BITBOARD = new long[8];
    static {
        for(int fileOrRank = 0; fileOrRank < 8; fileOrRank ++) {
            FILE_BITBOARD[fileOrRank] = Bitboard.BB[Bitboard.FILE][fileOrRank];
            RANK_BITBOARD[fileOrRank] = Bitboard.BB[Bitboard.RANK][fileOrRank];
        }
    }

    private static final int[][][] PIECE_VALUE = new int[7][11][25];
    static {
        for(int phase = 0; phase < 25; phase ++) {
            for(int numPiece = 1; numPiece < 11; numPiece ++) {
                PIECE_VALUE[Piece.QUEEN][numPiece][phase] = Crit.MATERIAL[Piece.QUEEN][phase] * numPiece;
                PIECE_VALUE[Piece.ROOK][numPiece][phase] = Crit.MATERIAL[Piece.ROOK][phase] * numPiece;
                PIECE_VALUE[Piece.BISHOP][numPiece][phase] = Crit.MATERIAL[Piece.BISHOP][phase] * numPiece;
                PIECE_VALUE[Piece.KNIGHT][numPiece][phase] = Crit.MATERIAL[Piece.KNIGHT][phase] * numPiece;
                PIECE_VALUE[Piece.PAWN][numPiece][phase] = Crit.MATERIAL[Piece.PAWN][phase] * numPiece;
            }
        }
    }
    private static final int[][] PHASE_VALUE = new int[7][19];
    static {
        for(int numPiece = 1; numPiece < 19; numPiece ++) {
            PHASE_VALUE[Piece.QUEEN][numPiece] = numPiece * 4;
            PHASE_VALUE[Piece.ROOK][numPiece] = numPiece * 2;
            PHASE_VALUE[Piece.BISHOP][numPiece] = numPiece;
            PHASE_VALUE[Piece.KNIGHT][numPiece] = numPiece;
        }
    }
    private static final int QUEEN_VALUE = PIECE_VALUE[Piece.QUEEN][1][24];
    private static final int[] BISHOP_VALUE = new int[25];
    static {
        for(int phase = 0; phase < 25; phase ++) {
            BISHOP_VALUE[phase] = PIECE_VALUE[Piece.BISHOP][1][phase];
        }
    }
    private static final int[][][][] BONUS = new int[7][2][64][25];
    static {
        for(int type = Piece.KING; type <= Piece.PAWN; type ++) {
            for(int player = 0; player < 2; player ++) {
                for(int square = 0; square < 64; square ++) {
                    for(int phase = 0; phase < 25; phase ++) {
                        BONUS[type][player][square][phase] = Crit.BONUS[type][player][square][phase];
                    }
                }
            }
        }
    }
    private static final int[][] PASSED_PAWN_RANK_BONUS = new int[2][8];
    static {
        for(int player = 0; player < 2; player ++) {
            for(int rank = 0; rank < 8; rank ++) {
                PASSED_PAWN_RANK_BONUS[player][rank] = 50 * (player == 0 ? rank : 7 - rank);
            }
        }
    }
    private static final int[] ROOK_PROTECTS = new int[25];
    private static final int[] KING_BLOCKS_ROOK = new int[25];
    private static final int[] QUEEN_EARLY_DEVELOPMENT = new int[25];
    private static final int[] ROOK_EARLY_DEVELOPMENT = new int[25];
    private static final int[] ROOK_PAIR = new int[25];
    private static final int[] ROOK_OPEN_FILE = new int[25];
    private static final int[] ROOK_ON_QUEEN_FILE = new int[25];
    private static final int[] BISHOP_PAIR = new int[25];
    private static final int[] BISHOP_OUTPOST = new int[25];
    private static final int[] KNIGHT_PAIR = new int[25];
    private static final int[] KNIGHT_OUTPOST = new int[25];
    private static final int[] DOUBLED_PAWN = new int[25];
    private static final int[] WEAK_PAWN = new int[25];
    private static final int[] ISOLATED_PAWN = new int[25];
    private static final int[] PAWN_PROTECTS = new int[25];
    private static final int[] PASSED_PAWN_PHALANX = new int[25];
    static {
        for(int phase = 0; phase < 25; phase ++) {
            ROOK_PROTECTS[phase] = ((int[]) Crit.VALUE[Crit.ROOK_PROTECTS])[phase];
            KING_BLOCKS_ROOK[phase] = ((int[]) Crit.VALUE[Crit.KING_BLOCKS_ROOK])[phase];
            QUEEN_EARLY_DEVELOPMENT[phase] = ((int[]) Crit.VALUE[Crit.QUEEN_EARLY_DEVELOPMENT])[phase];
            ROOK_EARLY_DEVELOPMENT[phase] = ((int[]) Crit.VALUE[Crit.ROOK_EARLY_DEVELOPMENT])[phase];
            ROOK_PAIR[phase] = ((int[]) Crit.VALUE[Crit.ROOK_PAIR])[phase];
            ROOK_OPEN_FILE[phase] = ((int[]) Crit.VALUE[Crit.ROOK_OPEN_FILE])[phase];
            ROOK_ON_QUEEN_FILE[phase] = ((int[]) Crit.VALUE[Crit.ROOK_ON_QUEEN_FILE])[phase];
            BISHOP_PAIR[phase] = ((int[]) Crit.VALUE[Crit.BISHOP_PAIR])[phase];
            BISHOP_OUTPOST[phase] = ((int[]) Crit.VALUE[Crit.BISHOP_OUTPOST])[phase];
            KNIGHT_PAIR[phase] = ((int[]) Crit.VALUE[Crit.KNIGHT_PAIR])[phase];
            KNIGHT_OUTPOST[phase] = ((int[]) Crit.VALUE[Crit.KNIGHT_OUTPOST])[phase];
            DOUBLED_PAWN[phase] = ((int[]) Crit.VALUE[Crit.DOUBLED_PAWN])[phase];
            WEAK_PAWN[phase] = ((int[]) Crit.VALUE[Crit.WEAK_PAWN])[phase];
            ISOLATED_PAWN[phase] = ((int[]) Crit.VALUE[Crit.ISOLATED_PAWN])[phase];
            PAWN_PROTECTS[phase] = ((int[]) Crit.VALUE[Crit.PAWN_PROTECTS])[phase];
            PASSED_PAWN_PHALANX[phase] = ((int[]) Crit.VALUE[Crit.PASSED_PAWN_PHALANX])[phase];
        }
    }
    private static final int[] QUEEN_AFFECTS_KING_SAFETY = new int[9];
    private static final int[] ROOK_AFFECTS_KING_SAFETY = new int[6];
    private static final int[] BISHOP_AFFECTS_KING_SAFETY = new int[5];
    private static final int[] KNIGHT_AFFECTS_KING_SAFETY = new int[4];
    static {
        for(int attacks = 1; attacks < 9; attacks ++) {
            QUEEN_AFFECTS_KING_SAFETY[attacks] = ((int[]) Crit.VALUE[Crit.QUEEN_AFFECTS_KING_SAFETY])[attacks];
            if(attacks < 6) ROOK_AFFECTS_KING_SAFETY[attacks] = ((int[]) Crit.VALUE[Crit.ROOK_AFFECTS_KING_SAFETY])[attacks];
            if(attacks < 5) BISHOP_AFFECTS_KING_SAFETY[attacks] = ((int[]) Crit.VALUE[Crit.BISHOP_AFFECTS_KING_SAFETY])[attacks];
            if(attacks < 4) KNIGHT_AFFECTS_KING_SAFETY[attacks] = ((int[]) Crit.VALUE[Crit.KNIGHT_AFFECTS_KING_SAFETY])[attacks];
        }
    }
    private static final int[][] QUEEN_ENEMY_KING_DISTANCE = new int[15][25];
    private static final int[][] ROOK_ENEMY_KING_DISTANCE = new int[15][25];
    private static final int[][] BISHOP_ENEMY_KING_DISTANCE = new int[15][25];
    private static final int[][] KNIGHT_ENEMY_KING_DISTANCE = new int[15][25];
    private static final int[][] BISHOP_PROTECTOR = new int[15][25];
    private static final int[][] KNIGHT_PROTECTOR = new int[15][25];
    static {
        for(int distance = 1; distance < 15; distance ++) {
            for(int phase = 0; phase < 25; phase ++) {
                QUEEN_ENEMY_KING_DISTANCE[distance][phase] = ((int[][]) Crit.VALUE[Crit.QUEEN_ENEMY_KING_DISTANCE])[distance][phase];
                ROOK_ENEMY_KING_DISTANCE[distance][phase] = ((int[][])  Crit.VALUE[Crit.ROOK_ENEMY_KING_DISTANCE])[distance][phase];
                BISHOP_ENEMY_KING_DISTANCE[distance][phase] = ((int[][])  Crit.VALUE[Crit.BISHOP_ENEMY_KING_DISTANCE])[distance][phase];
                KNIGHT_ENEMY_KING_DISTANCE[distance][phase] = ((int[][])  Crit.VALUE[Crit.KNIGHT_ENEMY_KING_DISTANCE])[distance][phase];
                BISHOP_PROTECTOR[distance][phase] = ((int[][]) Crit.VALUE[Crit.BISHOP_PROTECTOR])[distance][phase];
                KNIGHT_PROTECTOR[distance][phase] = ((int[][]) Crit.VALUE[Crit.KNIGHT_PROTECTOR])[distance][phase];
            }
        }
    }
    private static final int[][] MOBILITY_QUEEN = new int[29][25];
    private static final int[][] MOBILITY_ROOK = new int[15][25];
    private static final int[][] MOBILITY_BISHOP = new int[15][25];
    private static final int[][] MOBILITY_KNIGHT = new int[9][25];
    static {
        for(int phase = 0; phase < 25; phase ++) {
            for(int mobility = 1; mobility < 29; mobility ++) {
                MOBILITY_QUEEN[mobility][phase] = ((int[][]) Crit.VALUE[Crit.MOBILITY_QUEEN])[mobility][phase];
                if(mobility < 15) {
                    MOBILITY_ROOK[mobility][phase] = ((int[][]) Crit.VALUE[Crit.MOBILITY_ROOK])[mobility][phase];
                    MOBILITY_BISHOP[mobility][phase] = ((int[][]) Crit.VALUE[Crit.MOBILITY_BISHOP])[mobility][phase];
                }    
                if(mobility < 9) {
                    MOBILITY_KNIGHT[mobility][phase] = ((int[][]) Crit.VALUE[Crit.MOBILITY_KNIGHT])[mobility][phase];
                }       
            }
        }
    }
    private static final int[][][] PAWN_SHIELD_CLOSE = new int[2][4][25];
    private static final int[][][] PAWN_SHIELD_FAR = new int[2][4][25];
    private static final int[][][] PAWN_STORM_CLOSE = new int[2][4][25];
    private static final int[][][] PAWN_STORM_FAR = new int[2][4][25];
    static {
        for(int side = 0; side < 2; side ++) {
            for(int numPawns = 0; numPawns < 4; numPawns ++) {
                for(int phase = 0; phase < 25; phase ++) {
                    PAWN_SHIELD_CLOSE[side][numPawns][phase] = ((int[][][]) Crit.VALUE[Crit.PAWN_SHIELD_CLOSE])[side][numPawns][phase];
                    PAWN_SHIELD_FAR[side][numPawns][phase] = ((int[][][]) Crit.VALUE[Crit.PAWN_SHIELD_FAR])[side][numPawns][phase];
                    PAWN_STORM_CLOSE[side][numPawns][phase] = ((int[][][]) Crit.VALUE[Crit.PAWN_STORM_CLOSE])[side][numPawns][phase];
                    PAWN_STORM_FAR[side][numPawns][phase] = ((int[][][]) Crit.VALUE[Crit.PAWN_STORM_FAR])[side][numPawns][phase];
                }
            }
        }
    }
    private static final int[][][] ROOK_PAWN = new int[3][9][25];
    private static final int[][][] KNIGHT_PAWN = new int[3][9][25];
    static {
        for(int numPiece = 1; numPiece < 3; numPiece ++) {
            for(int numPawns = 0; numPawns < 9; numPawns ++) {
                for(int phase = 0; phase < 25; phase ++) {
                    ROOK_PAWN[numPiece][numPawns][phase] = ((int[][][]) Crit.VALUE[Crit.ROOK_PAWN])[numPiece][numPawns][phase];
                    KNIGHT_PAWN[numPiece][numPawns][phase] = ((int[][][]) Crit.VALUE[Crit.ROOK_PAWN])[numPiece][numPawns][phase];
                }
            }
        }
    }
    private static final int[][][] BAD_BISHOP = new int[9][9][25];
    static {
        for(int numPawns = 0; numPawns < 9; numPawns ++) {
            for(int otherPawns = 0; otherPawns < 9; otherPawns ++) {
                for(int phase = 0; phase < 25; phase ++) {
                    BAD_BISHOP[numPawns][otherPawns][phase] = ((int[][][]) Crit.VALUE[Crit.BAD_BISHOP])[numPawns][otherPawns][phase];
                }
            }
        }
    }

    private Eval() {}

    private static int kingEval(int player, int kingSquare, int phase, int kingRank, int kingFile, long rookBitboard, long pawnBitboard, long otherPawnBitboard, int pieceMaterial, int otherPieceMaterial, int otherKingFile, int otherKingRank) {
        // init eval and king square bonus
        int eval = BONUS[Piece.KING][player][kingSquare][phase];
        // king on back rank evals - king blocks rook, rook protects king, pawn shield, opponent pawn storm
        if(kingRank == (player == Value.WHITE ? WHITE_BACK_RANK : BLACK_BACK_RANK)) {
            switch(kingFile) {
                case 0: {
                    if(((player == Value.WHITE ? 0x000000000000000eL : 0x0e00000000000000L) & rookBitboard) != 0L) eval += ROOK_PROTECTS[phase];
                    eval += PAWN_SHIELD_CLOSE[1][Long.bitCount(Bitboard.BB[Bitboard.PAWN_SHIELD_QUEENSIDE_CLOSE_PLAYER0 + player][0] & pawnBitboard)][phase] +
                            PAWN_SHIELD_FAR[1][Long.bitCount(Bitboard.BB[Bitboard.PAWN_SHIELD_QUEENSIDE_FAR_PLAYER0 + player][0] & pawnBitboard)][phase] -
                            PAWN_STORM_CLOSE[1][Long.bitCount(Bitboard.BB[Bitboard.PAWN_STORM_QUEENSIDE_CLOSE_PLAYER0 + player][0] & otherPawnBitboard)][phase] -
                            PAWN_STORM_FAR[1][Long.bitCount(Bitboard.BB[Bitboard.PAWN_STORM_QUEENSIDE_FAR_PLAYER0 + player][0] & otherPawnBitboard)][phase];
                    break;
                }
                case 1: {
                    if(((player == Value.WHITE ? 0x000000000000000cL : 0x0c00000000000000L) & rookBitboard) != 0L) eval += ROOK_PROTECTS[phase];
                    if((Bitboard.BB[Bitboard.ROOK_START_POSITION_PLAYER0 + player][0] & 0x0100000000000001L & rookBitboard) != 0L) eval += KING_BLOCKS_ROOK[phase];
                    eval += PAWN_SHIELD_CLOSE[1][Long.bitCount(Bitboard.BB[Bitboard.PAWN_SHIELD_QUEENSIDE_CLOSE_PLAYER0 + player][0] & pawnBitboard)][phase] +
                            PAWN_SHIELD_FAR[1][Long.bitCount(Bitboard.BB[Bitboard.PAWN_SHIELD_QUEENSIDE_FAR_PLAYER0 + player][0] & pawnBitboard)][phase] -
                            PAWN_STORM_CLOSE[1][Long.bitCount(Bitboard.BB[Bitboard.PAWN_STORM_QUEENSIDE_CLOSE_PLAYER0 + player][0] & otherPawnBitboard)][phase] -
                            PAWN_STORM_FAR[1][Long.bitCount(Bitboard.BB[Bitboard.PAWN_STORM_QUEENSIDE_FAR_PLAYER0 + player][0] & otherPawnBitboard)][phase];
                    break;
                }
                case 2: {
                    if(((player == Value.WHITE ? 0x0000000000000008L : 0x0800000000000000L) & rookBitboard) != 0L) eval += ROOK_PROTECTS[phase];
                    if((Bitboard.BB[Bitboard.ROOK_START_POSITION_PLAYER0 + player][0] & 0x0300000000000003L & rookBitboard) != 0L) eval += KING_BLOCKS_ROOK[phase];
                    eval += PAWN_SHIELD_CLOSE[1][Long.bitCount(Bitboard.BB[Bitboard.PAWN_SHIELD_QUEENSIDE_CLOSE_PLAYER0 + player][0] & pawnBitboard)][phase] +
                            PAWN_SHIELD_FAR[1][Long.bitCount(Bitboard.BB[Bitboard.PAWN_SHIELD_QUEENSIDE_FAR_PLAYER0 + player][0] & pawnBitboard)][phase] -
                            PAWN_STORM_CLOSE[1][Long.bitCount(Bitboard.BB[Bitboard.PAWN_STORM_QUEENSIDE_CLOSE_PLAYER0 + player][0] & otherPawnBitboard)][phase] -
                            PAWN_STORM_FAR[1][Long.bitCount(Bitboard.BB[Bitboard.PAWN_STORM_QUEENSIDE_FAR_PLAYER0 + player][0] & otherPawnBitboard)][phase];
                    break;
                }
                case 3: {
                    if((Bitboard.BB[Bitboard.ROOK_START_POSITION_PLAYER0 + player][0] & 0x0700000000000007L & rookBitboard) != 0L) eval += KING_BLOCKS_ROOK[phase];
                    break;
                }
                case 4: {
                    break;
                }
                case 5: {
                    if(((player == Value.WHITE ? 0x0000000000000010L : 0x1000000000000000L) & rookBitboard) != 0L) eval += ROOK_PROTECTS[phase];
                    if((Bitboard.BB[Bitboard.ROOK_START_POSITION_PLAYER0 + player][0] & 0xe0000000000000e0L & rookBitboard) != 0L) eval += KING_BLOCKS_ROOK[phase];
                    eval += PAWN_SHIELD_CLOSE[0][Long.bitCount(Bitboard.BB[Bitboard.PAWN_SHIELD_KINGSIDE_CLOSE_PLAYER0 + player][0] & pawnBitboard)][phase] +
                            PAWN_SHIELD_FAR[0][Long.bitCount(Bitboard.BB[Bitboard.PAWN_SHIELD_KINGSIDE_FAR_PLAYER0 + player][0] & pawnBitboard)][phase] -
                            PAWN_STORM_CLOSE[0][Long.bitCount(Bitboard.BB[Bitboard.PAWN_STORM_KINGSIDE_CLOSE_PLAYER0 + player][0] & otherPawnBitboard)][phase] -
                            PAWN_STORM_FAR[0][Long.bitCount(Bitboard.BB[Bitboard.PAWN_STORM_KINGSIDE_FAR_PLAYER0 + player][0] & otherPawnBitboard)][phase];
                    break;
                }
                case 6: {
                    if(((player == Value.WHITE ? 0x0000000000000030L : 0x3000000000000000L) & rookBitboard) != 0L) eval += ROOK_PROTECTS[phase];
                    if((Bitboard.BB[Bitboard.ROOK_START_POSITION_PLAYER0 + player][0] & 0xc0000000000000c0L & rookBitboard) != 0L) eval += KING_BLOCKS_ROOK[phase];
                    eval += PAWN_SHIELD_CLOSE[0][Long.bitCount(Bitboard.BB[Bitboard.PAWN_SHIELD_KINGSIDE_CLOSE_PLAYER0 + player][0] & pawnBitboard)][phase] +
                            PAWN_SHIELD_FAR[0][Long.bitCount(Bitboard.BB[Bitboard.PAWN_SHIELD_KINGSIDE_FAR_PLAYER0 + player][0] & pawnBitboard)][phase] -
                            PAWN_STORM_CLOSE[0][Long.bitCount(Bitboard.BB[Bitboard.PAWN_STORM_KINGSIDE_CLOSE_PLAYER0 + player][0] & otherPawnBitboard)][phase] -
                            PAWN_STORM_FAR[0][Long.bitCount(Bitboard.BB[Bitboard.PAWN_STORM_KINGSIDE_FAR_PLAYER0 + player][0] & otherPawnBitboard)][phase];
                    break;
                }
                case 7: {
                    if(((player == Value.WHITE ? 0x0000000000000070L : 0x7000000000000000L) & rookBitboard) != 0L) eval += ROOK_PROTECTS[phase];
                    eval += PAWN_SHIELD_CLOSE[0][Long.bitCount(Bitboard.BB[Bitboard.PAWN_SHIELD_KINGSIDE_CLOSE_PLAYER0 + player][0] & pawnBitboard)][phase] +
                            PAWN_SHIELD_FAR[0][Long.bitCount(Bitboard.BB[Bitboard.PAWN_SHIELD_KINGSIDE_FAR_PLAYER0 + player][0] & pawnBitboard)][phase] -
                            PAWN_STORM_CLOSE[0][Long.bitCount(Bitboard.BB[Bitboard.PAWN_STORM_KINGSIDE_CLOSE_PLAYER0 + player][0] & otherPawnBitboard)][phase] -
                            PAWN_STORM_FAR[0][Long.bitCount(Bitboard.BB[Bitboard.PAWN_STORM_KINGSIDE_FAR_PLAYER0 + player][0] & otherPawnBitboard)][phase];
                    break;
                }
                default: break;
            }
        }
        // king distance
        if(pieceMaterial > otherPieceMaterial && pieceMaterial <= Piece.VALUE[Piece.QUEEN]) {
            final int opponentKingDistanceFromCenter = Math.abs(otherKingFile - CENTRE_FILE) + Math.abs(otherKingRank - CENTRE_RANK);
            final int kingDistance = Math.abs(kingFile - otherKingFile) + Math.abs(kingRank - otherKingRank);
            final int opponentKingEdgePenalty = opponentKingDistanceFromCenter * EDGE_WEIGHT;
            final int kingProximityBonus = (14 - kingDistance) * PROXIMITY_WEIGHT;
            eval += ((opponentKingEdgePenalty + kingProximityBonus) * (MAX_PHASE - phase)) / MAX_PHASE;
        }
        return eval;
    }
    
    private static int queenEval(long bitboard, int phase, int player, long bishopBitboard, long knightBitboard, long allOccupancy, long occupancy, int otherKingRank, int otherKingFile, int otherKingSquare) {
        // init eval and material value
        int eval = PIECE_VALUE[Piece.QUEEN][Long.bitCount(bitboard)][phase];
        int safety = 0;
        // early development
        if((bitboard & Bitboard.BB[Bitboard.QUEEN_START_POSITION_PLAYER0 + player][0]) == 0L &&
            (bishopBitboard & Bitboard.BB[Bitboard.BISHOP_START_POSITION_PLAYER0 + player][0]) != 0L &&
            (knightBitboard & Bitboard.BB[Bitboard.KNIGHT_START_POSITION_PLAYER0 + player][0]) != 0L) eval += QUEEN_EARLY_DEVELOPMENT[phase];
        // loop over queens
        while(bitboard != 0L) {
            final int square = BitOps.lsb(bitboard);
            bitboard &= bitboard - 1;
            // piece square bonus
            eval += BONUS[Piece.QUEEN][player][square][phase];
            // mobility
            final long queenAttacks = Magic.queenMoves(square, allOccupancy) & ~occupancy;
            eval += MOBILITY_QUEEN[Long.bitCount(queenAttacks)][phase];
            // other king distance
            eval += QUEEN_ENEMY_KING_DISTANCE[Math.abs((square >>> 3) - otherKingRank) + Math.abs((square & 7) - otherKingFile)][phase];
            // other king safety
            safety = QUEEN_AFFECTS_KING_SAFETY[Long.bitCount(queenAttacks & KING_RING[1 ^ player][otherKingSquare])];
        }
        return (eval << 8) | (safety & 0xff);
    }

    private static int rookEval(long bitboard, int phase, int player, long kingBitboard, long pawnBitboard, long allOccupancy, long occupancy, long otherPawnBitboard, long otherQueenBitboard, int otherKingRank, int otherKingFile, int otherKingSquare) {
        // init and material value
        final int numRooks = Long.bitCount(bitboard);
        int eval = PIECE_VALUE[Piece.ROOK][numRooks][phase];
        int safety = 0;
        // early development
        if(Long.bitCount(bitboard & Bitboard.BB[Bitboard.ROOK_START_POSITION_PLAYER0 + player][0]) < 2 && (kingBitboard & Bitboard.BB[Bitboard.KING_START_POSITION_PLAYER0 + player][0]) != 0L) eval += ROOK_EARLY_DEVELOPMENT[phase];
        // rook pair
        eval += numRooks > 1 ? ROOK_PAIR[phase] : 0;
        // rooks and pawns
        final int numPawns = Long.bitCount(pawnBitboard);
        eval += ROOK_PAWN[numRooks > 2 ? 2 : numRooks][numPawns > 8 ? 8 : numPawns][phase];
        // loop over rooks
        while(bitboard != 0L) {
            final int square = BitOps.lsb(bitboard);
            bitboard &= bitboard - 1;
            // piece square bonus
            eval += BONUS[Piece.ROOK][player][square][phase];
            // mobility
            final long rookAttacks = Magic.rookMoves(square, allOccupancy) & ~occupancy;
            eval += MOBILITY_ROOK[Long.bitCount(rookAttacks)][phase];
            // rook open file
            final int rookFile = square & 7;
            eval += ((pawnBitboard & Bitboard.BB[Bitboard.FILE][rookFile]) == 0L ? ROOK_OPEN_FILE[phase] : 0) + ((otherPawnBitboard & Bitboard.BB[Bitboard.FILE][rookFile]) == 0L ? ROOK_OPEN_FILE[phase] : 0);
            // rook opposed enemy queen
            eval += (otherQueenBitboard & Bitboard.BB[Bitboard.FILE][rookFile]) != 0L ? ROOK_ON_QUEEN_FILE[phase] : 0;
            // other king distance
            eval += ROOK_ENEMY_KING_DISTANCE[Math.abs((square >>> 3) - otherKingRank) + Math.abs(rookFile - otherKingFile)][phase];
            // other king safety
            safety = QUEEN_AFFECTS_KING_SAFETY[Long.bitCount(rookAttacks & KING_RING[1 ^ player][otherKingSquare])];
        }
        return (eval << 8) | (safety & 0xff);
    }

    private static int bishopEval(long bitboard, int phase, int player, long allOccupancy, long occupancy, long pawnBitboard, long otherPawnBitboard, long lightSquares, int kingRank, int kingFile, int otherKingRank, int otherKingFile, int otherKingSquare) {
        // init eval and material value
        final int numBishops = Long.bitCount(bitboard);
        int eval = PIECE_VALUE[Piece.BISHOP][numBishops][phase];
        int safety = 0;
        // bishop pair
        eval += numBishops > 1 ? BISHOP_PAIR[phase] : 0;
        // loop over bishops
        while(bitboard != 0L) {
            final int square = BitOps.lsb(bitboard);
            bitboard &= bitboard - 1;
            // piece square bonus
            eval += BONUS[Piece.BISHOP][player][square][phase];
            // mobility
            final long bishopAttacks = Magic.bishopMoves(square, allOccupancy) & ~occupancy;
            eval += MOBILITY_BISHOP[Long.bitCount(bishopAttacks)][phase];
            // outpost
            final int bishopFile = square & 7;
            final int bishopRank = square >>> 3;
            if((PAWN_ATTACKS[1 ^ player][square] & pawnBitboard) != 0L) {
                if((PASSED_PAWNS_FILES[player][bishopFile] & FORWARD_RANKS[player][bishopRank] & otherPawnBitboard) == 0L) eval += BISHOP_OUTPOST[phase];
            }
            // bad bishop
            final long squareColorBitboard = (square >>> 3 & 1) == (square & 1) ? ~lightSquares : lightSquares;
            eval += BAD_BISHOP[Long.bitCount(pawnBitboard & squareColorBitboard)][Long.bitCount(otherPawnBitboard & squareColorBitboard)][phase];
            // own king distance
            eval += BISHOP_PROTECTOR[Math.abs(bishopRank - kingRank) + Math.abs(bishopFile - kingFile)][phase];
            // other king distance
            eval += BISHOP_ENEMY_KING_DISTANCE[Math.abs(bishopRank - otherKingRank) + Math.abs(bishopFile - otherKingFile)][phase];
            // other king safety
            safety = QUEEN_AFFECTS_KING_SAFETY[Long.bitCount(bishopAttacks & KING_RING[1 ^ player][otherKingSquare])];
        }
        return (eval << 8) | (safety & 0xff);
    }

    private static int knightEval(long bitboard, int phase, long pawnBitboard, int player, long occupancy, long otherPawnBitboard, int kingRank, int kingFile, int otherKingRank, int otherKingFile, int otherKingSquare) {
        // init eval and material value
        final int numKnights = Long.bitCount(bitboard);
        int eval = PIECE_VALUE[Piece.KNIGHT][numKnights][phase];
        int safety = 0;
        // knight pair
        eval += numKnights > 1 ? KNIGHT_PAIR[phase] : 0;
        // knight and pawns
        final int numPawns = Long.bitCount(pawnBitboard);
        eval += KNIGHT_PAWN[numKnights > 2 ? 2 : numKnights][numPawns > 8 ? 8 : numPawns][phase];
        // loop over knights
        while(bitboard != 0L) {
            final int square = BitOps.lsb(bitboard);
            bitboard &= bitboard - 1;
            // piece square bonus
            eval += BONUS[Piece.KNIGHT][player][square][phase];
            // mobility
            final long knightAttacks = LEAP_ATTACKS_BITBOARD[square] & ~occupancy;
            eval += MOBILITY_KNIGHT[Long.bitCount(knightAttacks)][phase];
            // outpost
            final int knightFile = square & 7;
            final int knightRank = square >>> 3;
            if((PAWN_ATTACKS[1 ^ player][square] & pawnBitboard) != 0L) {
                if((PASSED_PAWNS_FILES[player][knightFile] & FORWARD_RANKS[player][knightRank] & otherPawnBitboard) != 0L) eval += KNIGHT_OUTPOST[phase];
            }
            // own king distance
            eval += KNIGHT_PROTECTOR[Math.abs(knightRank - kingRank) + Math.abs(knightFile - kingFile)][phase];
            // other king distance
            eval += KNIGHT_ENEMY_KING_DISTANCE[Math.abs(knightRank - otherKingRank) + Math.abs(knightFile - otherKingFile)][phase];
            // other king safety
            safety = QUEEN_AFFECTS_KING_SAFETY[Long.bitCount(knightAttacks & KING_RING[1 ^ player][otherKingSquare])];
        }
        return (eval << 8) | (safety & 0xff);
    }

    private static int pawnEval(long bitboard, int phase, int player, long knightBishopBitboard, long otherPawnBitboard, int materialValuePieces, int kingRank, int kingFile, int otherKingRank, int otherKingFile, int otherMaterialValuePieces, int thisPlayer, long kingBitboard) {
        // init eval and material value
        final int numPawns = Long.bitCount(bitboard);
        int eval = PIECE_VALUE[Piece.PAWN][numPawns][phase];
        long originalBitboard = bitboard;
        // loop over pawns
        while(bitboard != 0L) {
            final int square = BitOps.lsb(bitboard);
            bitboard &= bitboard - 1;
            // piece square bonus
            eval += BONUS[Piece.PAWN][player][square][phase];
            // doubled pawns
            final int pawnFile = square & 7;
            final long pawnFileBitboard = FILE_BITBOARD[pawnFile];
            if(Long.bitCount(bitboard & pawnFileBitboard) > 1) eval += DOUBLED_PAWN[phase];
            // weak pawn
            final int pawnRank = square >>> 3;
            final long adjacentFilesBitboard = (pawnFile > 0 ? FILE_BITBOARD[pawnFile - 1] : 0L) | (pawnFile < 7 ? FILE_BITBOARD[pawnFile + 1] : 0L);
            final long adjacentFilePawns = originalBitboard & adjacentFilesBitboard;
            if((adjacentFilePawns & FORWARD_RANKS[1 ^ player][player == 0 ? pawnRank + 1 : pawnRank - 1]) == 0L) eval += WEAK_PAWN[phase];
            // isolated pawn
            if(adjacentFilePawns == 0) eval += ISOLATED_PAWN[phase];
            // pawn protects
            if((PAWN_ATTACKS[player][square] & knightBishopBitboard) != 0L) eval += PAWN_PROTECTS[phase];
            // passed pawn
            final long forwardRanksBitboard = FORWARD_RANKS[player][pawnRank];
            final long otherPassedPawnBlockers = otherPawnBitboard & (pawnFileBitboard | adjacentFilesBitboard) & forwardRanksBitboard;
            if(otherPassedPawnBlockers == 0L) {
                // additional piece square bonus
                eval += PASSED_PAWN_RANK_BONUS[player][pawnRank];
                // phalanx
                eval += (originalBitboard & adjacentFilesBitboard & RANK_BITBOARD[pawnRank]) != 0L ? PASSED_PAWN_PHALANX[phase] : 0;
                // other king distance when low material
                if(materialValuePieces < QUEEN_VALUE) {
                    final int kingDist = 8 - Math.max(Math.abs(kingRank - pawnRank), Math.abs(kingFile - pawnFile));
                    final int otherKingDist = Math.max(Math.abs(otherKingRank - pawnRank), Math.abs(otherKingFile - pawnFile));
                    eval += (kingDist * kingDist + otherKingDist * otherKingDist) * (player == 0 ? pawnRank : 7 - pawnRank);
                }
                // other king stops pawn when other has no material
                if(otherMaterialValuePieces == 0 && (originalBitboard & FORWARD_RANKS[player][pawnRank] & FILE_BITBOARD[pawnFile]) != 0L) {
                    final int pawnPromoteDist = Math.abs((player == 0 ? 7 : 0) - pawnRank) + (pawnRank == (player == 0 ? 1 : 6) ? 1 : 0);
                    final int otherKingDistFromPromote = Math.max(Math.abs((player == 0 ? 7 : 0) - otherKingRank), Math.abs(pawnFile - otherKingFile));
                    final int pawnTurnToMove = player == thisPlayer ? 1 : 0;
                    final int kingTurnToMove = 1 ^ pawnTurnToMove;
                    final int ownKingInFront = (kingBitboard & FORWARD_RANKS[player][pawnRank] & FILE_BITBOARD[pawnFile]) != 0L ? 1 : 0;
                    final int pawnDist = pawnPromoteDist - pawnTurnToMove + ownKingInFront;
                    final int kingDist = otherKingDistFromPromote - kingTurnToMove;
                    if(kingDist > pawnDist) eval += BISHOP_VALUE[phase];
                }
            }
        }
        return eval;
    }
}

