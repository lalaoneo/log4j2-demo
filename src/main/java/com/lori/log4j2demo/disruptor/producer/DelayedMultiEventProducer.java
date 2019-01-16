package com.lori.log4j2demo.disruptor.producer;

import com.lmax.disruptor.RingBuffer;
import com.lori.log4j2demo.disruptor.event.ValueEvent;

public class DelayedMultiEventProducer implements EventProducer {
    @Override
    public void startProducing(RingBuffer<ValueEvent> ringBuffer, int count) {
        Runnable simpleProducer = () -> produce(ringBuffer, count, false);
        Runnable delayedProducer = () -> produce(ringBuffer, count, true);
        new Thread(simpleProducer).start();
        new Thread(delayedProducer).start();
    }

    private void produce(RingBuffer<ValueEvent> ringBuffer, int count, boolean b) {
        for (int i=0;i<count;i++){
            long seq = ringBuffer.next();
            ValueEvent event = ringBuffer.get(seq);
            event.setValue(i+200);
            ringBuffer.publish(seq);
            if (b){
                addDelay();
            }
        }
    }

    private void addDelay() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
