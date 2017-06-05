package com.minecolonies.api.colony.management;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.handlers.ICombiningColonyEventHandler;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * A {@link IWorldColonyController} is an Object that manages Colonies in a given World.
 */
public interface IWorldColonyController<B extends IBuilding, C extends IColony<B>> extends ICombiningColonyEventHandler, INBTSerializable<NBTTagCompound>
{

    /**
     * Method to get the world that the controller belongs to.
     *
     * @return The world of the controller.
     */
    World getWorld();

    /**
     * Create a new Colony in the given world and at that location.
     *
     * @param pos    Coordinate of the center of the colony.
     * @param player the player that creates the colony - owner.
     * @return The created colony.
     */
    @NotNull
    C createColony(@NotNull BlockPos pos, @NotNull EntityPlayer player);

    /**
     * Specify that colonies should be saved.
     */
    void markDirty();

    /**
     * Indicates if this {@link IWorldColonyController} needs to be saved.
     *
     * @return True when he needs to be saved, false when not.
     */
    boolean isDirty();

    /**
     * Delete a colony and kill all citizens/purge all buildings.
     *
     * @param id the colonies id.
     * @throws IllegalArgumentException when the given id is unknown.
     */
    void deleteColony(@NotNull IToken id) throws IllegalArgumentException;

    /**
     * Get Colony by UUID.
     *
     * @param id ID of colony.
     * @return Colony with given ID, or null if she is unknown.
     */
    @Nullable
    C getColony(@NotNull IToken id);

    /**
     * Syncs the achievements for all colonies.
     */
    void syncAllColoniesAchievements();

    /**
     * Get a AbstractBuilding by a World and coordinates.
     *
     * @param pos Block position.
     * @return AbstractBuilding at the given location.
     */
    B getBuilding(@NotNull BlockPos pos);

    /**
     * Get colony that contains a given coordinate.
     *
     * @param pos coordinates.
     * @return Colony at the given location.
     */
    C getColony(@NotNull BlockPos pos);

    /**
     * Get all colonies in this worlds.
     *
     * @return a list of colonies.
     */
    @NotNull
    ImmutableList<C> getColonies();

    /**
     * Gets the closest colony. Might return null if none is found.
     *
     * @param pos Block position to check for.
     * @return The closest colony. Null if it is not found.
     */
    @Nullable
    C getClosestColony(@NotNull BlockPos pos);

    /**
     * Returns a colony with the given Player as owner.
     *
     * @param owner Entity Player.
     * @return The colony belonging to specific player.
     */
    @Nullable
    C getColonyByOwner(@NotNull EntityPlayer owner);

    /**
     * Returns a colony with given Player as owner.
     *
     * @param owner UUID of the owner.
     * @return The Colony belonging to specific player.
     */
    @Nullable
    C getColonyByOwner(@NotNull UUID owner);

    /**
     * Check if a given coordinate is inside any other colony.
     *
     * @param pos the position to check.
     * @return true if a colony has been found.
     */
    boolean isCoordinateInAnyColony(@NotNull BlockPos pos);
}
