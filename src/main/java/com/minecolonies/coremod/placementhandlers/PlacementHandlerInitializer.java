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
        // TODO use the new 'add' helper static functions in PlacementHandlers
        PlacementHandlers.handlers.clear();
        PlacementHandlers.handlers.add(new PlacementHandlers.AirPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.FirePlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.GrassPlacementHandler());
        PlacementHandlers.handlers.add(new NetherrackPlacementHandler());
        PlacementHandlers.handlers.add(new GatePlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.DoorPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.BedPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.DoublePlantPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.SpecialBlockPlacementAttemptHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.FlowerPotPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.BlockGrassPathPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.StairBlockPlacementHandler());
        PlacementHandlers.handlers.add(new FencePlacementHandler());
        PlacementHandlers.handlers.add(new ChestPlacementHandler());
        PlacementHandlers.handlers.add(new WayPointBlockPlacementHandler());
        PlacementHandlers.handlers.add(new RackPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.FallingBlockPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.BannerPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.FluidSubstitutionPlacementHandler());
        PlacementHandlers.handlers.add(new BuildingSubstitutionBlock());
        PlacementHandlers.handlers.add(new BuildingBarracksTowerSub());
        PlacementHandlers.handlers.add(new FieldPlacementHandler());
        PlacementHandlers.handlers.add(new GeneralBlockPlacementHandler());
    }
}
