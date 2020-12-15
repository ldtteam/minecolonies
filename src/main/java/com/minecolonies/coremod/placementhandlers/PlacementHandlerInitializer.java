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
        PlacementHandlers.add(new GeneralBlockPlacementHandler());
        PlacementHandlers.add(new FieldPlacementHandler());
        PlacementHandlers.add(new BuildingBarracksTowerSub());
        PlacementHandlers.add(new BuildingSubstitutionBlock());
        PlacementHandlers.add(new RackPlacementHandler());
        PlacementHandlers.add(new WayPointBlockPlacementHandler());
        PlacementHandlers.add(new ChestPlacementHandler());
        PlacementHandlers.add(new FencePlacementHandler());
        PlacementHandlers.add(new GatePlacementHandler());
        PlacementHandlers.add(new NetherrackPlacementHandler());
    }
}
