package com.ohinteractive.minchessv2lib.impl.search;

public class SearchStack {
    
    public SearchStack(int maxDepth) {
        this.stack = new SearchFrame[maxDepth + 1];
        for(int i = 0; i <= maxDepth; i ++) {
            stack[i] = new SearchFrame();
        }
        this.top = 0;
    }

    public SearchFrame push() {
        return stack[top ++];
    }

    public SearchFrame pop() {
        return stack[-- top];
    }

    public boolean isEmpty() {
        return top == 0;
    }

    private final SearchFrame[] stack;
    private int top;


}
