package com.minecolonies.api.util;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.util.constant.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Logging utility class.
 */
public final class Log
{
    /**
     * Mod logger.
     */
    private static Logger logger = LogManager.getLogger(Constants.MOD_ID);

    /**
     * Private constructor to hide the public one.
     */
    private Log()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Getter for the minecolonies Logger.
     *
     * @return the logger.
     */
    public static Logger getLogger()
    {
        return logger;
    }
}
