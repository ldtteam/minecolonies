package com.minecolonies.coremod.colony.requestsystem.init;

import com.minecolonies.api.colony.requestsystem.manager.RequestMappingHandler;
import com.minecolonies.api.colony.requestsystem.requestable.*;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PrivateCrafting;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.requestable.SmeltableOre;
import com.minecolonies.coremod.colony.requestsystem.requests.StandardRequests;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.LevelRangeFilter;
import org.jetbrains.annotations.NotNull;

public class RequestSystemInitializer
{

    public static void onPostInit()
    {
        reconfigureLogging();

        Log.getLogger().warn("Register mappings");
        RequestMappingHandler.registerRequestableTypeMapping(Stack.class, StandardRequests.ItemStackRequest.class);
        RequestMappingHandler.registerRequestableTypeMapping(Burnable.class, StandardRequests.BurnableRequest.class);
        RequestMappingHandler.registerRequestableTypeMapping(Delivery.class, StandardRequests.DeliveryRequest.class);
        RequestMappingHandler.registerRequestableTypeMapping(Food.class, StandardRequests.FoodRequest.class);
        RequestMappingHandler.registerRequestableTypeMapping(Tool.class, StandardRequests.ToolRequest.class);
        RequestMappingHandler.registerRequestableTypeMapping(SmeltableOre.class, StandardRequests.SmeltAbleOreRequest.class);
        RequestMappingHandler.registerRequestableTypeMapping(StackList.class, StandardRequests.ItemStackListRequest.class);
        RequestMappingHandler.registerRequestableTypeMapping(PublicCrafting.class, StandardRequests.PublicCraftingRequest.class);
        RequestMappingHandler.registerRequestableTypeMapping(PrivateCrafting.class, StandardRequests.PrivateCraftingRequest.class);
    }

    private static void reconfigureLogging()
    {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        final LoggerConfig loggerConfig = getLoggerConfiguration(config, String.format("%s.requestsystem", Constants.MOD_ID));
        loggerConfig.addFilter(LevelRangeFilter.createFilter(Level.FATAL, MineColonies.getConfig().getCommon().enableDebugLogging.get() ? Level.DEBUG : Level.INFO, Filter.Result.NEUTRAL,
          Filter.Result.DENY));

        ctx.updateLoggers();

        LogManager.getLogger(String.format("%s.requestsystem", Constants.MOD_ID)).warn(String.format("Updated logging config. RS Debug logging enabled: %s",
          MineColonies.getConfig().getCommon().enableDebugLogging.get()));
    }

    private static LoggerConfig getLoggerConfiguration(@NotNull final Configuration configuration, @NotNull final String loggerName)
    {
        final LoggerConfig lc = configuration.getLoggerConfig(loggerName);
        if (lc.getName().equals(loggerName))
        {
            return lc;
        }
        else
        {
            final LoggerConfig nlc = new LoggerConfig(loggerName, lc.getLevel(), lc.isAdditive());
            nlc.setParent(lc);
            configuration.addLogger(loggerName, nlc);
            configuration.getLoggerContext().updateLoggers();

            return nlc;
        }
    }
}
