package com.minecolonies.coremod.placementhandlers;

import com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers;

/**
 * Registers all minecolonies placement handlers
 */
public final class PlacementHandlerInitializer
{
    /**
     * Private constructor to hide implicit one.
     */
    private PlacementHandlerInitializer()
    {
        /*
         * Intentionally left empty.
         */
    }

    public static void initHandlers()
    {
        PlacementHandlers.add(new GeneralBlockPlacementHandler(), PlacementHandlers.GeneralBlockPlacementHandler.class);
        PlacementHandlers.add(new BarracksTowerHandler());
        PlacementHandlers.add(new FieldPlacementHandler());
        PlacementHandlers.add(new DimensionFluidHandler());
        PlacementHandlers.add(new RackPlacementHandler());
        PlacementHandlers.add(new GravePlacementHandler());
        PlacementHandlers.add(new NamedGravePlacementHandler());
        PlacementHandlers.add(new WayPointBlockPlacementHandler());
        PlacementHandlers.add(new GatePlacementHandler());
        PlacementHandlers.add(new NetherrackPlacementHandler());
    }
}
