package com.minecolonies.api.colony.management.implementation;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.handlers.IColonyEventHandler;
import com.minecolonies.api.colony.management.IColonyManager;
import com.minecolonies.api.colony.management.IWorldColonyController;
import com.minecolonies.api.configuration.Configurations;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

public class StandardSidelessColonyManager<B extends IBuilding, C extends IColony<B>> implements IColonyManager<B, C>
{

    HashMap<World, IWorldColonyController<B, C>> worldColonyControllerMap = new HashMap<>();

    @Override
    public boolean isDirty()
    {
        return worldColonyControllerMap.values().stream().anyMatch(c -> c.isDirty());
    }

    @NotNull
    @Override
    public IWorldColonyController<B, C> getControllerForWorld(@NotNull final World world)
    {
        if (!worldColonyControllerMap.containsKey(world))
        {
            //Unknown world
            //Get the save data from the world.
            StandardWorldColonyControllerWorldSavedData worldData = (StandardWorldColonyControllerWorldSavedData) world.getPerWorldStorage()
                                                                                                                    .getOrLoadData(StandardWorldColonyControllerWorldSavedData.class,
                                                                                                                      StandardWorldColonyControllerWorldSavedData.WORLD_SAVED_DATA_PREFIX);
            if (worldData == null)
            {
                //Brand new world
                worldData = new StandardWorldColonyControllerWorldSavedData();
            }

            //We got a set of data, lets get a new Controller
            StandardSidelessWorldColonyController<B, C> controller = new StandardSidelessWorldColonyController<>(world, worldData);

            //Controller created, registering:
            worldColonyControllerMap.put(world, controller);
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
