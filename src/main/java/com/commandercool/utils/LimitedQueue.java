package com.commandercool.utils;

import java.util.LinkedList;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LimitedQueue<E> extends LinkedList<E> {

    private final int limit;

    @Override
    public void push(E e) {
        if (size() > limit) {
            removeLast();
        }
        super.push(e);
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }
}
