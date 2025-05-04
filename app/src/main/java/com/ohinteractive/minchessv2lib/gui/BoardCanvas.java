package com.ohinteractive.minchessv2lib.gui;

import java.util.HashMap;
import java.util.Map;

import com.ohinteractive.minchessv2lib.gui.ImageCache.Piece;
import com.ohinteractive.minchessv2lib.util.BitOps;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class BoardCanvas extends Canvas {

    public static final int SQUARE_SIZE = 64;
    public static final int BOARD_SIZE = 8;

    public BoardCanvas() {
        setWidth(SQUARE_SIZE * BOARD_SIZE);
        setHeight(SQUARE_SIZE * BOARD_SIZE);
        drawEmptyBoard();
    }

    public void drawEmptyBoard() {
        GraphicsContext gc = getGraphicsContext2D();
        for (int rank = 0; rank < BOARD_SIZE; rank++) {
            for (int file = 0; file < BOARD_SIZE; file++) {
                boolean light = (rank + file) % 2 == 0;
                gc.setFill(light ? Color.BEIGE : Color.SADDLEBROWN);
                gc.fillRect(file * SQUARE_SIZE, rank * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
            }
        }
    }

    public void drawPosition(long[] board) {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        drawBoard(gc);
        drawPieces(gc, board);
        drawHightlights(gc);
    }

    public void setHighlight(int square, Color color) {
        highlights.put(square, color);
    }

    public void clearHighlights() {
        highlights.clear();
    }

    private static final int TILE_SIZE = 64;
    private static final int BOARD_WIDTH = 8;
    private static final int BOARD_HEIGHT = 8;

    private final Map<Integer, Color> highlights = new HashMap<>();

    private void drawBoard(GraphicsContext gc) {
        for(int y = 0; y < BOARD_HEIGHT; y ++) {
            for(int x = 0; x < BOARD_WIDTH; x ++) {
                boolean isLight = (x + y) % 2 == 0;
                gc.setFill(isLight ? Color.BEIGE : Color.BROWN);
                gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private void drawPieces(GraphicsContext gc, long[] board) {
        for(int i = 1; i < 7; i ++) {
            drawBitboard(gc, board[0], board[1], board[2], board[3], i, true);
            drawBitboard(gc, board[0], board[1], board[2], board[3], i, false);
        }
    }

    private void drawBitboard(GraphicsContext gc, long board0, long board1, long board2, long board3, int piece, boolean isWhite) {
        long bitboard = (-(piece & 1) & board0) & (-(piece >>> 1 & 1) & board1) & (-(piece >>> 2 & 1) & board2) & ~(-(piece >>> 3 & 1) ^ board3);
        while(bitboard != 0L) {
            int square = BitOps.LSB[(int) (((bitboard & -bitboard) * BitOps.DB) >>> 58)];
            bitboard &= bitboard - 1;
            int x = square & 7;
            int y = 7 - (square >>> 3);
            Piece p = Piece.valueOf((isWhite ? "W" : "B") + "PNBRQK".charAt(piece - 1));
            Image img = ImageCache.get(p, TILE_SIZE * 0.8);
            gc.drawImage(img, x * TILE_SIZE + TILE_SIZE * 0.1, y * TILE_SIZE + TILE_SIZE * 0.1);
        }
    }

    private void drawHightlights(GraphicsContext gc) {
        for(Map.Entry<Integer, Color> entry : highlights.entrySet()) {
            int square = entry.getKey();
            Color color = entry.getValue();
            int x = square & 7;
            int y = 7 - (square >>> 3);
            gc.setFill(color.deriveColor(0, 1, 1, 0.4));
            gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }
    }
}

