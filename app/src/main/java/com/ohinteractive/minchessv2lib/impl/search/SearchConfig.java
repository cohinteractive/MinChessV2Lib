package com.ohinteractive.minchessv2lib.impl.search;

public record SearchConfig(
    long[] rootPosition,
    int player,
    int maxDepth,
    long maxTimeMillis
) {}
