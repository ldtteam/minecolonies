package com.minecolonies.api.colony;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.compatibility.ICompatibilityManager;
import com.minecolonies.api.crafting.IRecipeManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface IColonyManager
{

    static IColonyManager getInstance()
    {
        return IMinecoloniesAPI.getInstance().getColonyManager();
    }

    /**
     * Create a new Colony in the given world and at that location.
     *
     * @param w      World of the colony.
     * @param pos    Coordinate of the center of the colony.
     * @param player the player that creates the colony - owner.
     * @param style  the default style of the colony.
     */
    void createColony(@NotNull World w, BlockPos pos, @NotNull PlayerEntity player, @NotNull String style);

    /**
     * Delete the colony in a world.
     *
     * @param id         the id of it.
     * @param canDestroy if can destroy the buildings.
     * @param world      the world.
     */
    void deleteColonyByWorld(int id, boolean canDestroy, World world);

    /**
     * Delete the colony by dimension.
     *
     * @param id         the id of it.
     * @param canDestroy if can destroy the buildings.
     * @param dimension  the dimension.
     */
    void deleteColonyByDimension(int id, boolean canDestroy, int dimension);

    /**
     * Get Colony by UUID.
     *
     * @param id ID of colony.
     * @return Colony with given ID.
     */
    @Nullable
    IColony getColonyByWorld(int id, World world);

    /**
     * Get Colony by UUID.
     *
     * @param id ID of colony.
     * @return Colony with given ID.
     */
    @Nullable
    IColony getColonyByDimension(int id, int dimension);

    /**
     * Get a AbstractBuilding by a World and coordinates.
     *
     * @param w   World.
     * @param pos Block position.
     * @return AbstractBuilding at the given location.
     */
    IBuilding getBuilding(@NotNull World w, @NotNull BlockPos pos);

    /**
     * Get colony that contains a given coordinate from world.
     *
     * @param w   World.
     * @param pos coordinates.
     * @return Colony at the given location.
     */
    IColony getColonyByPosFromWorld(@NotNull World w, @NotNull BlockPos pos);

    /**
     * Get colony that contains a given coordinate from dimension.
     *
     * @param dim the dimension.
     * @param pos coordinates.
     * @return Colony at the given location.
     */
    IColony getColonyByPosFromDim(int dim, @NotNull BlockPos pos);

    /**
     * Check if a position is too close to another colony to found a new colony.
     *
     * @param w   World.
     * @param pos coordinates.
     * @return true if so.
     */
    boolean isTooCloseToColony(@NotNull World w, @NotNull BlockPos pos);

    /**
     * Get all colonies in this world.
     *
     * @param w World.
     * @return a list of colonies.
     */
    @NotNull
    List<IColony> getColonies(@NotNull World w);

    /**
     * Get all colonies in all worlds.
     *
     * @return a list of colonies.
     */
    @NotNull
    List<IColony> getAllColonies();

    /**
     * Get all colonies in all worlds.
     *
     * @param abandonedSince time in hours since the last contact.
     * @return a list of colonies.
     */
    @NotNull
    List<IColony> getColoniesAbandonedSince(int abandonedSince);

    /**
     * Get a AbstractBuilding by position.
     *
     * @param pos Block position.
     * @return Returns the view belonging to the building at (x, y, z).
     */
    IBuildingView getBuildingView(int dimension, BlockPos pos);

    /**
     * Side neutral method to get colony.
     * On clients it returns the view.
     * On servers it returns the colony itself.
     *
     * @param w   World.
     * @param pos coordinates.
     * @return View of colony or colony itself depending on side.
     */
    @Nullable
    IColony getIColony(@NotNull World w, @NotNull BlockPos pos);

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
    IColony getClosestIColony(@NotNull World w, @NotNull BlockPos pos);

    /**
     * Returns the closest view {@link #getColonyView(int, int)}.
     *
     * @param w   World.
     * @param pos Block Position.
     * @return View of the closest colony.
     */
    @Nullable
    IColonyView getClosestColonyView(@Nullable World w, @Nullable BlockPos pos);

    /**
     * Get closest colony by x,y,z.
     *
     * @param w   World.
     * @param pos coordinates.
     * @return Colony closest to coordinates.
     */
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
     * @return IColony belonging to specific player.
     */
    @Nullable
    IColony getIColonyByOwner(@NotNull World w, @NotNull PlayerEntity owner);

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
     * @param event {@link net.minecraftforge.event.TickEvent.ServerTickEvent}
     */
    void onServerTick(@NotNull TickEvent.ServerTickEvent event);

    /**
     * Write colonies to NBT data for saving.
     *
     * @param compound NBT-Tag.
     */
    void write(@NotNull CompoundNBT compound);

    /**
     * Read Colonies from saved NBT data.
     *
     * @param compound NBT Tag.
     */
    void read(@NotNull CompoundNBT compound);

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
     * to the World. Additionally, when loading the first world, load the manager data.
     *
     * @param world World.
     */
    void onWorldLoad(@NotNull World world);

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
     * @param colonyData        {@link PacketBuffer} with colony data.
     * @param isNewSubscription whether this is a new subscription or not.
     * @param dim               the dimension.
     */
    void handleColonyViewMessage(int colonyId, @NotNull PacketBuffer colonyData, @NotNull World world, boolean isNewSubscription, int dim);

    /**
     * Get IColonyView by ID.
     *
     * @param id        ID of colony.
     * @param dimension the dimension id.
     * @return The IColonyView belonging to the colony.
     */
    IColonyView getColonyView(int id, int dimension);

    /**
     * Returns result of {@link IColonyView#handlePermissionsViewMessage(PacketBuffer)}
     * if {@link #getColonyView(int, int)}. gives a not-null result. If {@link
     * #getColonyView(int, int)} is null, returns null.
     *
     * @param colonyID ID of the colony.
     * @param data     {@link PacketBuffer} with colony data.
     * @param dim      the dimension.
     */
    void handlePermissionsViewMessage(int colonyID, @NotNull PacketBuffer data, int dim);

    /**
     * Returns result of {@link IColonyView#handleColonyViewCitizensMessage(int,
     * PacketBuffer)} if {@link #getColonyView(int, int)} gives a not-null result. If
     * {@link #getColonyView(int, int)} is null, returns null.
     *
     * @param colonyId  ID of the colony.
     * @param citizenId ID of the citizen.
     * @param buf       {@link PacketBuffer} with colony data.
     * @param dim       the dimension.
     */
    void handleColonyViewCitizensMessage(int colonyId, int citizenId, PacketBuffer buf, int dim);

    /**
     * Returns result of {@link IColonyView#handleColonyViewWorkOrderMessage(PacketBuffer)}
     * (int, ByteBuf)} if {@link #getColonyView(int, int)} gives a not-null result.
     * If {@link #getColonyView(int, int)} is null, returns null.
     *
     * @param colonyId ID of the colony.
     * @param buf      {@link PacketBuffer} with colony data.
     * @param dim      the dimension.
     */
    void handleColonyViewWorkOrderMessage(int colonyId, PacketBuffer buf, int dim);

    /**
     * Returns result of {@link IColonyView#handleColonyViewRemoveCitizenMessage(int)}
     * if {@link #getColonyView(int, int)} gives a not-null result. If {@link
     * #getColonyView(int, int)} is null, returns null.
     *
     * @param colonyId  ID of the colony.
     * @param citizenId ID of the citizen.
     * @param dim       the dimension.
     */
    void handleColonyViewRemoveCitizenMessage(int colonyId, int citizenId, int dim);

    /**
     * Returns result of {@link IColonyView#handleColonyBuildingViewMessage(BlockPos,
     * PacketBuffer)} if {@link #getColonyView(int, int)} gives a not-null result. If
     * {@link #getColonyView(int, int)} is null, returns null.
     *
     * @param colonyId   ID of the colony.
     * @param buildingId ID of the building.
     * @param buf        {@link PacketBuffer} with colony data.
     * @param dim        the dimension.
     */
    void handleColonyBuildingViewMessage(int colonyId, BlockPos buildingId, @NotNull PacketBuffer buf, int dim);

    /**
     * Returns result of {@link IColonyView#handleColonyViewRemoveBuildingMessage(BlockPos)}
     * if {@link #getColonyView(int, int)} gives a not-null result. If {@link
     * #getColonyView(int, int)} is null, returns null.
     *
     * @param colonyId   ID of the colony.
     * @param buildingId ID of the building.
     * @param dim        the dimension.
     */
    void handleColonyViewRemoveBuildingMessage(int colonyId, BlockPos buildingId, int dim);

    /**
     * Returns result of {@link IColonyView#handleColonyViewRemoveWorkOrderMessage(int)}
     * if {@link #getColonyView(int, int)} gives a not-null result. If {@link
     * #getColonyView(int, int)} is null, returns null.
     *
     * @param colonyId    ID of the colony.
     * @param workOrderId ID of the workOrder.
     * @param dim         the dimension.
     */
    void handleColonyViewRemoveWorkOrderMessage(int colonyId, int workOrderId, int dim);

    /**
     * Handle a message about the hapiness.
     * if {@link #getColonyView(int, int)} gives a not-null result. If {@link
     * #getColonyView(int, int)} is null, returns null.
     *
     * @param colonyId Id of the colony.
     * @param data     Datas about the hapiness
     * @param dim      the dimension.
     */
    void handleHappinessDataMessage(int colonyId, HappinessData data, int dim);

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

    /**
     * Get an instance of the compatibilityManager.
     *
     * @return the manager.
     */
    ICompatibilityManager getCompatibilityManager();

    /**
     * Getter for the recipeManager.
     *
     * @return an IRecipeManager.
     */
    IRecipeManager getRecipeManager();

    /**
     * Get the top colony id of all colonies.
     *
     * @return the top id.
     */
    int getTopColonyId();

    /**
     * Reset all colony views on login to new world.
     */
    void resetColonyViews();
}
