package com.minecolonies.api.colony;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * ----------------------- Not Documented Object ---------------------
 * TODO: Document Object
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
     * Delete a colony and kill all citizens/purge all buildings.
     *
     * @param id the colonies id.
     */
    void deleteColony(int id);

    /**
     * Get Colony by UUID.
     *
     * @param id ID of colony.
     * @return Colony with given ID.
     */
    IColony getColony(int id);

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
    IBakedModel getBuilding(@NotNull World w, @NotNull BlockPos pos);

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

    /*

    TODO: Rewrite this so that it is not side dependent.
    The Colony manager for each side should determine this on his own.

     */
    /**
     * Get a AbstractBuilding by position.
     *
     * @param pos Block position.
     * @return Returns the view belonging to the building at (x, y, z).
     *
    AbstractBuilding.View getBuildingView(BlockPos pos);
     */

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

    /*

    TODO: Again rewrite this method to be side independent. Have the colony manager for the specific side return either the view or
    TODO: the actuall implementation.

    **
     * Returns the closest view {@link #getColonyView(World, BlockPos)}.
     *
     * @param w   World.
     * @param pos Block Position.
     * @return View of the closest colony.
     *
    @Nullable
    ColonyView getClosestColonyView(@NotNull World w, @NotNull BlockPos pos);

*/

    /**
     * Side neutral method to get colony.
     * On clients it returns the view.
     * On servers it returns the colony itself.
     * <p>
     * Returns a colony or view with the given Player as owner.
     *
     * @param w     World.
     * @param owner Entity Player.
     * @return IColony belonging to specific player.
     */
    @Nullable
    IColony getIColonyByOwner(@NotNull World w, @NotNull EntityPlayer owner);

    /**
     * Side neutral method to get colony.
     * On clients it returns the view.
     * On servers it returns the colony itself.
     * <p>
     * Returns a colony or view with given Player as owner.
     *
     * @param w     World
     * @param owner UUID of the owner.
     * @return IColony belonging to specific player.
     */
    @Nullable
    IColony getIColonyByOwner(@NotNull World w, UUID owner);

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
     * Sends view message to the right view.
     *
     * @param colonyId          ID of the colony.
     * @param colonyData        {@link ByteBuf} with colony data.
     * @param isNewSubscription whether this is a new subscription or not.
     * @return the response message.
     */
    @Nullable
    IMessage handleColonyViewMessage(int colonyId, @NotNull ByteBuf colonyData, boolean isNewSubscription);

    /**
     * Returns result of {@link ColonyView#handlePermissionsViewMessage(ByteBuf)}
     * if {@link #getColonyView(int)}. gives a not-null result. If {@link
     * #getColonyView(int)} is null, returns null.
     *
     * @param colonyID ID of the colony.
     * @param data     {@link ByteBuf} with colony data.
     * @return result of {@link ColonyView#handlePermissionsViewMessage(ByteBuf)}
     * or null.
     */
    IMessage handlePermissionsViewMessage(int colonyID, @NotNull ByteBuf data);

    /**
     * Returns result of {@link ColonyView#handleColonyViewCitizensMessage(int,
     * ByteBuf)} if {@link #getColonyView(int)} gives a not-null result. If
     * {@link #getColonyView(int)} is null, returns null.
     *
     * @param colonyId  ID of the colony.
     * @param citizenId ID of the citizen.
     * @param buf       {@link ByteBuf} with colony data.
     * @return result of {@link ColonyView#handleColonyViewCitizensMessage(int,
     * ByteBuf)} or null.
     */
    IMessage handleColonyViewCitizensMessage(int colonyId, int citizenId, ByteBuf buf);

    /**
     * Returns result of {@link ColonyView#handleColonyViewWorkOrderMessage(ByteBuf)}
     * (int, ByteBuf)} if {@link #getColonyView(int)} gives a not-null result.
     * If {@link #getColonyView(int)} is null, returns null.
     *
     * @param colonyId ID of the colony.
     * @param buf      {@link ByteBuf} with colony data.
     * @return result of {@link ColonyView#handleColonyViewWorkOrderMessage(ByteBuf)}
     * or null.
     */
    IMessage handleColonyViewWorkOrderMessage(int colonyId, ByteBuf buf);

    /**
     * Returns result of {@link ColonyView#handleColonyViewRemoveCitizenMessage(int)}
     * if {@link #getColonyView(int)} gives a not-null result. If {@link
     * #getColonyView(int)} is null, returns null.
     *
     * @param colonyId  ID of the colony.
     * @param citizenId ID of the citizen.
     * @return result of {@link ColonyView#handleColonyViewRemoveCitizenMessage(int)}
     * or null.
     */
    IMessage handleColonyViewRemoveCitizenMessage(int colonyId, int citizenId);

    /**
     * Returns result of {@link ColonyView#handleColonyBuildingViewMessage(BlockPos, IToken, ByteBuf)} if {@link #getColonyView(int)} gives a not-null result.
     * If {@link #getColonyView(int)} is null, returns null.
     *
     * @param colonyId         ID of the colony.
     * @param buildingLocation The location of the building.
     * @param buildingId       ID of the building.
     * @param buf              {@link ByteBuf} with colony data.
     * @return result of {@link ColonyView#handleColonyBuildingViewMessage(BlockPos, IToken, ByteBuf)} or null.
     */
    IMessage handleColonyBuildingViewMessage(
                                              int colonyId,
                                              BlockPos buildingLocation,
                                              IToken buildingId,
                                              @NotNull ByteBuf buf);

    /**
     * Returns result of {@link ColonyView#handleColonyViewRemoveBuildingMessage(BlockPos)}
     * if {@link #getColonyView(int)} gives a not-null result. If {@link
     * #getColonyView(int)} is null, returns null.
     *
     * @param colonyId   ID of the colony.
     * @param buildingId ID of the building.
     * @return result of {@link ColonyView#handleColonyViewRemoveBuildingMessage(BlockPos)}
     * or null.
     */
    IMessage handleColonyViewRemoveBuildingMessage(int colonyId, BlockPos buildingId);

    /**
     * Returns result of {@link ColonyView#handleColonyViewRemoveWorkOrderMessage(int)}
     * if {@link #getColonyView(int)} gives a not-null result. If {@link
     * #getColonyView(int)} is null, returns null.
     *
     * @param colonyId    ID of the colony.
     * @param workOrderId ID of the workOrder.
     * @return result of {@link ColonyView#handleColonyViewRemoveWorkOrderMessage(int)}
     * or null.
     */
    IMessage handleColonyViewRemoveWorkOrderMessage(int colonyId, int workOrderId);

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
