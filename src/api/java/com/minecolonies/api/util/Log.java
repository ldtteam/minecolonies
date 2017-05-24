package com.minecolonies.api.util;

import com.minecolonies.api.util.constant.Constants;
import org.apache.logging.log4j.Level;
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
    private static Logger logger = null;

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
        //Only create logger if current logger is empty.
        if (logger == null)
        {
            Log.logger = LogManager.getLogger(Constants.MOD_ID);
        }
        return logger;
    }

    /**
     * Method used to print a big warning.
     * @param format The format the use.
     * @param data The data to format.
     */
    public static void bigWarning(String format, Object... data)
    {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        getLogger().log(Level.WARN, "****************************************");
        getLogger().log(Level.WARN, "* "+format, data);
        for (int i = 2; i < 8 && i < trace.length; i++)
        {
            getLogger().log(Level.WARN, "*  at %s%s", trace[i].toString(), i == 7 ? "..." : "");
        }
        getLogger().log(Level.WARN, "****************************************");
    }

}
