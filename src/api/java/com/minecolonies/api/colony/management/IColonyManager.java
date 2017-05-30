package com.minecolonies.api.colony.management;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Objects that implement this interface function as
 */
public interface IColonyManager
{
    /**
     * Create a new Colony in the given world and at that location.
     *
     * @param w      World of the colony.
     * @param pos    Coordinate of the center of the colony.
     * @param player the player that creates the colony - owner.
     * @return The created colony.
     */
    @NotNull
    IColony createColony(@NotNull World w, BlockPos pos, @NotNull EntityPlayer player);

    /**
     * Specify that colonies should be saved.
     */
    void markDirty();

    /**
     * Indicates if this {@link IColonyManager} needs to be saved.
     * @return True when he needs to be saved, false when not.
     */
    boolean isDirty();

    /**
     * Delete a colony and kill all citizens/purge all buildings.
     *
     * @param id the colonies id.
     * @throws IllegalArgumentException when the given id is unknown.
     */
    void deleteColony(IToken id) throws IllegalArgumentException;

    /**
     * Get Colony by UUID.
     *
     * @param id ID of colony.
     * @return Colony with given ID, or null if she is unknown.
     */
    @Nullable
    IColony getColony(IToken id);

    /**
     * Syncs the achievements for all colonies.
     */
    void syncAllColoniesAchievements();

    /**
     * Get a AbstractBuilding by a World and coordinates.
     *
     * @param w   World.
     * @param pos Block position.
     * @return AbstractBuilding at the given location.
     */
    IBuilding getBuilding(@NotNull World w, @NotNull BlockPos pos);

    /**
     * Get colony that contains a given coordinate.
     *
     * @param w   World.
     * @param pos coordinates.
     * @return Colony at the given location.
     */
    IColony getColony(@NotNull World w, @NotNull BlockPos pos);

    /**
     * Get all colonies in this world.
     *
     * @param w World.
     * @return a list of colonies.
     */
    @NotNull
    ImmutableList<IColony> getColonies(@NotNull World w);

    /**
     * Get all colonies in all worlds.
     *
     * @return a list of colonies.
     */
    @NotNull
    ImmutableList<IColony> getColonies();

    /**
     * Side neutral method to get colony.
     * On clients it returns the view.
     * On servers it returns the colony itself.
     * {@link #getClosestColony(World, BlockPos)}
     *
     * @param w   World.
     * @param pos Block position.
     * @return View of colony or colony itself depending on side, closest to
     * coordinates.
     */
    @Nullable
    IColony getClosestColony(@NotNull World w, @NotNull BlockPos pos);

    /**
     * Side neutral method to get colony.
     * On clients it returns the view.
     * On servers it returns the colony itself.
     * <p>
     * Returns a colony or view with the given Player as owner.
     *
     * @param w     World.
     * @param owner Entity Player.
     * @return C belonging to specific player.
     */
    @Nullable
    IColony getColonyByOwner(@NotNull World w, @NotNull EntityPlayer owner);

    /**
     * Side neutral method to get colony.
     * On clients it returns the view.
     * On servers it returns the colony itself.
     * <p>
     * Returns a colony or view with given Player as owner.
     *
     * @param w     World
     * @param owner UUID of the owner.
     * @return C belonging to specific player.
     */
    @Nullable
    IColony getColonyByOwner(@NotNull World w, UUID owner);

    /**
     * Returns the minimum distance between two town halls, to not make colonies
     * collide.
     *
     * @return Minimum town hall distance.
     */
    int getMinimumDistanceBetweenTownHalls();

    /**
     * On server tick, tick every Colony.
     * NOTE: Review this for performance.
     *
     * @param event {@link net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent}
     */
    void onServerTick(@NotNull TickEvent.ServerTickEvent event);

    /**
     * Write colonies to NBT data for saving.
     *
     * @param compound NBT-Tag.
     */
    void writeToNBT(@NotNull NBTTagCompound compound);

    /**
     * On Client tick, clears views when player left.
     *
     * @param event {@link TickEvent.ClientTickEvent}.
     */
    void onClientTick(@NotNull TickEvent.ClientTickEvent event);

    /**
     * On world tick, tick every Colony in that world.
     * NOTE: Review this for performance.
     *
     * @param event {@link TickEvent.WorldTickEvent}.
     */
    void onWorldTick(@NotNull TickEvent.WorldTickEvent event);

    /**
     * When a world is loaded, Colonies in that world need to grab the reference
     * to the World. Additionally, when loading the first world, load all
     * colonies.
     *
     * @param world World.
     */
    void onWorldLoad(@NotNull World world);

    /**
     * Method called to backup the colony data.
     *
     * @return True when backup successfull, false when not.
     */
    boolean backupColonyData();

    /**
     * Read Colonies from saved NBT data.
     *
     * @param compound NBT Tag.
     */
    void readFromNBT(@NotNull NBTTagCompound compound);

    /**
     * Set the server UUID.
     *
     * @param uuid the universal unique id
     */
    void setServerUUID(UUID uuid);

    /**
     * Get the Universal Unique ID for the server.
     *
     * @return the server Universal Unique ID for ther
     */
    UUID getServerUUID();

    /**
     * Saves data when world is saved.
     *
     * @param world World.
     */
    void onWorldSave(@NotNull World world);

    /**
     * When a world unloads, all colonies in that world are informed.
     * Additionally, when the last world is unloaded, delete all colonies.
     *
     * @param world World.
     */
    void onWorldUnload(@NotNull World world);

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

    /**
     * Check if a given coordinate is inside any other colony.
     *
     * @param world the world to check in.
     * @param pos   the position to check.
     * @return true if a colony has been found.
     */
    boolean isCoordinateInAnyColony(@NotNull World world, BlockPos pos);
}
