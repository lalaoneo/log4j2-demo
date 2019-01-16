package com.lori.log4j2demo.disruptor.utils;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.lori.log4j2demo.disruptor.consumer.EventConsumer;
import com.lori.log4j2demo.disruptor.event.ValueEvent;

import java.util.concurrent.ThreadFactory;

public class DisruptorUtils {

    public static Disruptor<ValueEvent> createDisruptor(final ProducerType producerType, final EventConsumer eventConsumer) {
        final ThreadFactory factory = DaemonThreadFactory.INSTANCE;
        Disruptor<ValueEvent> disruptor = new Disruptor<>(ValueEvent.EVENT_FACTORY,16,factory,producerType,new BlockingWaitStrategy());
        disruptor.handleEventsWith(eventConsumer.getEventHandlers());
        return disruptor;
    }
}
