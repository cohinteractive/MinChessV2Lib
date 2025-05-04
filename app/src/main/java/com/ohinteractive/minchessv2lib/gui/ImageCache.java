package com.ohinteractive.minchessv2lib.gui;

import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ImageCache {

    public enum Piece {
        WP, WN, WB, WR, WQ, WK,
        BP, BN, BB, BR, BQ, BK
    }

    public static Image get(Piece piece, double size) {
        return cache.computeIfAbsent(piece, p -> {
            String path = "/pieces/" + p.name().toLowerCase() + ".png";
            InputStream is = ImageCache.class.getResourceAsStream(path);
            if(is == null) throw new RuntimeException("Missing image: " + path);
            return new Image(is, size, size, true, true);
        });
    }

    private static final Map<Piece, Image> cache = new HashMap<>();

}

