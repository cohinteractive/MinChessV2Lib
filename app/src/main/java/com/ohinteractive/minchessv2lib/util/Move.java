package com.ohinteractive.minchessv2lib.util;

import com.ohinteractive.minchessv2lib.impl.Board;
import com.ohinteractive.minchessv2lib.impl.Gen;

public class Move {
    
    public static String string(long move) {
        int promotePiece = (int) move >>> Board.PROMOTE_PIECE_SHIFT & Board.PIECE_BITS;
        return Board.squareToString((int) move & Board.SQUARE_BITS) + Board.squareToString((int) move >>> Board.TARGET_SQUARE_SHIFT & Board.SQUARE_BITS)
                + (promotePiece == Value.NONE ? "" : Piece.SHORT_STRING[promotePiece].toUpperCase());
    }

    public static String notation(long[] board, long move) {
        final long board0 = board[0];
        final long board1 = board[1];
        final long board2 = board[2];
        final long board3 = board[3];
        int startSquare = (int) move & Board.SQUARE_BITS;
        int startFile = startSquare & Value.FILE;
        int startRank = startSquare >>> 3;
        int targetSquare = (int) move >>> Board.TARGET_SQUARE_SHIFT & Board.SQUARE_BITS;
        int targetFile = targetSquare & Value.FILE;
        int targetRank = targetSquare >>> 3;
        int startPiece = (int) move >>> Board.START_PIECE_SHIFT & Board.PIECE_BITS;
        int player = startPiece >>> 3;
        final long colorMask = ~(-(player & 1) ^ board3);
        long pieceBitboard = (-(startPiece & 1) & board0) & (-(startPiece >>> 1 & 1) & board1) & (-(startPiece >>> 2 & 1) & board2) & colorMask;
        int startType = startPiece & Piece.TYPE;
        int targetPiece = (int) move >>> Board.TARGET_PIECE_SHIFT & Board.PIECE_BITS;
        int promotePiece = (int) move >>> Board.PROMOTE_PIECE_SHIFT & Board.PIECE_BITS;
        long allOccupancy = board0 | board1 | board2;
        String notation = "";
        switch (startType) {
            case Piece.KING: {
                if (Math.abs(startSquare - targetSquare) == 2) {
                    return "O-O" + (targetFile == Value.FILE_G ? "" : "-O");
                }
                notation = "K";
                break;
            }
            case Piece.QUEEN: {
                notation = "Q";
                long queensAttackTargetSquare = Magic.queenMoves(targetSquare, allOccupancy) & pieceBitboard;
                if (queensAttackTargetSquare > 1L) {
                    int queensOnFile = Long.bitCount(queensAttackTargetSquare & Bitboard.BB[Bitboard.FILE][targetFile]);
                    int queensOnRank = Long.bitCount(queensAttackTargetSquare & Bitboard.BB[Bitboard.RANK][targetRank]);
                    int queensOnDiagonals = Long
                            .bitCount(queensAttackTargetSquare & (Bitboard.BB[Bitboard.DIAGONAL_ATTACKS][targetSquare]));
                    if (queensOnRank > 1) {
                        notation += Value.FILE_STRING.charAt(startFile);
                    }
                    if (queensOnFile > 1) {
                        notation += Integer.toString(startRank + 1);
                    }
                    if (notation.length() == 1 && queensOnDiagonals > 1) {
                        notation += Value.FILE_STRING.charAt(startFile);
                    }
                }
                break;
            }
            case Piece.ROOK: {
                notation = "R";
                long rooksAttackTargetSquare = Magic.rookMoves(targetSquare, allOccupancy) & pieceBitboard;
                if (rooksAttackTargetSquare > 1L) {
                    int rooksOnFile = Long.bitCount(rooksAttackTargetSquare & Bitboard.BB[Bitboard.FILE][targetFile]);
                    int rooksOnRank = Long.bitCount(rooksAttackTargetSquare & Bitboard.BB[Bitboard.RANK][targetRank]);
                    if (rooksOnRank > 1) {
                        notation += Value.FILE_STRING.charAt(startFile);
                    }
                    if (rooksOnFile > 1) {
                        notation += Integer.toString(startRank + 1);
                    }
                }
                break;
            }
            case Piece.BISHOP: {
                notation = "B";
                long bishopsAttackTargetSquare = Magic.bishopMoves(targetSquare, allOccupancy) & pieceBitboard;
                if (bishopsAttackTargetSquare > 1L) {
                    int bishopsOnFile = Long.bitCount(bishopsAttackTargetSquare & Bitboard.BB[Bitboard.FILE][targetFile]);
                    int bishopsOnRank = Long.bitCount(bishopsAttackTargetSquare & Bitboard.BB[Bitboard.RANK][targetRank]);
                    int bishopsOnDiagonals = Long
                            .bitCount(bishopsAttackTargetSquare & (Bitboard.BB[Bitboard.DIAGONAL_ATTACKS][targetSquare]));
                    if (bishopsOnRank > 1) {
                        notation += Value.FILE_STRING.charAt(startFile);
                    }
                    if (bishopsOnFile > 1) {
                        notation += Integer.toString(startRank + 1);
                    }
                    if (notation.length() == 1 && bishopsOnDiagonals > 1) {
                        notation += Value.FILE_STRING.charAt(startFile);
                    }
                }
                break;
            }
            case Piece.KNIGHT: {
                notation = "N";
                if (Long.bitCount(Bitboard.BB[Bitboard.LEAP_ATTACKS][targetSquare] & pieceBitboard) > 1) {
                    if (Long.bitCount(Bitboard.BB[Bitboard.RANK][startRank] & pieceBitboard) > 1) {
                        notation += Value.FILE_STRING.charAt(startFile);
                    }
                    if (Long.bitCount(Bitboard.BB[Bitboard.FILE][startFile] & pieceBitboard) > 1) {
                        notation += Integer.toString(startRank + 1);
                    }
                }
                break;
            }
            case Piece.PAWN:
            default: {
                notation = "";
                break;
            }
        }
        if (targetPiece != Value.NONE || targetSquare == Board.enPassantSquare(board)) {
            if (startType == Piece.PAWN) {
                notation += Value.FILE_STRING.charAt(startFile);
            }
            notation += "x";
        }
        notation += Board.squareToString(targetSquare);
        if (promotePiece != Value.NONE) {
            notation += "=";
            switch (promotePiece & Piece.TYPE) {
                case Piece.QUEEN:
                    notation += "Q";
                    break;
                case Piece.ROOK:
                    notation += "R";
                    break;
                case Piece.BISHOP:
                    notation += "B";
                    break;
                case Piece.KNIGHT:
                    notation += "N";
                    break;
            }
        }
        long[] tempBoard = Board.makeMove(board, move);
        if (Board.isPlayerInCheck(tempBoard, 1 ^ player)) {
            long[] moves = Gen.gen(tempBoard, true, false);
            if (moves[Gen.MOVELIST_SIZE] == 0) {
                notation += "#";
            } else {
                notation += "+";
            }
        }
        return notation;
    }

    public static String verbose(int move) {
        int startSquare = move & 0x3f;
        int targetSquare = (move >>> 6) & 0x3f;
        int promotePiece = (move >>> 12) & 0xf;
        int startPiece = (move >>> 16) & 0xf;
        int targetPiece = (move >>> 20) & 0xf;
        return Piece.SHORT_STRING[startPiece] + "[" + startSquare + "] " + Piece.SHORT_STRING[targetPiece] + "["
                + targetSquare + "] " + Piece.SHORT_STRING[promotePiece] + "[" + promotePiece + "]";
    }

    private static final String PIECE_STRING = " KQRBNPXXkqrbnp";

    public static int stringToInt(long[] board, String moveString) {
        if(moveString.length() < 4) return Value.INVALID;
        int startFile = Value.FILE_STRING.indexOf(moveString.charAt(0));
        if(startFile == Value.INVALID) return Value.INVALID;
        int startRank = Character.getNumericValue(moveString.charAt(1)) - 1;
        if(startRank == Value.INVALID) return Value.INVALID;
        int targetFile = Value.FILE_STRING.indexOf(moveString.charAt(2));
        if(targetFile == Value.INVALID) return Value.INVALID;
        int targetRank = Character.getNumericValue(moveString.charAt(3)) - 1;
        if(targetRank == Value.INVALID) return Value.INVALID;
        int startSquare = startRank << 3 | startFile;
        int targetSquare = targetRank << 3 | targetFile;
        int promotePiece = 0;
        if (moveString.length() > 4) {
            promotePiece = PIECE_STRING.indexOf(moveString.charAt(4));
            if(promotePiece == Value.INVALID) return Value.INVALID;
        }
        return startSquare | (targetSquare << 6) | (promotePiece << 12) | (Board.getSquare(board, startSquare) << 16)
                | (Board.getSquare(board, targetSquare) << 20);
    }

    public static long isValid(long[] moves, int startSquare, int targetSquare) {
        int index = Value.INVALID;
        if(startSquare == index || targetSquare == index) return -1;
        for (int moveIndex = 0; moveIndex < moves[Gen.MOVELIST_SIZE]; moveIndex ++) {
            int move = (int) moves[moveIndex];
            if ((move & 0xfff) == convertStartTarget(startSquare, targetSquare)) {
                return move;
            }
        }
        return Value.INVALID;
    }

    public static int convertStartTarget(int startSquare, int targetSquare) {
        return startSquare | (targetSquare << 6);
    }

    public static String moveListString(long[] moveList) {
        String string = "";
        for(int i = 0; i < moveList[99]; i ++) {
            long move = moveList[i];
            string += "Move " + (i + 1) + ": " + string(move) + " (" + ((int) (move >>> 32)) + ")\n";
        }
        return string;
    }

    private Move() {}

}
