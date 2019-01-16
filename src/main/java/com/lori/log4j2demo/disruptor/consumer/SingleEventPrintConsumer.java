package com.lori.log4j2demo.disruptor.consumer;

import com.lmax.disruptor.EventHandler;
import com.lori.log4j2demo.disruptor.event.ValueEvent;

public class SingleEventPrintConsumer implements EventConsumer {
    @Override
    public EventHandler<ValueEvent>[] getEventHandlers() {
        final EventHandler<ValueEvent> eventHandler = (event,sequence,endOfBatch)->print(event.getValue(),sequence);
        return new EventHandler[]{eventHandler};
    }

    private void print(int value, long sequence) {
        System.out.println("Id is " + value + " sequence id that was used is " + sequence);
    }
}
