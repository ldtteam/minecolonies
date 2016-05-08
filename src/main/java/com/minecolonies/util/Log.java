package com.minecolonies.util;

import com.minecolonies.lib.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Logging utility class
 */
public class Log
{

    /**
     * Mod logger
     */
    public static final Logger logger = LogManager.getLogger(Constants.MOD_ID);

    /**
     * Generates a logger for a specific class
     *
     * @param clazz Class to generate logger for
     * @return Created {@link Logger}
     */
    public static Logger generateLoggerForClass(Class<?> clazz)
    {
        return LogManager.getLogger(Constants.MOD_ID + "::" + clazz.getSimpleName());
    }
}
