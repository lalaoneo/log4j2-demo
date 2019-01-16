package com.lori.log4j2demo.disruptor.producer;

import com.lmax.disruptor.RingBuffer;
import com.lori.log4j2demo.disruptor.event.ValueEvent;

public interface EventProducer {

    void startProducing(final RingBuffer<ValueEvent> ringBuffer, final int count);
}
