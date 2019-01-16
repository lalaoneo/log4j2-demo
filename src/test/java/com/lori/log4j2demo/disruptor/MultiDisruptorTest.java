package com.lori.log4j2demo.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lori.log4j2demo.disruptor.consumer.EventConsumer;
import com.lori.log4j2demo.disruptor.consumer.MultiEventPrintConsumer;
import com.lori.log4j2demo.disruptor.event.ValueEvent;
import com.lori.log4j2demo.disruptor.producer.DelayedMultiEventProducer;
import com.lori.log4j2demo.disruptor.producer.EventProducer;
import com.lori.log4j2demo.disruptor.utils.DisruptorUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MultiDisruptorTest {

    @Test
    public void testMultiDisruptor() throws InterruptedException {
        EventConsumer consumer = new MultiEventPrintConsumer();

        EventProducer producer = new DelayedMultiEventProducer();

        Disruptor<ValueEvent> disruptor = DisruptorUtils.createDisruptor(ProducerType.MULTI,consumer);
        /**
         * 创建disruptor比较慢,等待disruptor创建成功
         */
        Thread.sleep(1000);

        RingBuffer<ValueEvent> ringBuffer = disruptor.start();

        producer.startProducing(ringBuffer,1000);

        /**
         * 等待线程执行完成
         */
        Thread.sleep(3000);

        disruptor.halt();
        disruptor.shutdown();
    }
}
