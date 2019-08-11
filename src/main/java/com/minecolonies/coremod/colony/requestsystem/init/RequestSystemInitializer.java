package com.minecolonies.coremod.colony.requestsystem.init;

import com.minecolonies.api.colony.requestsystem.manager.RequestMappingHandler;
import com.minecolonies.api.colony.requestsystem.requestable.*;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PrivateCrafting;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.requestable.SmeltableOre;
import com.minecolonies.coremod.colony.requestsystem.requests.StandardRequests;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.LevelRangeFilter;

public class RequestSystemInitializer
{

    public static void onPostInit()
    {
        reconfigureLogging();

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
        final LoggerConfig loggerConfig = config.getLoggerConfig(String.format("%s.requestsystem", Constants.MOD_ID));
        loggerConfig.addFilter(LevelRangeFilter.createFilter(Configurations.requestSystem.enableDebugLogging ? Level.DEBUG : Level.INFO, Level.FATAL, Filter.Result.NEUTRAL,
          Filter.Result.DENY));

        ctx.updateLoggers();
    }
}
