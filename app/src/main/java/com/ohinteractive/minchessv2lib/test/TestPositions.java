package com.ohinteractive.minchessv2lib.test;

public final class TestPositions {

    public static final String[] ALL = {
        // MATERIAL
        "4k3/8/8/8/8/8/4Q3/4K3 w - - 0 1",  // White has queen
        "4k3/8/8/8/8/8/4r3/4K3 b - - 0 1",  // Black has rook

        // MOBILITY
        "4k3/8/8/3Q4/8/8/8/4K3 w - - 0 1",  // Queen mobility
        "4k3/8/3n4/8/3N4/8/8/4K3 w - - 0 1", // Knight mobility

        // KING SAFETY
        "4k3/8/8/8/8/8/4PPP1/4K2R w - - 0 1", // King with pawn shield
        "4k3/8/8/8/8/8/8/4K3 w - - 0 1",      // Lone exposed king

        // PASSED PAWNS
        "4k3/8/8/8/8/8/3P4/4K3 w - - 0 1",  // Clear passed pawn
        "4k3/8/8/8/3p4/8/3P4/4K3 w - - 0 1", // Blocked passed pawn

        // PAWN STRUCTURE (Doubled, Isolated)
        "4k3/8/8/8/8/8/3P1P2/4K3 w - - 0 1", // Doubled pawns
        "4k3/8/8/8/8/8/3P4/4K3 w - - 0 1",  // Isolated pawn

        // ROOK FILE USAGE
        "4k3/8/8/8/8/8/8/3RK3 w - - 0 1",   // Rook on open file
        "4k3/8/8/8/8/8/8/3RKP2 w - - 0 1",  // Rook blocked

        // BISHOPS
        "4k3/8/8/8/8/8/3B1B2/4K3 w - - 0 1", // Bishop pair
        "4k3/8/8/3p4/8/8/3B4/4K3 w - - 0 1", // Bad bishop

        // KNIGHTS
        "4k3/8/8/3N4/8/2p5/8/4K3 w - - 0 1", // Knight outpost
        "4k3/8/8/3N4/8/8/8/4K3 w - - 0 1",  // Knight no outpost

        // DRAW SCENARIOS
        "4k3/8/8/8/8/8/7B/4K3 w - - 99 50", // K+B vs K, 50-move rule
        "4k3/8/8/8/8/8/7N/4K3 w - - 0 1"    // K+N vs K (draw)
    };

    private TestPositions() {} // prevent instantiation
}
