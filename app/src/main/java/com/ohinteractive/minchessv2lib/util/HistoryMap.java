package com.ohinteractive.minchessv2lib.util;

public class HistoryMap {
    
    public HistoryMap() {
        this(512);
    }

    public HistoryMap(int initialCapacity) {
        this.capacity = initialCapacity;
        this.hashMask = initialCapacity - 1;
        this.keys = new long[initialCapacity];
        this.counts = new byte[initialCapacity];
        this.generations = new byte[initialCapacity];
        this.generation = 1;
        this.threshold = capacity >>> 1;
    }

    public void reset() {
        this.generation ++;
        if(this.generation == 0) {
            for(int i = 0; i < capacity; i ++) this.generations[i] = 0;
            this.generation = 1;
        }
    }

    public int increment(long key) {
        int index = (int) key & this.hashMask;
        while(true) {
            final int g = this.generations[index];
            final long k = this.keys[index];
            if(g == this.generation && k == key) return ++ this.counts[index];
            if(g != this.generation) {
                this.keys[index] = key;
                this.counts[index] = 1;
                this.generations[index] = (byte) this.generation;
                if(-- threshold == 0) resize();
                return 1;
            }
            index = (index + 1) & this.hashMask;
        }
    }

    public void resize() {
        final int newCapacity = this.capacity << 1;
        final long[] oldKeys = this.keys;
        final byte[] oldCounts = this.counts;
        final byte[] oldGenerations = this.generations;

        this.keys = new long[newCapacity];
        this.counts = new byte[newCapacity];
        this.generations = new byte[newCapacity];

        final int newThreshold = newCapacity >>> 1;
        final int oldCapacity = this.capacity;
        final int oldGeneration = this.generation;

        this.capacity = newCapacity;
        this.hashMask = this.capacity - 1;
        this.threshold = newThreshold;
        this.generation = 1;

        for(int i = 0; i < oldCapacity; i ++) {
            if(oldGenerations[i] != oldGeneration) continue;
            final long key = oldKeys[i];
            final byte count = oldCounts[i];
            int index = (int) key & this.hashMask;
            while(true) {
                if(this.generations[index] != generation) {
                    this.keys[index] = key;
                    this.counts[index] = count;
                    this.generations[index] = (byte) this.generation;
                    break;
                }
                index = (index + 1) & this.hashMask;
            }
        }
    }

    private long[] keys;
    private byte[] counts;
    private byte[] generations;
    private int generation;
    private int capacity;
    private int hashMask;
    private int threshold;

}
