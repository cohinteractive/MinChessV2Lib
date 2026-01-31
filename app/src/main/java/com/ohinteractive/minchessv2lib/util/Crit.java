package com.ohinteractive.minchessv2lib.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Crit {
    
    public static final int MATERIAL_QUEEN = 1;
    public static final int MATERIAL_ROOK = 2;
    public static final int MATERIAL_BISHOP = 3;
    public static final int MATERIAL_KNIGHT = 4;
    public static final int MATERIAL_PAWN = 5;
    public static final int SQUARE_BONUS_KING = 6;
    public static final int SQUARE_BONUS_QUEEN = 7;
    public static final int SQUARE_BONUS_ROOK = 8;
    public static final int SQUARE_BONUS_BISHOP = 9;
    public static final int SQUARE_BONUS_KNIGHT = 10;
    public static final int SQUARE_BONUS_PAWN = 11;
    public static final int PAWN_SHIELD_CLOSE = 12;
    public static final int PAWN_SHIELD_FAR = 13;
    public static final int PAWN_STORM_CLOSE = 14;
    public static final int PAWN_STORM_FAR = 15;
    public static final int ROOK_PROTECTS = 16;
    public static final int KING_BLOCKS_ROOK = 17;
    public static final int QUEEN_EARLY_DEVELOPMENT = 18;
    public static final int ROOK_EARLY_DEVELOPMENT = 19;
    public static final int ROOK_PAIR = 20;
    public static final int ROOK_OPEN_FILE = 21;
    public static final int ROOK_ON_QUEEN_FILE = 22;
    public static final int BISHOP_PAIR = 23;
    public static final int BISHOP_OUTPOST = 24;
    public static final int KNIGHT_PAIR = 25;
    public static final int KNIGHT_OUTPOST = 26;
    public static final int DOUBLED_PAWN = 27;
    public static final int WEAK_PAWN = 28;
    public static final int ISOLATED_PAWN = 29;
    public static final int PAWN_PROTECTS = 30;
    public static final int PAWN_STORM_OWN_KING_OPPOSITE = 31;
    public static final int PASSED_PAWN_PHALANX = 32;
    public static final int KING_ENDGAME_DISTANCE = 33;
    public static final int MOBILITY_QUEEN = 34;
    public static final int MOBILITY_ROOK = 35;
    public static final int MOBILITY_BISHOP = 36;
    public static final int MOBILITY_KNIGHT = 37;
    public static final int QUEEN_AFFECTS_KING_SAFETY = 38;
    public static final int ROOK_AFFECTS_KING_SAFETY = 39;
    public static final int BISHOP_AFFECTS_KING_SAFETY = 40;
    public static final int KNIGHT_AFFECTS_KING_SAFETY = 41;
    public static final int QUEEN_ENEMY_KING_DISTANCE = 42;
    public static final int ROOK_ENEMY_KING_DISTANCE = 43;
    public static final int BISHOP_ENEMY_KING_DISTANCE = 44;
    public static final int KNIGHT_ENEMY_KING_DISTANCE = 45;
    public static final int ROOK_PAWN = 46;
    public static final int BAD_BISHOP = 47;
    public static final int BISHOP_PROTECTOR = 48;
    public static final int KNIGHT_PAWN = 49;
    public static final int KNIGHT_PROTECTOR = 50;
    public static final int PASSED_PAWN_SQUARE_BONUS = 51;
    public static final int PASSED_PAWN_UNSTOPPABLE = 52;
    public static final int PASSED_PAWN_ENEMY_KING_DISTANCE = 53;
    public static final int KING_SAFETY = 54;

    public static final int MAX_CRITERIA = 55;
    public static final String[] CRITERIA_NAME = { "", // 0
    "m_queen", "m_rook", "m_bishop", "m_knight", "m_pawn", // 1 - 5
    "s_king", "s_queen", "s_rook", "s_bishop", "s_knight", "s_pawn", // 6 - 11
    "b_pawnShieldClose", "b_pawnShieldFar", "p_pawnStormClose", "p_pawnStormFar", // 12 - 15
    "b_rookProtects", "p_kingBlocksRook", // 16 - 17
    "p_queenEarlyDevelopment", // 18
    "p_rookEarlyDevelopment", "p_rookPair", "b_rookOpenFile", "b_rookOnQueenFile", // 19 - 22
    "b_bishopPair", "b_bishopOutpost", // 23 - 24
    "p_knightPair", "b_knightOutpost", // 25 - 26
    "p_doubledPawn", "p_weakPawn", "p_isolatedPawn", "b_pawnProtects", "b_pawnStormOwnKingOpposite", "b_passed_pawn_phalanx", // 27 - 32
    "kingEndgameDistance", // 33
    "mobilityQueen", "mobilityRook", "mobilityBishop", "mobilityKnight", // 34 - 37
    "queenAffectsKingSafety", "rookAffectsKingSafety", "bishopAffectsKingSafety", "knightAffectsKingSafety", // 38 - 41
    "queenEnemyKingDistance", "rookEnemyKingDistance", "bishopEnemyKingDistance", "knightEnemyKingDistance", // 42 - 45
    "rookPawn", // 46
    "badBishop", "bishopProtector", // 47 - 48
    "knightPawn", "knightProtector", // 49 - 50
    "passedPawnSquareBonus", "passedPawnUnstoppable", "passedPawnEnemyKingDistance", // 51 - 53
    "kingSafety" // 54
    };
    public static double[] criteriaWeight = new double[MAX_CRITERIA];
    static {
        for(int i = 0; i < MAX_CRITERIA; i ++) {
            criteriaWeight[i] = 1.0;
        }
    }

    public static final int[][][][] BONUS = new int[7][2][64][25];
    static {
        loadBonus("bonus.properties");
    }

    public static final int[][] MATERIAL = new int[7][25];
    static {
        loadMaterial("material.properties");
    }

    public static final Object[] VALUE = new Object[MAX_CRITERIA];
    static {
        VALUE[PAWN_SHIELD_CLOSE] = new int[2][4][25];
        VALUE[PAWN_SHIELD_FAR] = new int[2][4][25];
        VALUE[PAWN_STORM_CLOSE] = new int[2][4][25];
        VALUE[PAWN_STORM_FAR] = new int[2][4][25];
        VALUE[ROOK_PROTECTS] = new int[25];
        VALUE[KING_BLOCKS_ROOK] = new int[25];
        VALUE[QUEEN_EARLY_DEVELOPMENT] = new int[25];
        VALUE[ROOK_EARLY_DEVELOPMENT] = new int[25];
        VALUE[ROOK_PAIR] = new int[25];
        VALUE[ROOK_OPEN_FILE] = new int[25];
        VALUE[ROOK_ON_QUEEN_FILE] = new int[25];
        VALUE[BISHOP_PAIR] = new int[25];
        VALUE[BISHOP_OUTPOST] = new int[25];
        VALUE[KNIGHT_PAIR] = new int[25];
        VALUE[KNIGHT_OUTPOST] = new int[25];
        VALUE[DOUBLED_PAWN] = new int[25];
        VALUE[WEAK_PAWN] = new int[25];
        VALUE[ISOLATED_PAWN] = new int[25];
        VALUE[PAWN_PROTECTS] = new int[25];
        VALUE[PASSED_PAWN_PHALANX] = new int[25];
        VALUE[KING_ENDGAME_DISTANCE] = new int[15][25];
        VALUE[MOBILITY_QUEEN] = new int[29][25];
        VALUE[MOBILITY_ROOK] = new int[15][25];
        VALUE[MOBILITY_BISHOP] = new int[15][25];
        VALUE[MOBILITY_KNIGHT] = new int[9][25];
        VALUE[QUEEN_AFFECTS_KING_SAFETY] = new int[9];
        VALUE[ROOK_AFFECTS_KING_SAFETY] = new int[9];
        VALUE[BISHOP_AFFECTS_KING_SAFETY] = new int[9];
        VALUE[KNIGHT_AFFECTS_KING_SAFETY] = new int[9];
        VALUE[QUEEN_ENEMY_KING_DISTANCE] = new int[15][25];
        VALUE[ROOK_ENEMY_KING_DISTANCE] = new int[15][25];
        VALUE[BISHOP_ENEMY_KING_DISTANCE] = new int[15][25];
        VALUE[KNIGHT_ENEMY_KING_DISTANCE] = new int[15][25];
        VALUE[ROOK_PAWN] = new int[3][9][25];
        VALUE[BAD_BISHOP] = new int[9][9][25];
        VALUE[BISHOP_PROTECTOR] = new int[15][25];
        VALUE[KNIGHT_PAWN] = new int[3][9][25];
        VALUE[KNIGHT_PROTECTOR] = new int[15][25];
        loadCrit("crit.properties");
    }
    
    private Crit() {}

    private static void loadMaterial(String filename) {
        Properties properties = new Properties();
        try(InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)) {
            if(in == null) {
                throw new IOException("Resource not found: " + filename);
            }
            properties.load(in);
            for(int type = 2; type < 7; type ++) {
                for(int phase = 0; phase < 25; phase ++) {
                    MATERIAL[type][phase] = Integer.parseInt(properties.getProperty("MATERIAL[" + (type - 2) + "][" + phase + "]"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadBonus(String filename) {
        Properties properties = new Properties();
        try(InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)) {
            if(in == null) {
                throw new IOException("Resource not found: " + filename);
            }
            properties.load(in);
            for(int type = 1; type < 7; type ++) {
                for(int player = 0; player < 2; player ++) {
                    for(int square = 0; square < 64; square ++) {
                        for(int phase = 0; phase < 25; phase ++) {
                            BONUS[type][player][square][phase] = Integer.parseInt(properties.getProperty("BONUS[" + type + "][" + player + "][" + square + "][" + phase + "]"));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadCrit(String filename) {
        Properties properties = new Properties();
        try(InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)) {
            if(in == null) {
                throw new IOException("Resource not found: " + filename);
            }
            properties.load(in);
            for(int phase = 0; phase < 25; phase ++) {
                for(int side = 0; side < 2; side ++) {
                    for(int numPawns = 0; numPawns < 4; numPawns ++) {
                            ((int[][][]) VALUE[PAWN_SHIELD_CLOSE])[side][numPawns][phase] = Integer.parseInt(properties.getProperty("PAWN_SHIELD[" + side + "][" + numPawns + "][0][" + phase + "]"));
                            ((int[][][]) VALUE[PAWN_SHIELD_FAR])[side][numPawns][phase] = Integer.parseInt(properties.getProperty("PAWN_SHIELD[" + side + "][" + numPawns + "][1][" + phase + "]"));
                            ((int[][][]) VALUE[PAWN_STORM_CLOSE])[side][numPawns][phase] = Integer.parseInt(properties.getProperty("PAWN_STORM[" + side + "][" + numPawns + "][0][" + phase + "]"));
                            ((int[][][]) VALUE[PAWN_STORM_FAR])[side][numPawns][phase] = Integer.parseInt(properties.getProperty("PAWN_STORM[" + side + "][" + numPawns + "][1][" + phase + "]"));
                    }
                }
                ((int[]) VALUE[ROOK_PROTECTS])[phase] = Integer.parseInt(properties.getProperty("ROOK_PROTECTS[" + phase + "]"));
                ((int[]) VALUE[KING_BLOCKS_ROOK])[phase] = Integer.parseInt(properties.getProperty("KING_BLOCKS_ROOK[" + phase + "]"));
                ((int[]) VALUE[QUEEN_EARLY_DEVELOPMENT])[phase] = Integer.parseInt(properties.getProperty("QUEEN_EARLY_DEVELOPMENT[" + phase + "]"));
                ((int[]) VALUE[ROOK_EARLY_DEVELOPMENT])[phase] = Integer.parseInt(properties.getProperty("ROOK_EARLY_DEVELOPMENT[" + phase + "]"));
                ((int[]) VALUE[ROOK_PAIR])[phase] = Integer.parseInt(properties.getProperty("ROOK_PAIR[" + phase + "]"));
                ((int[]) VALUE[ROOK_OPEN_FILE])[phase] = Integer.parseInt(properties.getProperty("ROOK_OPEN_FILE[" + phase + "]"));
                ((int[]) VALUE[ROOK_ON_QUEEN_FILE])[phase] = Integer.parseInt(properties.getProperty("ROOK_ON_QUEEN_FILE[" + phase + "]"));
                ((int[]) VALUE[BISHOP_PAIR])[phase] = Integer.parseInt(properties.getProperty("BISHOP_PAIR[" + phase + "]"));
                ((int[]) VALUE[BISHOP_OUTPOST])[phase] = Integer.parseInt(properties.getProperty("BISHOP_OUTPOST[" + phase + "]"));
                ((int[]) VALUE[KNIGHT_PAIR])[phase] = Integer.parseInt(properties.getProperty("KNIGHT_PAIR[" + phase + "]"));
                ((int[]) VALUE[KNIGHT_OUTPOST])[phase] = Integer.parseInt(properties.getProperty("KNIGHT_OUTPOST[" + phase + "]"));
                ((int[]) VALUE[DOUBLED_PAWN])[phase] = Integer.parseInt(properties.getProperty("DOUBLED_PAWN[" + phase + "]"));
                ((int[]) VALUE[WEAK_PAWN])[phase] = Integer.parseInt(properties.getProperty("WEAK_PAWN[" + phase + "]"));
                ((int[]) VALUE[ISOLATED_PAWN])[phase] = Integer.parseInt(properties.getProperty("ISOLATED_PAWN[" + phase + "]"));
                ((int[]) VALUE[PAWN_PROTECTS])[phase] = Integer.parseInt(properties.getProperty("PAWN_PROTECTS[" + phase + "]"));
                ((int[]) VALUE[PASSED_PAWN_PHALANX])[phase] = Integer.parseInt(properties.getProperty("PASSED_PAWN_PHALANX[" + phase + "]"));
                for(int distance = 1; distance < 15; distance ++) {
                    ((int[][]) VALUE[QUEEN_ENEMY_KING_DISTANCE])[distance][phase] = Integer.parseInt(properties.getProperty("QUEEN_ENEMY_KING_DISTANCE[" + distance + "][" + phase + "]"));
                    ((int[][]) VALUE[ROOK_ENEMY_KING_DISTANCE])[distance][phase] = Integer.parseInt(properties.getProperty("ROOK_ENEMY_KING_DISTANCE[" + distance + "][" + phase + "]"));
                    ((int[][]) VALUE[BISHOP_ENEMY_KING_DISTANCE])[distance][phase] = Integer.parseInt(properties.getProperty("BISHOP_ENEMY_KING_DISTANCE[" + distance + "][" + phase + "]"));
                    ((int[][]) VALUE[KNIGHT_ENEMY_KING_DISTANCE])[distance][phase] = Integer.parseInt(properties.getProperty("KNIGHT_ENEMY_KING_DISTANCE[" + distance + "][" + phase + "]"));
                    ((int[][]) VALUE[BISHOP_PROTECTOR])[distance][phase] = Integer.parseInt(properties.getProperty("BISHOP_PROTECTOR[" + distance + "][" + phase + "]"));
                    ((int[][]) VALUE[KNIGHT_PROTECTOR])[distance][phase] = Integer.parseInt(properties.getProperty("KNIGHT_PROTECTOR[" + distance + "][" + phase + "]"));
                    if(distance > 1) {
                        ((int[][]) VALUE[KING_ENDGAME_DISTANCE])[distance][phase] = Integer.parseInt(properties.getProperty("KING_ENDGAME_DISTANCE[" + distance + "][" + phase + "]"));
                    }
                }
                for(int mobility = 1; mobility < 29; mobility ++) {
                    ((int[][]) VALUE[MOBILITY_QUEEN])[mobility][phase] = Integer.parseInt(properties.getProperty("MOBILITY_QUEEN[" + mobility + "][" + phase + "]"));
                    if(mobility < 15) {
                        ((int[][]) VALUE[MOBILITY_ROOK])[mobility][phase] = Integer.parseInt(properties.getProperty("MOBILITY_ROOK[" + mobility + "][" + phase + "]"));
                        ((int[][]) VALUE[MOBILITY_BISHOP])[mobility][phase] = Integer.parseInt(properties.getProperty("MOBILITY_BISHOP[" + mobility + "][" + phase + "]"));
                        if(mobility < 9) {
                            ((int[][]) VALUE[MOBILITY_KNIGHT])[mobility][phase] = Integer.parseInt(properties.getProperty("MOBILITY_KNIGHT[" + mobility + "][" + phase + "]"));
                        }
                    }
                }
                for(int numRooks = 1; numRooks < 3; numRooks ++) {
                    for(int numPawns = 0; numPawns < 9; numPawns ++) {
                        ((int[][][]) VALUE[ROOK_PAWN])[numRooks][numPawns][phase] = Integer.parseInt(properties.getProperty("ROOK_PAWN[" + numRooks + "][" + numPawns + "][" + phase + "]"));
                        ((int[][][]) VALUE[KNIGHT_PAWN])[numRooks][numPawns][phase] = Integer.parseInt(properties.getProperty("KNIGHT_PAWN[" + numRooks + "][" + numPawns + "][" + phase + "]"));
                        for(int numOtherPawns = 0; numOtherPawns < 9; numOtherPawns ++) {
                            ((int[][][]) VALUE[BAD_BISHOP])[numPawns][numOtherPawns][phase] = Integer.parseInt(properties.getProperty("BAD_BISHOP[" + numPawns + "][" + numOtherPawns + "][" + phase + "]"));
                        }
                    }
                }
            }
            for(int safety = 1; safety < 9; safety ++) {
                ((int[]) VALUE[QUEEN_AFFECTS_KING_SAFETY])[safety] = Integer.parseInt(properties.getProperty("QUEEN_AFFECTS_KING_SAFETY[" + safety + "]"));
                ((int[]) VALUE[ROOK_AFFECTS_KING_SAFETY])[safety] = Integer.parseInt(properties.getProperty("ROOK_AFFECTS_KING_SAFETY[" + safety + "]"));
                ((int[]) VALUE[BISHOP_AFFECTS_KING_SAFETY])[safety] = Integer.parseInt(properties.getProperty("BISHOP_AFFECTS_KING_SAFETY[" + safety + "]"));
                ((int[]) VALUE[KNIGHT_AFFECTS_KING_SAFETY])[safety] = Integer.parseInt(properties.getProperty("KNIGHT_AFFECTS_KING_SAFETY[" + safety + "]"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
