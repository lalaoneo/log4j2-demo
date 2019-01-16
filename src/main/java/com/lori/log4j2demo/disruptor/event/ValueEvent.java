package com.lori.log4j2demo.disruptor.event;

import com.lmax.disruptor.EventFactory;

public class ValueEvent {

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ValueEvent{" +
                "value=" + value +
                '}';
    }

    private int value;

    public static final EventFactory<ValueEvent> EVENT_FACTORY = ()-> new ValueEvent();
}
