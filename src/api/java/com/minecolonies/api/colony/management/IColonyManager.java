package com.minecolonies.api.colony.management;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.handlers.ICombiningColonyEventHandler;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Objects that implement this interface function as
 */
public interface IColonyManager<B extends IBuilding, C extends IColony<B>> extends ICombiningColonyEventHandler
{

    /**
     * Indicates if this {@link IColonyManager} needs to be saved.
     *
     * @return True when he needs to be saved, false when not.
     */
    boolean isDirty();

    /**
     * Method to get a {@link IWorldColonyController} for a given world
     *
     * @param world The world to get the controller for.
     * @return The {@link IWorldColonyController} associated with that world.
     */
    @NotNull
    IWorldColonyController<B, C> getControllerForWorld(@NotNull World world);

    /**
     * Syncs the achievements for all colonies.
     */
    void syncAllColoniesAchievements();

    /**
     * Get all colonies in all worlds.
     *
     * @return a list of colonies.
     */
    @NotNull
    ImmutableList<IColony> getColonies();

    /**
     * Returns the minimum distance between two town halls, to not make colonies
     * collide.
     *
     * @return Minimum town hall distance.
     */
    int getMinimumDistanceBetweenTownHalls();

    /**
     * Get the Universal Unique ID for the server.
     *
     * @return the server Universal Unique ID for ther
     */
    UUID getServerUUID();

    /**
     * Set the server UUID.
     *
     * @param uuid the universal unique id
     */
    void setServerUUID(UUID uuid);

    /**
     * Whether or not a new schematic have been downloaded.
     *
     * @return True if a new schematic have been received.
     */
    boolean isSchematicDownloaded();

    /**
     * Set the schematic downloaded
     *
     * @param downloaded True if a new schematic have been received.
     */
    void setSchematicDownloaded(boolean downloaded);
}
