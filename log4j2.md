# log4j2原理机制
* 启动时会初始化LoggerContext日志上下文对象
* 初始化默认名称的日志配置文件log4j2-spring.xml
## log4j2的性能高于logback
* 其根本原因是在log4j2使用了LMAX disruptor框架
* 具体介绍看[LMX Disruptor](E:\study-files\源码\muilti-thread\LMAX_Disruptor.md)
## bind()机制
* StaticLoggerBinder.getSingleton()获取StaticLoggerBinder单例对象
* StaticLoggerBinder实例化时会创建Log4jLoggerFactory对象,用于加载Logger对象
* 第一次调用iLoggerFactory.getLogger时,懒惰初始化Log4jLogger对象,并缓存起来
## marker机制
* 使用方式与logback不同
```xml
<console name="Console" target="SYSTEM_OUT">
    <MarkerFilter marker="FIRST_MARKER" onMatch="ACCEPT" onMismatch="DENY"/>
    <!--输出日志的格式-->
    <PatternLayout pattern="[%d{HH:mm:ss:SSS}] [%p] - %l %X{lori_id} - %m%n"/>
</console>
```
* 上面的配置结果,只有标记了FIRST_MARKER的日志信息才会输出
## MDC机制
* 因为采用门面模式,所有使用方式都是一样的MDC.put("lori_id","i am lori")
* 原理也是一样的,设置的数据与当前线程绑定
* 可以查看logback原理机制
## 使用
* 引用依赖,spring-boot-starter需要排除掉logging依赖
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
    <!-- 依赖者需要显示申明,否则不依赖此jar -->
    <optional>true</optional>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j2</artifactId>
</dependency>
```
* 配置xml文件,不配置控制台会抛出creating converter for xwEx java.lang.reflect.InvocationTargetException异常,默认文件名称:log4j2-spring.xml,否则需要在application.properties指定日志配置文件名
```xml
<configuration status="warn" monitorInterval="30">
    <appenders>
        <console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="[%d{HH:mm:ss:SSS}] [%p] - %l - %m%n"/>
        </console>
    </appenders>
    <loggers>
        <root level="INFO">
            <appender-ref ref="Console"/>
        </root>
    </loggers>
</configuration>
```