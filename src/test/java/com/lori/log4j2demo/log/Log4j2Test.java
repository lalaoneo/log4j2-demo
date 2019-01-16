package com.lori.log4j2demo.log;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Log4j2Test {

    private static Logger logger = LoggerFactory.getLogger(Log4j2Test.class);

    @Test
    public void testLog4j2(){
        logger.info("测试日志能正常打印");
    }
}
