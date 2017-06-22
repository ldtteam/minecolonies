package com.minecolonies.api.colony.management.implementation;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.handlers.IColonyEventHandler;
import com.minecolonies.api.colony.management.IColonyManager;
import com.minecolonies.api.colony.management.IWorldColonyController;
import com.minecolonies.api.colony.management.legacy.LegacyColonyManagerLoader;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.Log;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

public class StandardSidelessColonyManager<B extends IBuilding, C extends IColony<B>> implements IColonyManager<B, C>
{
    HashMap<World, IWorldColonyController<B, C>> worldColonyControllerMap = new HashMap<>();
    LegacyColonyManagerLoader<B, C> legacyColonyManagerLoader = new LegacyColonyManagerLoader<>();

    @Override
    public boolean isDirty()
    {
        return worldColonyControllerMap.values().stream().anyMatch(c -> c.isDirty());
    }

    @NotNull
    @Override
    public IWorldColonyController<B, C> getControllerForWorld(@NotNull final World world)
    {
        Log.getLogger().debug("Getting controller for World: " + world.provider.getDimension());

        if (!worldColonyControllerMap.containsKey(world))
        {
            Log.getLogger().debug("Unknown world. Attempting extraction from WorldSaveData.");

            //Unknown world
            //Get the save data from the world.
            StandardWorldColonyControllerWorldSavedData worldData = (StandardWorldColonyControllerWorldSavedData) world.getPerWorldStorage()
                                                                                                                    .getOrLoadData(StandardWorldColonyControllerWorldSavedData.class,
                                                                                                                      StandardWorldColonyControllerWorldSavedData.WORLD_SAVED_DATA_PREFIX);
            StandardSidelessWorldColonyController<B, C> controller;


            if (worldData == null)
            {
                Log.getLogger().debug("World did not contain WorldSaveData for Minecolonies. Attempting load through Legacy system.");
                //Attempt to load a legacy world. Will automatically construct a Data Object for a new world if it did not exists in the legacy data too.
                controller = StandardSidelessWorldColonyController.attemptLoadFromLegacy(world, legacyColonyManagerLoader);

                Log.getLogger().debug("Saving newly created WorldDataSave to the world.");
                //Save the new Dataobject to the world.
                world.getPerWorldStorage().setData(StandardWorldColonyControllerWorldSavedData.WORLD_SAVED_DATA_PREFIX, controller.getSavedData());
            } else {
                Log.getLogger().debug("Successfully retrieved controller data from WorldSaveData store. Constructing new Controller.");
                //We got a set of data, lets get a new Controller
                controller = new StandardSidelessWorldColonyController<>(world, worldData);
            }

            Log.getLogger().debug("Registering new Controller.");
            //Controller created, registering:
            worldColonyControllerMap.put(world, controller);
        } else {
            Log.getLogger().debug("Controller already constructed using him.");
        }

        return worldColonyControllerMap.get(world);
    }

    @Override
    public void syncAllColoniesAchievements()
    {

    }

    @NotNull
    @Override
    public ImmutableList<IColony> getColonies()
    {
        return ImmutableList.copyOf(worldColonyControllerMap.values().stream()
                                      .map(c -> c.getColonies())
                                      .flatMap(cm -> cm.stream())
                                      .collect(Collectors.toList()));
    }

    @Override
    public int getMinimumDistanceBetweenTownHalls()
    {
        //  [TownHall](Radius)+(Padding)+(Radius)[TownHall]
        return (2 * Configurations.workingRangeTownHall) + Configurations.townHallPadding;
    }

    @Override
    public UUID getServerUUID()
    {
        return null;
    }

    @Override
    public void setServerUUID(final UUID uuid)
    {

    }

    @Override
    public boolean isSchematicDownloaded()
    {
        return false;
    }

    @Override
    public void setSchematicDownloaded(final boolean downloaded)
    {

    }

    @NotNull
    @Override
    public ImmutableCollection<IColonyEventHandler> getCombinedHandlers()
    {
        return null;
    }


}
