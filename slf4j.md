# 简介
* 全称:simple logging facade for java
* 什么是门面模式?
    * 门面模式的核心为Facade即门面对象,其核心为以下几点
        * 知道所有子角色的功能及责任
        * 将客户端发来的请求委派到子系统中,本身没有实际业务逻辑
        * 不参与子系统内业务逻辑的实现
* slf4j是门面模式的经典应用
    * 它只是一个日志标准,并不是日志系统的具体实现
    * 使用端只管打印日志,如何打印由具体打印组件实现
        * slf4j-simple
        * log4j2
        * logback
    * 只做两件事情
        * 提供日志接口
        * 提供获取具体日志打印对象
* 使用方式
    * 这个用例并不会打印出日志,因为pom没有引入具体的日志实现组件
```java
private static Logger logger = LoggerFactory.getLogger(LogSlf4jDemo.class);

    public static void main(String[] args) {
        logger.info("first slf4j demo");
    }
```
# 源码分析
## Logger日志打印对象
* LoggerFactory.getLogger(LogSlf4jDemo.class)获取Logger日志打印对象
* bind()方法会扫描org/slf4j/impl/StaticLoggerBinder.class此类
    * 如果扫描到会调用StaticLoggerBinder.getSingleton()获取实例类
    * 如果存在多个,由JVM选择一个进行加载
    * 具体逻辑由子系统实现
* 由于没有找到,这里会抛出NoClassDefFoundError,并输出警告
* 此时会创建一个静默NOPLoggerFactory的ILoggerFactory对象,防止程序启动失败
    * 最终是由ILoggerFactory获取Logger对象
* NOPLoggerFactory会创建一个NOPLogger,它打印日志的方法都为空,所以不会打印任何日志
## Marker标记对象
* 对某个输出日志加上标记,再配置过滤规则
```java
private static final Logger logger = LoggerFactory.getLogger(App.class);
	 public static void main( String[] args )
	 {
	 	String confidentialMarkerText = "CONFIDENTIAL";
		 Marker confidentialMarker = MarkerFactory.getMarker(confidentialMarkerText);
		 logger.debug("Hello world from slf4j");
		 logger.debug("This logger supports confidentail messages....");
		 logger.debug(confidentialMarker,"This is a confidential message....");
		 logger.debug("Just logged a confidential message");
	 }
```
```xml
<configuration>

	<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
		<!-- encoders are assigned the type ch.qos.logback.classic.encoder.PatternLayoutEncoder
			by default -->
		<encoder>
			<pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
			</pattern>
		</encoder>
	</appender>

	<turboFilter class="ch.qos.logback.classic.turbo.MarkerFilter">
		<Name>CONFIDENTIAL_FILTER</Name>
		<Marker>CONFIDENTIAL</Marker>
		<OnMatch>DENY</OnMatch>
	</turboFilter>


	<root level="DEBUG">
		<appender-ref ref="STDOUT" />
	</root>
</configuration>
```
## MDC
* 全名:Mapped Diagnostic Contexts 映射诊断上下文
* 为了便于我们诊断线上问题而出现的方法工具类
* 线上各种日志击穿,没有统一的标识表名那些日志是属于同一个线程输出的
* 使用方式
```java
private static final Logger logger = LoggerFactory.getLogger(LogTest.class);
 
    public static void main(String[] args) {
 
        MDC.put("THREAD_ID", String.valueOf(Thread.currentThread().getId()));
 
        logger.info("纯字符串信息的info级别日志");
 

```
> [2015-04-30 15:34:35 INFO  io.github.ketao1989.log4j.LogTest.main(LogTest.java:29)] 1 纯字符串信息的info级别日志
## 日志输出
* 建议使用{},底层采用indexOf()方法来进行替换
* %s是基本库内置的,它是使用string拼接的方式,性能开销大
* {}的解析是由MessageFormatter进行解析的