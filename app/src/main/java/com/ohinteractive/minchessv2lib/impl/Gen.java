package com.ohinteractive.minchessv2lib.impl;

import com.ohinteractive.minchessv2lib.util.BitOps;
import com.ohinteractive.minchessv2lib.util.Bitboard;
import com.ohinteractive.minchessv2lib.util.Magic;
import com.ohinteractive.minchessv2lib.util.Piece;
import com.ohinteractive.minchessv2lib.util.Value;

public class Gen {
    
    public static final int MAX_MOVELIST_SIZE = 100;
    public static final int MOVELIST_SIZE = MAX_MOVELIST_SIZE - 1;

    public static long[] gen(long[] board, boolean legal, boolean tactical) {
        final int player = (int) board[Board.STATUS] & Board.PLAYER_BIT;
        final int playerBit = player << Board.PLAYER_SHIFT;
        final long board0 = board[0];
        final long board1 = board[1];
        final long board2 = board[2];
        final long board3 = board[3];
        final int status = (int) board[Board.STATUS];
        final long allOccupancy = board0 | board1 | board2;
        final long otherOccupancy = allOccupancy & (-(player & 1) ^ board3);
        long[] moves = new long[MAX_MOVELIST_SIZE];
        int moveListLength = 0;
        moveListLength = getKingMoves(board0, board1, board2, board3, status, moves, Piece.KING | playerBit, moveListLength, player, allOccupancy, otherOccupancy, tactical);
        moveListLength = getQueenMoves(board0, board1, board2, board3, moves, Piece.QUEEN | playerBit, player, moveListLength, allOccupancy, otherOccupancy, tactical);
        moveListLength = getPawnMoves(board0, board1, board2, board3, status, moves, Piece.PAWN | playerBit, moveListLength, player, allOccupancy, otherOccupancy, tactical);
        moveListLength = getRookMoves(board0, board1, board2, board3, moves, Piece.ROOK | playerBit, player, moveListLength, allOccupancy, otherOccupancy, tactical);
        moveListLength = getBishopMoves(board0, board1, board2, board3, moves, Piece.BISHOP | playerBit, player, moveListLength, allOccupancy, otherOccupancy, tactical);
        moveListLength = getKnightMoves(board0, board1, board2, board3, player, moves, Piece.KNIGHT | playerBit, moveListLength, allOccupancy, otherOccupancy, tactical);
        moves[MOVELIST_SIZE] = moveListLength;
        return legal ? purgeIllegalMoves(board, moves, player) : moves;
    }

    private Gen() {}

    private static long[] purgeIllegalMoves(long[] board, long[] moves, int player) {
        long[] legalMoves = new long[MAX_MOVELIST_SIZE];
        int legalMoveCount = 0;
        for(int i = 0; i < moves[99]; i ++) {
            final long move = moves[i];
            final long[] boardAfterMove = Board.makeMove(board, move);
            if(!Board.isPlayerInCheck(boardAfterMove, player)) legalMoves[legalMoveCount ++] = move;
        }
        legalMoves[MOVELIST_SIZE] = legalMoveCount;
        return legalMoves;
    }

    private static int getKingMoves(long board0, long board1, long board2, long board3, int status, long[] moves, int piece, int moveListLength, int player, long allOccupancy, long otherOccupancy, boolean tactical) {
        final long bitboard = board0 & ~board1 & ~board2 & (-((1 ^ player) & 1) ^ board3);
        final int square = BitOps.lsb(bitboard);
        final long kingAttacks = KING_ATTACKS[square];
        long moveBitboard = kingAttacks & otherOccupancy;
        while(moveBitboard != 0L) {
            final int targetSquare = BitOps.lsb(moveBitboard);
            moveBitboard &= moveBitboard - 1;
            moves[moveListLength ++] = square | (targetSquare << Board.TARGET_SQUARE_SHIFT) | (piece << Board.START_PIECE_SHIFT) | (Board.getSquare(board0, board1, board2, board3, targetSquare) << Board.TARGET_PIECE_SHIFT);
        }
        if(tactical) return moveListLength;
        moveBitboard = kingAttacks & ~allOccupancy;
        while(moveBitboard!= 0L) {
            moves[moveListLength ++] = square | (BitOps.lsb(moveBitboard) << Board.TARGET_SQUARE_SHIFT) | (piece << Board.START_PIECE_SHIFT);
            moveBitboard &= moveBitboard - 1;
        }
        final int castling = status >>> Board.CASTLING_SHIFT & Board.CASTLING_BITS;
        final boolean kingSide = (castling & (player == Value.WHITE ? 0b1 : 0b100)) != Value.NONE;
        final boolean queenSide = (castling & (player == Value.WHITE ? 0b10 : 0b1000)) != Value.NONE;
        if(kingSide || queenSide) {
            final int other = 1 ^ player;
            if(!Board.isSquareAttackedByPlayer(board0, board1, board2, board3, square, other)) {
                if(kingSide) {
                    if((allOccupancy & (player == Value.WHITE ? 0x0000000000000060L : 0x6000000000000000L)) == 0L && !Board.isSquareAttackedByPlayer(board0, board1, board2, board3, square + 1, other))
                        moves[moveListLength ++] = square | ((square + 2) << Board.TARGET_SQUARE_SHIFT) | (piece << Board.START_PIECE_SHIFT);
                }
                if(queenSide) {
                    if((allOccupancy & (player == Value.WHITE ? 0x000000000000000eL : 0x0e00000000000000L)) == 0L && !Board.isSquareAttackedByPlayer(board0, board1, board2, board3, square - 1, other))
                        moves[moveListLength ++] = square | ((square - 2) << Board.TARGET_SQUARE_SHIFT) | (piece << Board.START_PIECE_SHIFT);
                }
            }
        }
        return moveListLength;
    }

    private static int getKnightMoves(long board0, long board1, long board2, long board3, int player, long[] moves, int piece, int moveListLength, long allOccupancy, long otherOccupancy, boolean tactical) {
        long knightBitboard = board0 & ~board1 & board2 & (-((1 ^ player) & 1) ^ board3);
        while(knightBitboard != 0L) {
            final int square = BitOps.lsb(knightBitboard);
            knightBitboard &= knightBitboard - 1;
            final long knightAttacks = LEAP_ATTACKS[square];
            long moveBitboard = knightAttacks & otherOccupancy;
            while(moveBitboard != 0L) {
                final int targetSquare = BitOps.lsb(moveBitboard);
                moveBitboard &= moveBitboard - 1;
                moves[moveListLength ++] = square | (targetSquare << Board.TARGET_SQUARE_SHIFT) | (piece << Board.START_PIECE_SHIFT) | (Board.getSquare(board0, board1, board2, board3, targetSquare) << Board.TARGET_PIECE_SHIFT);
            }
            if(tactical) continue;
            moveBitboard = knightAttacks & ~allOccupancy;
            while(moveBitboard != 0L) {
                moves[moveListLength ++] = square | (BitOps.lsb(moveBitboard) << Board.TARGET_SQUARE_SHIFT) | (piece << Board.START_PIECE_SHIFT);
                moveBitboard &= moveBitboard - 1;
            }
        }
        return moveListLength;
    }

    private static int getPawnMoves(long board0, long board1, long board2, long board3, int status, long[] moves, int piece, int moveListLength, int player, long allOccupancy, long otherOccupancy, boolean tactical) {
        long pawnBitboard = ~board0 & board1 & board2 & (-((1 ^ player) & 1) ^ board3);
        final int playerBit = player << Board.PLAYER_SHIFT;
        final int eSquare = status >>> Board.ESQUARE_SHIFT & Board.SQUARE_BITS;
        otherOccupancy |= (eSquare > 0 ? (1L << eSquare) : 0L);
        final long[] pawnAttacks = PAWN_ATTACKS[player];
        final long[] pawnAdvanceSingle = PAWN_ADVANCE_SINGLE[player];
        final long[] pawnAdvanceDouble = PAWN_ADVANCE_DOUBLE[player];
        while(pawnBitboard != 0L) {
            final int square = BitOps.lsb(pawnBitboard);
            pawnBitboard &= pawnBitboard - 1;
            long moveBitboard = pawnAttacks[square] & otherOccupancy;
            while(moveBitboard != 0L) {
                final int targetSquare = BitOps.lsb(moveBitboard);
                moveBitboard &= moveBitboard - 1;
                final int targetRank = targetSquare >>> 3;
                final int moveInfo = square | (targetSquare << Board.TARGET_SQUARE_SHIFT) | (piece << Board.START_PIECE_SHIFT) | (Board.getSquare(board0, board1, board2, board3, targetSquare) << Board.TARGET_PIECE_SHIFT);
                if(targetRank == (player == Value.WHITE ? 7 : 0)) {
                    moves[moveListLength++] = moveInfo | ((Piece.QUEEN | playerBit) << Board.PROMOTE_PIECE_SHIFT);
                    moves[moveListLength++] = moveInfo | ((Piece.ROOK | playerBit) << Board.PROMOTE_PIECE_SHIFT);
                    moves[moveListLength++] = moveInfo | ((Piece.BISHOP | playerBit) << Board.PROMOTE_PIECE_SHIFT);
                    moves[moveListLength++] = moveInfo | ((Piece.KNIGHT | playerBit) << Board.PROMOTE_PIECE_SHIFT);
                } else {
                    moves[moveListLength ++] = moveInfo;
                }
            }
            if(tactical) continue;
            moveBitboard = pawnAdvanceSingle[square] & ~allOccupancy;
            if(moveBitboard != 0L) {
                moveBitboard = (moveBitboard | pawnAdvanceDouble[square]) & ~allOccupancy;
            }
            while(moveBitboard != 0L) {
                final int targetSquare = BitOps.lsb(moveBitboard);
                moveBitboard &= moveBitboard - 1;
                final int targetRank = targetSquare >>> 3;
                final int moveInfo = square | (targetSquare << Board.TARGET_SQUARE_SHIFT) | (piece << Board.START_PIECE_SHIFT);
                if(targetRank == (player == Value.WHITE ? 7 : 0)) {
                    moves[moveListLength++] = moveInfo | ((Piece.QUEEN | playerBit) << Board.PROMOTE_PIECE_SHIFT);
                    moves[moveListLength++] = moveInfo | ((Piece.BISHOP | playerBit) << Board.PROMOTE_PIECE_SHIFT);
                    moves[moveListLength++] = moveInfo | ((Piece.KNIGHT | playerBit) << Board.PROMOTE_PIECE_SHIFT);
                    moves[moveListLength++] = moveInfo | ((Piece.ROOK | playerBit) << Board.PROMOTE_PIECE_SHIFT);
                } else {
                    moves[moveListLength ++] = moveInfo;
                }
            }
        }
        return moveListLength;
    }

    private static int getQueenMoves(long board0, long board1, long board2,long board3, long[] moves, int piece, int player, int moveListLength, long allOccupancy, long otherOccupancy, boolean tactical) {
        long queenBitboard = ~board0 & board1 & ~board2 & (-((1 ^ player) & 1) ^ board3);
        while(queenBitboard != 0L) {
            final int square = BitOps.lsb(queenBitboard);
            queenBitboard &= queenBitboard - 1;
            final long magic = Magic.queenMoves(square, allOccupancy);
            long moveBitboard = magic & otherOccupancy;
            while(moveBitboard != 0L) {
                final int targetSquare = BitOps.lsb(moveBitboard);
                moveBitboard &= moveBitboard - 1;
                moves[moveListLength ++] = square | (targetSquare << Board.TARGET_SQUARE_SHIFT) | (piece << Board.START_PIECE_SHIFT) | (Board.getSquare(board0, board1, board2, board3, targetSquare) << Board.TARGET_PIECE_SHIFT);
            }
            if(tactical) continue;
            moveBitboard = magic & ~allOccupancy;
            while(moveBitboard != 0L) {
                moves[moveListLength ++] = square | (BitOps.lsb(moveBitboard) << Board.TARGET_SQUARE_SHIFT) | (piece << Board.START_PIECE_SHIFT);
                moveBitboard &= moveBitboard - 1;
            }
        }
        return moveListLength;
    }

    private static int getRookMoves(long board0, long board1, long board2, long board3, long[] moves, int piece, int player, int moveListLength, long allOccupancy, long otherOccupancy, boolean tactical) {
        long rookBitboard = board0 & board1 & ~board2 & (-((1 ^ player) & 1) ^ board3);
        while(rookBitboard != 0L) {
            final int square = BitOps.lsb(rookBitboard);
            rookBitboard &= rookBitboard - 1;
            final long magic = Magic.rookMoves(square, allOccupancy);
            long moveBitboard = magic & otherOccupancy;
            while(moveBitboard != 0L) {
                final int targetSquare = BitOps.lsb(moveBitboard);
                moveBitboard &= moveBitboard - 1;
                moves[moveListLength ++] = square | (targetSquare << Board.TARGET_SQUARE_SHIFT) | (piece << Board.START_PIECE_SHIFT) | (Board.getSquare(board0, board1, board2, board3, targetSquare) << Board.TARGET_PIECE_SHIFT);
            }
            if(tactical) continue;
            moveBitboard = magic & ~allOccupancy;
            while(moveBitboard != 0L) {
                moves[moveListLength ++] = square | (BitOps.lsb(moveBitboard) << Board.TARGET_SQUARE_SHIFT) | (piece << Board.START_PIECE_SHIFT);
                moveBitboard &= moveBitboard - 1;
            }
        }
        return moveListLength;
    }

    private static int getBishopMoves(long board0, long board1, long board2, long board3, long[] moves, int piece, int player, int moveListLength, long allOccupancy, long otherOccupancy, boolean tactical) {
        long bishopBitboard = ~board0 & ~board1 & board2 & (-((1 ^ player) & 1) ^ board3);
        while(bishopBitboard != 0L) {
            final int square = BitOps.lsb(bishopBitboard);
            bishopBitboard &= bishopBitboard - 1;
            final long magic = Magic.bishopMoves(square, allOccupancy);
            long moveBitboard = magic & otherOccupancy;
            while(moveBitboard != 0L) {
                final int targetSquare = BitOps.lsb(moveBitboard);
                moveBitboard &= moveBitboard - 1;
                moves[moveListLength ++] = square | (targetSquare << Board.TARGET_SQUARE_SHIFT) | (piece << Board.START_PIECE_SHIFT) | (Board.getSquare(board0, board1, board2, board3, targetSquare) << Board.TARGET_PIECE_SHIFT);
            }
            if(tactical) continue;
            moveBitboard = magic & ~allOccupancy;
            while(moveBitboard != 0L) {
                moves[moveListLength ++] = square | (BitOps.lsb(moveBitboard) << Board.TARGET_SQUARE_SHIFT) | (piece << Board.START_PIECE_SHIFT);
                moveBitboard &= moveBitboard - 1;
            }
        }
        return moveListLength;
    }

    private static final long[] KING_ATTACKS = new long[64];
    private static final long[] LEAP_ATTACKS = new long[64];
    private static final long[][] PAWN_ATTACKS = new long[2][64];
    private static final long[][] PAWN_ADVANCE_SINGLE = new long[2][64];
    private static final long[][] PAWN_ADVANCE_DOUBLE = new long[2][64];
    static {
        for(int square = 0; square < 64; square ++) {
            KING_ATTACKS[square] = Bitboard.BB[Bitboard.KING_ATTACKS][square];
            LEAP_ATTACKS[square] = Bitboard.BB[Bitboard.LEAP_ATTACKS][square];
            for(int player = 0; player < 2; player ++) {
                PAWN_ATTACKS[player][square] = Bitboard.BB[Bitboard.PAWN_ATTACKS_PLAYER0 + player][square];
                PAWN_ADVANCE_SINGLE[player][square] = Bitboard.BB[Bitboard.PAWN_ADVANCE_1_PLAYER0 + player][square];
                PAWN_ADVANCE_DOUBLE[player][square] = Bitboard.BB[Bitboard.PAWN_ADVANCE_2_PLAYER0 + player][square];
            }
        }
    }

}
