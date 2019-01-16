package com.lori.log4j2demo.disruptor.consumer;

import com.lmax.disruptor.EventHandler;
import com.lori.log4j2demo.disruptor.event.ValueEvent;

public interface EventConsumer {

    EventHandler<ValueEvent>[] getEventHandlers();
}
