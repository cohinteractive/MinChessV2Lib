package com.ohinteractive.minchessv2lib.impl;

import com.ohinteractive.minchessv2lib.util.BitOps;
import com.ohinteractive.minchessv2lib.util.Bitboard;
import com.ohinteractive.minchessv2lib.util.Fen;
import com.ohinteractive.minchessv2lib.util.Magic;
import com.ohinteractive.minchessv2lib.util.Piece;
import com.ohinteractive.minchessv2lib.util.Value;
import com.ohinteractive.minchessv2lib.util.Zobrist;

public class Board {

    public static final int STATUS = 4;
    public static final int MAX_BITBOARDS = 6;
    public static final int KEY = MAX_BITBOARDS - 1;
    public static final int PLAYER_BIT = 1;

    public static int player(long[] board) {
        return (int) board[STATUS] & PLAYER_BIT;
    }

    public static final int WHITE_KINGSIDE_BIT = 0b1;
    public static final int BLACK_KINGSIDE_BIT = 0b100;
    public static final int WHITE_KINGSIDE_BIT_UNSHIFTED = 0b10;
    public static final int BLACK_KINGSIDE_BIT_UNSHIFTED = 0b1000;

    public static boolean kingSide(long[] board, int player) {
        return (board[STATUS] & (player == Value.WHITE ? WHITE_KINGSIDE_BIT_UNSHIFTED : BLACK_KINGSIDE_BIT_UNSHIFTED)) != 0L;
    }

    public static final int WHITE_QUEENSIDE_BIT = 0b10;
    public static final int BLACK_QUEENSIDE_BIT = 0b1000;
    public static final int WHITE_QUEENSIDE_BIT_UNSHIFTED = 0b100;
    public static final int BLACK_QUEENSIDE_BIT_UNSHIFTED = 0b10000;

    public static boolean queenSide(long[] board, int player) {
        return (board[STATUS] & (player == Value.WHITE ? WHITE_QUEENSIDE_BIT_UNSHIFTED : BLACK_QUEENSIDE_BIT_UNSHIFTED)) != 0L;
    }

    public static final long WHITE_ENPASSANT_SQUARES = 0x0000ff0000000000L;
    public static final long BLACK_ENPASSANT_SQUARES = 0x0000000000ff0000L;

    public static boolean isValidEnPassantSquareForPlayer(int square, int player) {
        return ((1L << square) & (player == Value.WHITE ? WHITE_ENPASSANT_SQUARES : BLACK_ENPASSANT_SQUARES)) != 0L;
    }

    public static final int ESQUARE_SHIFT = 5;
    public static final int SQUARE_BITS = 0b111111;
    
    public static boolean hasValidEnPassantSquare(long[] board) {
        return ((1L << ((int) board[STATUS] >>> ESQUARE_SHIFT & SQUARE_BITS)) & (((int) board[STATUS] & PLAYER_BIT) == 0 ? WHITE_ENPASSANT_SQUARES : BLACK_ENPASSANT_SQUARES)) != 0L;
    }

    public static int enPassantSquare(long[] board) {
        int eSquare = (int) board[STATUS] >>> ESQUARE_SHIFT & SQUARE_BITS;
        return ((1L << eSquare) & (((int) board[STATUS] & PLAYER_BIT) == 0 ? WHITE_ENPASSANT_SQUARES : BLACK_ENPASSANT_SQUARES)) != 0L ? eSquare : Value.INVALID;
    }

    public static final int HALF_MOVE_CLOCK_SHIFT = 11;
    public static final int HALF_MOVE_CLOCK_BITS = 0b1111111;

    public static int halfMoveClock(long[] board) {
        return (int) board[STATUS] >>> HALF_MOVE_CLOCK_SHIFT & HALF_MOVE_CLOCK_BITS;
    }

    public static final int FULL_MOVE_NUMBER_SHIFT = 18;
    public static final int FULL_MOVE_NUMBER_BITS = 0b1111111111;

    public static int fullMoveNumber(long[] board) {
        return (int) board[STATUS] >>> FULL_MOVE_NUMBER_SHIFT & FULL_MOVE_NUMBER_BITS;
    }

    public static long key(long[] board) {
        return board[KEY];
    }

    public static final int IN_CHECK_SHIFT = 28;
    public static final int IN_CHECK_BIT = 0b1;
    public static final int IN_CHECK_BIT_UNSHIFTED = 0b10000000000000000000000000000;
    public static final int HAS_CHECKED_SHIFT = 29;
    public static final int HAS_CHECKED_BIT = 0b1;
    public static final int HAS_CHECKED_BIT_UNSHIFTED = 0b100000000000000000000000000000;

    public static final String FEN_STARTING_POSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    public static long[] startingPosition() {
        return fromFen(FEN_STARTING_POSITION);
    }

    public static final int CASTLING_SHIFT = 1;
    public static final int CASTLING_BITS = 0b1111;
    public static final int OCCUPANCY_BIT = 0b1000;
    public static final int SQUARE_A1 = 0;
    public static final int SQUARE_A8 = 56;
    public static final int SQUARE_H1 = 7;
    public static final int SQUARE_H8 = 63;

    public static long[] fromFen(String fen) {
        long[] board = new long[MAX_BITBOARDS];
        int[] pieces = Fen.getPieces(fen);
        long board0 = 0;
        long board1 = 0;
        long board2 = 0;
        long board3 = 0;
        for(int square = SQUARE_A1; square <= SQUARE_H8; square ++) {
            int piece = pieces[square];
            if(piece != Value.NONE) {
                long squareBit = 1L << square;
                board0 |= -(piece & 1) & squareBit;
                board1 |= -(piece >>> 1 & 1) & squareBit;
                board2 |= -(piece >>> 2 & 1) & squareBit;
                board3 |= -(piece >>> 3 & 1) & squareBit;
            }
        }
        board[0] = board0;
        board[1] = board1;
        board[2] = board2;
        board[3] = board3;
        boolean whiteToMove = Fen.getWhiteToMove(fen);
        long status = whiteToMove ? Value.WHITE : Value.BLACK;
        int castling = Fen.getCastling(fen);
        status ^= castling << CASTLING_SHIFT;
        int eSquare = Fen.getEnPassantSquare(fen);
        if((whiteToMove && (eSquare > 39 && eSquare < 48)) || (!whiteToMove && (eSquare > 15 && eSquare < 24))) {
            status ^= eSquare << ESQUARE_SHIFT;
        } else {
            eSquare = Value.INVALID;
        }
        status ^= Fen.getHalfMoveClock(fen) << HALF_MOVE_CLOCK_SHIFT;
        board[STATUS] = status ^ Fen.getFullMoveNumber(fen) << FULL_MOVE_NUMBER_SHIFT;
        board[KEY] = Zobrist.getKey(pieces, whiteToMove, (castling & WHITE_KINGSIDE_BIT) != 0,
            (castling & WHITE_QUEENSIDE_BIT) != 0, (castling & BLACK_KINGSIDE_BIT) != 0,
            (castling & BLACK_QUEENSIDE_BIT) != 0, eSquare);
        return board;
    }

    public static final int PLAYER_SHIFT = 3;
    public static final int START_PIECE_SHIFT = 16;
    public static final int PIECE_BITS = 0b1111;
    public static final int TARGET_SQUARE_SHIFT = 6;
    public static final int TARGET_PIECE_SHIFT = 20;
    public static final int PROMOTE_PIECE_SHIFT = 12;
    public static final int WHITE_CASTLING_BITS = WHITE_KINGSIDE_BIT | WHITE_QUEENSIDE_BIT;
    public static final int BLACK_CASTLING_BITS = BLACK_KINGSIDE_BIT | BLACK_QUEENSIDE_BIT;

    public static long[] makeMove(long[] board, long move) {
        long[] newBoard = new long[board.length];
        System.arraycopy(board, 0, newBoard, 0, board.length);
        int status = (int) newBoard[STATUS];
        int castling = status >>> CASTLING_SHIFT & CASTLING_BITS;
        int eSquare = enPassantSquare(newBoard);
        final int originalESquare = eSquare;
        int halfMoveClock = status >>> HALF_MOVE_CLOCK_SHIFT & HALF_MOVE_CLOCK_BITS;
        int fullMoveNumber = status >>> FULL_MOVE_NUMBER_SHIFT & FULL_MOVE_NUMBER_BITS;
        long key = board[KEY];
        final int startSquare = (int) move & SQUARE_BITS;
        final int startPiece = (int) move >>> START_PIECE_SHIFT & PIECE_BITS;
        final int startPieceType = startPiece & Piece.TYPE;
        final int targetSquare = (int) move >>> TARGET_SQUARE_SHIFT & SQUARE_BITS;
        final int targetPiece = (int) move >>> TARGET_PIECE_SHIFT & PIECE_BITS;
        final long targetSquareBit = 1L << targetSquare;
        final int player = status & PLAYER_BIT;
        long board0 = newBoard[0];
        long board1 = newBoard[1];
        long board2 = newBoard[2];
        long board3 = newBoard[3];
        if(eSquare != Value.INVALID) {
            key ^= Zobrist.ENPASSANT_FILE[eSquare & Value.FILE];
            eSquare = Value.INVALID;
        }
        if(targetPiece != Value.NONE) {
            halfMoveClock = 0;
            final int other = 1 ^ player;
            board0 ^= -(targetPiece & 1) & targetSquareBit;
            board1 ^= -(targetPiece >>> 1 & 1) & targetSquareBit;
            board2 ^= -(targetPiece >>> 2 & 1) & targetSquareBit;
            board3 ^= -(other & 1) & targetSquareBit;
            key ^= Zobrist.PIECE[targetPiece & Piece.TYPE][other][targetSquare];
        }
        switch(startPieceType) {
            case Piece.QUEEN:
            case Piece.BISHOP:
            case Piece.KNIGHT: {
                final long pieceMoveBits = (1L << startSquare) | targetSquareBit;
                board0 ^= -(startPiece & 1) & pieceMoveBits;
                board1 ^= -(startPiece >>> 1 & 1) & pieceMoveBits;
                board2 ^= -(startPiece >>> 2 & 1) & pieceMoveBits;
                board3 ^= -(player & 1) & pieceMoveBits;
                key ^= Zobrist.PIECE[startPieceType][player][startSquare]
                    ^  Zobrist.PIECE[startPieceType][player][targetSquare];
                break;
            }
            case Piece.KING: {
                final long pieceMoveBits = (1L << startSquare) | targetSquareBit;
                board0 ^= pieceMoveBits;
                board3 ^= -(player & 1) & pieceMoveBits;
                key ^= Zobrist.PIECE[Piece.KING][player][startSquare]
                    ^  Zobrist.PIECE[Piece.KING][player][targetSquare];
                final boolean playerKingSideCastling  = (castling & (player == Value.WHITE ? WHITE_KINGSIDE_BIT : BLACK_KINGSIDE_BIT))  != 0;
                final boolean playerQueenSideCastling = (castling & (player == Value.WHITE ? WHITE_QUEENSIDE_BIT : BLACK_QUEENSIDE_BIT)) != 0;
                if(playerKingSideCastling || playerQueenSideCastling) {
                    key ^= (playerKingSideCastling  ? Zobrist.KING_SIDE[player]  : 0)
                        ^  (playerQueenSideCastling ? Zobrist.QUEEN_SIDE[player] : 0);
                    castling &= ~(player == Value.WHITE ? WHITE_CASTLING_BITS : BLACK_CASTLING_BITS);
                }
                if(Math.abs(startSquare - targetSquare) == 2) {
                    final long rookMoveBits;
                    if((targetSquare & Value.FILE) == Value.FILE_G) {
                        rookMoveBits = (1L << (targetSquare + 1)) | (1L << (targetSquare - 1));
                        key ^= Zobrist.PIECE[Piece.ROOK][player][targetSquare + 1]
                            ^  Zobrist.PIECE[Piece.ROOK][player][targetSquare - 1];
                    } else {
                        rookMoveBits = (1L << (targetSquare - 2)) | (1L << (targetSquare + 1));
                        key ^= Zobrist.PIECE[Piece.ROOK][player][targetSquare - 2]
                            ^  Zobrist.PIECE[Piece.ROOK][player][targetSquare + 1];
                    }
                    board0 ^= rookMoveBits;
                    board1 ^= rookMoveBits;
                    board3 ^= -(player & 1) & rookMoveBits;
                }
                break;
            }
            case Piece.ROOK: {
                final long pieceMoveBits = (1L << startSquare) | targetSquareBit;
                board0 ^= pieceMoveBits;
                board1 ^= pieceMoveBits;
                board3 ^= -(player & 1) & pieceMoveBits;
                key ^= Zobrist.PIECE[Piece.ROOK][player][startSquare]
                    ^  Zobrist.PIECE[Piece.ROOK][player][targetSquare];
                if((castling & Value.KINGSIDE_BIT[player]) != Value.NONE) {
                    if(startSquare == (player == Value.WHITE ? SQUARE_H1 : SQUARE_H8)) {
                        castling ^= Value.KINGSIDE_BIT[player];
                        key ^= Zobrist.KING_SIDE[player];
                    }
                }
                if((castling & Value.QUEENSIDE_BIT[player]) != Value.NONE) {
                    if(startSquare == (player == Value.WHITE ? SQUARE_A1 : SQUARE_A8)) {
                        castling ^= Value.QUEENSIDE_BIT[player];
                        key ^= Zobrist.QUEEN_SIDE[player];
                    }
                }
                break;
            }
            case Piece.PAWN: {
                final int promotePiece = (int) move >>> PROMOTE_PIECE_SHIFT & PIECE_BITS;
                halfMoveClock = 0;
                if(promotePiece == Value.NONE) {
                    final long pieceMoveBits = (1L << startSquare) | targetSquareBit;
                    board1 ^= pieceMoveBits;
                    board2 ^= pieceMoveBits;
                    board3 ^= -(player & 1) & pieceMoveBits;
                    key ^= Zobrist.PIECE[startPieceType][player][startSquare]
                        ^  Zobrist.PIECE[startPieceType][player][targetSquare];
                } else {
                    final long startSquareBit = 1L << startSquare;
                    board0 ^= -(promotePiece & 1) & targetSquareBit;
                    board1 ^= startSquareBit ^ (-(promotePiece >>> 1 & 1) & targetSquareBit);
                    board2 ^= startSquareBit ^ (-(promotePiece >>> 2 & 1) & targetSquareBit);
                    board3 ^= -(player & 1) & (startSquareBit | targetSquareBit);
                    key ^= Zobrist.PIECE[startPieceType][player][startSquare]
                        ^  Zobrist.PIECE[promotePiece & Piece.TYPE][player][targetSquare];
                }
                if(targetSquare == originalESquare) {
                    final int other = 1 ^ player;
                    final int captureSquare = targetSquare + (player == Value.WHITE ? -8 : 8);
                    final long captureSquareBit = 1L << captureSquare;
                    board1 ^= captureSquareBit;
                    board2 ^= captureSquareBit;
                    board3 ^= -(other & 1) & captureSquareBit;
                    key ^= Zobrist.PIECE[Piece.PAWN][other][captureSquare];
                }
                if(Math.abs(startSquare - targetSquare) == 16) {
                    eSquare = startSquare + (player == Value.WHITE ? 8 : -8);
                    key ^= Zobrist.ENPASSANT_FILE[eSquare & Value.FILE];
                }
                break;
            }
            default: break;
        }
        if((targetPiece & Piece.TYPE) == Piece.ROOK) {
            int other = 1 ^ player;
            if((castling & (other == Value.WHITE ? WHITE_KINGSIDE_BIT : BLACK_KINGSIDE_BIT)) != Value.NONE) {
                if(targetSquare == (other == Value.WHITE ? SQUARE_H1 : SQUARE_H8)) {
                    castling ^= (other == Value.WHITE ? WHITE_KINGSIDE_BIT : BLACK_KINGSIDE_BIT);
                    key ^= Zobrist.KING_SIDE[other];
                }
            }
            if((castling & (other == Value.WHITE ? WHITE_QUEENSIDE_BIT : BLACK_QUEENSIDE_BIT)) != Value.NONE) {
                if(targetSquare == (other == Value.WHITE ? SQUARE_A1 : SQUARE_A8)) {
                    castling ^= (other == Value.WHITE ? WHITE_QUEENSIDE_BIT : BLACK_QUEENSIDE_BIT);
                    key ^= Zobrist.QUEEN_SIDE[other];
                }
            }
        }
        newBoard[0] = board0;
        newBoard[1] = board1;
        newBoard[2] = board2;
        newBoard[3] = board3;
        newBoard[STATUS] =
            (1 ^ player) |
            (castling << CASTLING_SHIFT) |
            (eSquare != Value.INVALID ? (eSquare << ESQUARE_SHIFT) : 0) |
            (halfMoveClock << HALF_MOVE_CLOCK_SHIFT) |
            ((fullMoveNumber + player) << FULL_MOVE_NUMBER_SHIFT);
        newBoard[KEY] = key ^ Zobrist.WHITEMOVE;
        return newBoard;
    }

    public static final long ENPASSANT_RESET_BITS = ~(SQUARE_BITS << ESQUARE_SHIFT);

    public static long[] nullMove(long[] board) {
        long[] newBoard = new long[MAX_BITBOARDS];
        System.arraycopy(board, 0, newBoard, 0, MAX_BITBOARDS);
        long key = board[KEY];
        int eSquare = (int) newBoard[STATUS] >>> ESQUARE_SHIFT & SQUARE_BITS;
        if(eSquare > 0) {
            key ^= Zobrist.ENPASSANT_FILE[eSquare & 7];
        }
        key ^= Zobrist.WHITEMOVE;
        newBoard[STATUS] = (board[STATUS] ^ PLAYER_BIT) & ENPASSANT_RESET_BITS;
        newBoard[KEY] = key;
        return newBoard;
    }

    public static int getSquare(long[] board, int square) {
        return (int) (((board[3] >>> square & 1) << 3) |
                      ((board[2] >>> square & 1) << 2) |
                      ((board[1] >>> square & 1) << 1) |
                      (board[0] >>> square & 1));
    }

    public static int getSquare(long board0, long board1, long board2, long board3, int square) {
        return (int) (((board3 >>> square & 1) << 3) |
                      ((board2 >>> square & 1) << 2) |
                      ((board1 >>> square & 1) << 1) |
                      (board0 >>> square & 1));
    }

    public static boolean isSquareAttackedByPlayer(long[] board, int square, int player) {
        final long board0 = board[0];
        final long board1 = board[1];
        final long board2 = board[2];
        final long board3 = board[3];
        if((LEAP_ATTACKS[square] & (board0 & ~board1 & board2 & (-(player & 1) & board3))) != 0L) return true;
        if((PAWN_ATTACKS[1 ^ player][square] & (~board0 & board1 & board2 & (-(player & 1) & board3))) != 0L) return true;
        if((KING_ATTACKS[square] & (board0 & ~board1 & ~board2 & (-(player & 1) & board3))) != 0L) return true;
        final long allOccupancy = board0 | board1| board2;
        if((Magic.bishopMoves(square, allOccupancy) & (~board0 & ~board1 & board2 & (-(player & 1) & board3))
        | (~board0 & board1 & ~board2 & (-(player & 1) & board3))) != 0L) return true;
        if((Magic.rookMoves(square, allOccupancy) & (board0 & board1 & ~board2 & (-(player & 1) & board3))
        | (~board0 & board1 & ~board2 & (-(player & 1) & board3))) != 0L) return true;
        return false;
    }

    public static boolean isSquareAttackedByPlayer(long board0, long board1, long board2, long board3, int square, int player) {
        if((LEAP_ATTACKS[square] & (board0 & ~board1 & board2 & (-(player & 1) & board3))) != 0L) return true;
        if((PAWN_ATTACKS[1 ^ player][square] & (~board0 & board1 & board2 & (-(player & 1) & board3))) != 0L) return true;
        if((KING_ATTACKS[square] & (board0 & ~board1 & ~board2 & (-(player & 1) & board3))) != 0L) return true;
        final long allOccupancy = board0 | board1| board2;
        if((Magic.bishopMoves(square, allOccupancy) & (~board0 & ~board1 & board2 & (-(player & 1) & board3))
        | (~board0 & board1 & ~board2 & (-(player & 1) & board3))) != 0L) return true;
        if((Magic.rookMoves(square, allOccupancy) & (board0 & board1 & ~board2 & (-(player & 1) & board3))
        | (~board0 & board1 & ~board2 & (-(player & 1) & board3))) != 0L) return true;
        return false;
    }

    public static boolean isPlayerInCheck(long[] board, int player) {
        final long board0 = board[0];
        final long board1 = board[1];
        final long board2 = board[2];
        final long board3 = board[3];
        final int square = BitOps.lsb(board0 & ~board1 & ~board2 & (-(player & 1) & board3));
        if((LEAP_ATTACKS[square] & (board0 & ~board1 & board2 & (-(player & 1) & board3))) != 0L) return true;
        if((PAWN_ATTACKS[1 ^ player][square] & (~board0 & board1 & board2 & (-(player & 1) & board3))) != 0L) return true;
        if((KING_ATTACKS[square] & (board0 & ~board1 & ~board2 & (-(player & 1) & board3))) != 0L) return true;
        final long allOccupancy = board0 | board1| board2;
        if((Magic.bishopMoves(square, allOccupancy) & (~board0 & ~board1 & board2 & (-(player & 1) & board3))
        | (~board0 & board1 & ~board2 & (-(player & 1) & board3))) != 0L) return true;
        if((Magic.rookMoves(square, allOccupancy) & (board0 & board1 & ~board2 & (-(player & 1) & board3))
        | (~board0 & board1 & ~board2 & (-(player & 1) & board3))) != 0L) return true;
        return false;
    }

    public static void drawText(long[] board) {
        System.out.println(boardString(board));
    }

    public static String boardString(long[] board) {
        StringBuilder boardString = new StringBuilder();
        for(int i = SQUARE_A1; i <= SQUARE_H8; i ++) {
            int square = i ^ 0x38;
            int piece = getSquare(board, square);
            boardString.append((piece != Value.NONE ? Piece.SHORT_STRING[piece] : ".")).append((i & 7) == 7 ? "\n" : " ");
        }
        return boardString.toString();
    }

    private static final long[] LEAP_ATTACKS = new long[64];
    private static final long[] KING_ATTACKS = new long[64];
    static {
        for(int square = 0; square < 64; square ++) {
            LEAP_ATTACKS[square] = Bitboard.BB[Bitboard.LEAP_ATTACKS][square];
            KING_ATTACKS[square] = Bitboard.BB[Bitboard.KING_ATTACKS][square];
        }
    }
    private static final long[][] PAWN_ATTACKS = new long[2][64];
    static {
        for(int player = 0; player < 2; player ++) {
            for(int square = 0; square < 64; square ++) {
                PAWN_ATTACKS[player][square] = Bitboard.BB[Bitboard.PAWN_ATTACKS_PLAYER0 + player][square];
            }
        }
    }

    private Board() {}

    /*
     * How to use the 4 bitboard board:
     * To get a bitboard for a general piece:
     * long bitboard = (-(piece & 1) & board0) | (-(piece >>> 1 & 1) & board1) | (-(piece >>> 2 & 1) & board2) | (-(piece >>> 3 & 1) & board3);
     * To get a bitboard for a specific piece, knowing type and color, we know which boards to touch, e.g. white king:
     * // white king is on board0 only, we need to remove all other board bits
     * long bitboard = board0 & ~board1 & ~board2 & ~board3;
     * To get a bitboard for a specific piece type, not knowing color, we know which piece boards to touch, e.g. king, general color:
     * // king is on board0, we need to remove all other board bits except board3, where we determine if it is there or not
     * long bitboard = board0 & ~board1 & ~board2 & (-(color & 1) & board3);
     * To
     */
    
}
