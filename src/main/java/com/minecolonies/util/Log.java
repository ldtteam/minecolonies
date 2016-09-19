package com.minecolonies.util;

import com.minecolonies.lib.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Logging utility class
 */
public final class Log
{
    /**
     * Mod logger
     */
    private static Logger logger = null;

    /**
     * Private constructor to hide the public one.
     */
    private Log()
    {
        //Hides implicit constructor.
    }

    /**
     * Getter for the minecolonies Logger.
     *
     * @return the logger.
     */
    public static Logger getLogger()
    {
        if (logger == null)
        {
            Log.logger = LogManager.getLogger(Constants.MOD_ID);
        }
        return logger;
    }
}
