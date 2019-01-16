package com.lori.log4j2demo.disruptor.producer;

import com.lmax.disruptor.RingBuffer;
import com.lori.log4j2demo.disruptor.event.ValueEvent;

public class SingleEventProducer implements EventProducer {
    @Override
    public void startProducing(RingBuffer<ValueEvent> ringBuffer, int count) {
        final Runnable runnable = ()->produce(ringBuffer,count);
        new Thread(runnable).start();
    }

    private void produce(RingBuffer<ValueEvent> ringBuffer, int count) {
        for (int i=0;i<count;i++){
            final long seq = ringBuffer.next();
            final ValueEvent event = ringBuffer.get(seq);
            event.setValue(i+100);
            ringBuffer.publish(seq);
        }
    }
}
