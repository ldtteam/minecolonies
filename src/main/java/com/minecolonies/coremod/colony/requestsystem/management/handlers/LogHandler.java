package com.minecolonies.coremod.colony.requestsystem.management.handlers;

import com.minecolonies.coremod.MineColonies;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class used to process the separate logger for the request system.
 */
public final class LogHandler
{
    private static final Logger logger = LogManager.getLogger("Minecolonies:RequestSystem");

    /**
     * Method used to log a string when the debug logging for the system is enabled.
     *
     * @param logEntry The string to write.
     */
    public static void log(final String logEntry)
    {
        if (MineColonies.getConfig().getCommon().enableDebugLogging.get())
        {
            logger.info(logEntry);
        }
    }
}
