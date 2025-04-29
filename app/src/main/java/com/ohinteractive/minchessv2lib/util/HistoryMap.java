package com.ohinteractive.minchessv2lib.util;

public class HistoryMap {
    
    public HistoryMap() {
        this(512);
    }

    public HistoryMap(int initialCapacity) {
        this.capacity = initialCapacity;
        this.hashMask = capacity - 1;
        this.keys = new long[capacity];
        this.counts = new byte[capacity];
        this.size = 0;
    }

    public int increment(long key) {
        if(this.size >= (this.capacity >>> 1)) resize();
        int index = (int) key & this.hashMask;
        while(true) {
            if(this.keys[index] == EMPTY) {
                this.keys[index] = key;
                this.counts[index] = 1;
                this.size ++;
                return 1;
            }
            if(this.keys[index] == key) return ++ this.counts[index];
            index = (index + 1) & this.hashMask;
        }
    }

    public void reset() {
        for(int i = 0; i < this.capacity; i ++) {
            this.keys[i] = EMPTY;
            this.counts[i] = 0;
        }
        this.size = 0;
    }

    public void resize() {
        int newCapacity = capacity << 1;
        long[] oldKeys = keys;
        final int oldKeysLength = oldKeys.length;
        byte[] oldCounts = counts;
        this.keys = new long[newCapacity];
        this.counts = new byte[newCapacity];
        this.capacity = newCapacity;
        this.hashMask = this.capacity - 1;
        this.size = 0;
        for(int i = 0; i < oldKeysLength; i ++) {
            long key = oldKeys[i];
            if(key != EMPTY) {
                int count = oldCounts[i];
                reinsert(key, count);
            }
        }
    }

    private void reinsert(long key, int count) {
        int index = (int) key & this.hashMask;
        while(true) {
            if(this.keys[index] == EMPTY) {
                this.keys[index] = key;
                this.counts[index] = (byte) count;
                size ++;
                return;
            }
            index = (index + 1) & this.hashMask;
        }
    }

    private static final long EMPTY = 0L;
    private long[] keys;
    private byte[] counts;
    private int capacity;
    private int hashMask;
    private int size;

}
