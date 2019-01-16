# Disruptor机制
![avatar](\src\main\resources\images\1.png)
* 一个生产者消费者模型框架,用的不是队列而是用数组来缓存数据
## ringbuffer
* 它是一个指定长度的数组,长度要求是2的幂次方,原因:计算数组下标时,与运算比取模运算更快
* 优点
    * 这种设计可以避免锁竞争,采取CAS自旋无锁方式,性能更好
    * 数组这种数据结构在内存的地址是连续的,比链表更好,而且可以预加载,访问速度更快
    * 内存对象的优化,ringbuffer已经创建了数据对象,只需要修改数据发布一下就OK,避免频繁的创建对象,申请对象内存的开销,也可以避免GC频繁回收垃圾对象
```java
private void fill(EventFactory<E> eventFactory)
{
    for (int i = 0; i < bufferSize; i++)
    {
        // 数据里面的数据对象已经创建好
        entries[BUFFER_PAD + i] = eventFactory.newInstance();
    }
}
```
## next()方法
```java
public long next(int n){
    long current;
    long next;
    do
    {
        current = cursor.get();
        next = current + n;
        long wrapPoint = next - bufferSize;
        long cachedGatingSequence = gatingSequenceCache.get();
        if (wrapPoint > cachedGatingSequence || cachedGatingSequence > current){
            long gatingSequence = Util.getMinimumSequence(gatingSequences, current);
            if (wrapPoint > gatingSequence)
            {
                waitStrategy.signalAllWhenBlocking();
                LockSupport.parkNanos(1); 
                continue;
            }
            gatingSequenceCache.set(gatingSequence);
        }
        else if (cursor.compareAndSet(current, next)){
            break;
        }
    }
    while (true);
    return next;
}
```
* ringBuffer初始化时,序列初始值为-1,next()方法默认是n=1
* 此方法由生产者调用,ringbuffer满了生产者会阻塞等待LockSupport.parkNanos(1),阻塞之前唤醒等待中的消费者进行处理
* ringbuffer没有满,通过CAS更新游标的序列值,这就是高性能之处,无锁化处理
## get(seq)方法
* 通过序列号获取对应数组下标的数据对象
```java
protected final E elementAt(long sequence)
{
    return (E) UNSAFE.getObject(entries, REF_ARRAY_BASE + ((sequence & indexMask) << REF_ELEMENT_SHIFT));
}
```
* 通过位运算计算出数据内存地址,直接操作JDK代码性能更优
## publish(long sequence)方法
* 修改数组的数据后,发布事件
```java
public void publish(final long sequence)
{
    setAvailable(sequence);
    waitStrategy.signalAllWhenBlocking();
}
private void setAvailableBufferValue(int index, int flag)
{
    long bufferAddress = (index * SCALE) + BASE;
    UNSAFE.putOrderedInt(availableBuffer, bufferAddress, flag);
}
private final int[] availableBuffer
```
* availableBuffer与ringbuffer大小一致,标识ringbuffer那个下标可以进行消费,相当于ringbuffer数组的标识
* 单个生产者直接设置游标值,标识ringbuffer那个数组下标可以进行消费
* waitStrategy.signalAllWhenBlocking()唤醒等待的消费者进行消费处理
* [参考文献](https://blog.csdn.net/zhxdick/article/details/52041876?locationNum=4&fps=1)
## Disruptor机制
```java
public Disruptor(
            final EventFactory<T> eventFactory,
            final int ringBufferSize,
            final ThreadFactory threadFactory,
            final ProducerType producerType,
            final WaitStrategy waitStrategy)
{
    this(
        RingBuffer.create(producerType, eventFactory, ringBufferSize, waitStrategy),
        new BasicExecutor(threadFactory));
}
```
* 创建ringbuffer及线程池
```java
disruptor.handleEventsWith(eventConsumer.getEventHandlers())
```
* 向ConsumerRepository注册消费者处理的handle
* disruptor.start()启动线程池执行执行消费者处理,如果有数据调用注册的handle进行处理,没有阻塞住,根据不同的策略进行设置
## BatchEventProcessor读取ringbuffer数据线程
* 通过线程池执行其run方法
```java
public void run(){
    if (!running.compareAndSet(false, true))
    {
        throw new IllegalStateException("Thread is already running");
    }
    sequenceBarrier.clearAlert();
    notifyStart();
    T event = null;
    long nextSequence = sequence.get() + 1L;
    try{
        while (true){
            try{
                final long availableSequence = sequenceBarrier.waitFor(nextSequence);

                while (nextSequence <= availableSequence){
                    event = dataProvider.get(nextSequence);
                    eventHandler.onEvent(event, nextSequence, nextSequence == availableSequence);
                    nextSequence++;
                }
                sequence.set(availableSequence);
            }
            catch (final TimeoutException e){
                notifyTimeout(sequence.get());
            }
            catch (final AlertException ex){
                if (!running.get()){
                    break;
                }
            }
            catch (final Throwable ex){
                exceptionHandler.handleEventException(ex, nextSequence, event);
                sequence.set(nextSequence);
                nextSequence++;
            }
        }
    }
    finally{
        notifyShutdown();
        running.set(false);
    }
}
```
* sequenceBarrier.waitFor(nextSequence)在获取不到数据的情况下,根据不同的策略进行等待
* eventHandler.onEvent(event, nextSequence, nextSequence == availableSequence)回调业务处理handle
## WaitStrategy等待策略
* BlockingWaitStrategy
    * processorNotifyCondition.await()进入等待,让出CPU,等待唤醒
    * 生产者push事件后会调用signalAllWhenBlocking()唤醒等待的消费者
    * 延迟较高使用了Lock机制,并且等待唤醒
```java
public long waitFor(long sequence, Sequence cursorSequence, Sequence dependentSequence, SequenceBarrier barrier)
        throws AlertException, InterruptedException{
    long availableSequence;
    if (cursorSequence.get() < sequence){
        lock.lock();
        try
        {
            while (cursorSequence.get() < sequence)
            {
                barrier.checkAlert();
                processorNotifyCondition.await();
            }
        }
        finally
        {
            lock.unlock();
        }
    }
}
```
* SleepingWaitStrategy
    * 是另一种较为平衡CPU消耗与延迟的WaitStrategy
    * 在不同次数的重试后，采用不同的策略选择继续尝试或者让出CPU或者sleep。这种策略延迟不均匀
```java
private int applyWaitMethod(final SequenceBarrier barrier, int counter) throws AlertException {
    //检查是否需要终止
    barrier.checkAlert();
    //如果在200~100,重试
    if (counter > 100) {
        --counter;
    }
    //如果在100~0,调用Thread.yield()让出CPU
    else if (counter > 0) {
        --counter;
        Thread.yield();
    }
    //<0的话，利用LockSupport.parkNanos(1L)来sleep最小时间
    else {
        LockSupport.parkNanos(1L);
    }
    return counter;
}
```
* BusySpinWaitStrategy
    * 是一种延迟最低，最耗CPU的策略。通常用于消费线程数小于CPU数的场景
    * 它无需唤醒,也不会进入等待,一直自旋获取数据,比较耗CPU
```java
public long waitFor(
            final long sequence, Sequence cursor, final Sequence dependentSequence, final SequenceBarrier barrier)
            throws AlertException, InterruptedException {
    long availableSequence;
    //一直while自旋检查
    while ((availableSequence = dependentSequence.get()) < sequence) {
        barrier.checkAlert();
    }
    return availableSequence;
}
```
* [参考文献](https://blog.csdn.net/zhxdick/article/details/52077883)
