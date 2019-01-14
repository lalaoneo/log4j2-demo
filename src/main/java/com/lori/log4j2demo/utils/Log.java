package com.lori.log4j2demo.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {

    private static Logger logger = LoggerFactory.getLogger(Log.class);

    public static void log() {
        logger.info("first slf4j demo");
    }
}
