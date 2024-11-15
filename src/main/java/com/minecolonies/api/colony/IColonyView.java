package com.minecolonies.api.colony;

import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHallView;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.permissions.ColonyPlayer;
import com.minecolonies.api.colony.permissions.IPermissions;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.workorders.IWorkOrderView;
import com.minecolonies.api.network.IMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public interface IColonyView extends IColony
{
    /**
     * Get a copy of the freePositions list.
     *
     * @return the list of free to interact positions.
     */
    List<BlockPos> getFreePositions();

    /**
     * Get a copy of the freeBlocks list.
     *
     * @return the list of free to interact blocks.
     */
    List<Block> getFreeBlocks();

    /**
     * Add a new free to interact position.
     *
     * @param pos position to add.
     */
    void addFreePosition(@NotNull BlockPos pos);

    /**
     * Add a new free to interact block.
     *
     * @param block block to add.
     */
    void addFreeBlock(@NotNull Block block);

    /**
     * Remove a free to interact position.
     *
     * @param pos position to remove.
     */
    void removeFreePosition(@NotNull BlockPos pos);

    /**
     * Remove a free to interact block.
     *
     * @param block state to remove.
     */
    void removeFreeBlock(@NotNull Block block);

    /**
     * Returns the dimension ID of the view.
     *
     * @return dimension ID of the view.
     */
    ResourceKey<Level> getDimension();

    /**
     * Getter for the manual hiring or not.
     *
     * @return the boolean true or false.
     */
    boolean isManualHiring();

    /**
     * Getter for the manual housing or not.
     *
     * @return the boolean true or false.
     */
    boolean isManualHousing();

    /**
     * Getter for letting citizens move in or not.
     *
     * @return the boolean true or false.
     */
    boolean canMoveIn();

    /**
     * Get the town hall View for this ColonyView.
     *
     * @return {@link ITownHallView} of the colony.
     */
    @Nullable
    ITownHallView getTownHall();

    /**
     * Get a AbstractBuilding.View for a given building (by coordinate-id) using raw x,y,z.
     *
     * @param x x-coordinate.
     * @param y y-coordinate.
     * @param z z-coordinate.
     * @return {@link IBuildingView} of a AbstractBuilding for the given Coordinates/ID, or null.
     */
    IBuildingView getBuilding(int x, int y, int z);

    /**
     * Get a AbstractBuilding.View for a given building (by coordinate-id) using ChunkCoordinates.
     *
     * @param buildingId Coordinates/ID of the AbstractBuilding.
     * @return {@link IBuildingView} of a AbstractBuilding for the given Coordinates/ID, or null.
     */
    IBuildingView getBuilding(BlockPos buildingId);

    /**
     * Returns a map of players in the colony. Key is the UUID, value is {@link Player}
     *
     * @return Map of UUID's and {@link Player}
     */
    @NotNull
    Map<UUID, ColonyPlayer> getPlayers();

    /**
     * Returns the maximum amount of total citizen beds in the colony.
     *
     * @return maximum amount of citizens.
     */
    int getCitizenCount();

    /**
     * Returns the maximum amount of citizen slots in the colony considering beds and guard towers
     *
     * @return maximum amount of citizens.
     */
    int getCitizenCountLimit();

    /**
     * Getter for the citizens map.
     *
     * @return a unmodifiable Map of the citizen.
     */
    Map<Integer, ICitizenDataView> getCitizens();

    /**
     * Getter for the workOrders.
     *
     * @return a unmodifiable Collection of the workOrders.
     */
    Collection<IWorkOrderView> getWorkOrders();

    /**
     * Getter for a single workorder.
     *
     * @param id workorder id.
     * @return a specific workorder.
     */
    IWorkOrderView getWorkOrder(final int id);

    /**
     * Gets the CitizenDataView for a citizen id.
     *
     * @param id the citizen id.
     * @return CitizenDataView for the citizen.
     */
    ICitizenDataView getCitizen(int id);

    /**
     * Populate a ColonyView from the network data.
     *
     * @param buf               {@link FriendlyByteBuf} to read from.
     * @param isNewSubscription Whether this is a new subscription of not.
     * @param world             the world it is in.
     * @return null == no response.
     */
    @Nullable
    IMessage handleColonyViewMessage(@NotNull FriendlyByteBuf buf, @NotNull Level world, boolean isNewSubscription);

    /**
     * Update permissions.
     *
     * @param buf buffer containing permissions.
     * @return null == no response
     */
    @Nullable
    IMessage handlePermissionsViewMessage(@NotNull FriendlyByteBuf buf);

    /**
     * Update a ColonyView's workOrders given a network data ColonyView update packet. This uses a full-replacement - workOrders do not get updated and are instead overwritten.
     *
     * @param buf Network data.
     * @return null == no response.
     */
    @Nullable
    IMessage handleColonyViewWorkOrderMessage(FriendlyByteBuf buf);

    /**
     * Update a ColonyView's citizens given a network data ColonyView update packet. This uses a full-replacement - citizens do not get updated and are instead overwritten.
     *
     * @param id  ID of the citizen.
     * @param buf Network data.
     * @return null == no response.
     */
    @Nullable
    IMessage handleColonyViewCitizensMessage(int id, FriendlyByteBuf buf);

    /**
     * Handles visitor view messages
     * @param refresh if all need to be refreshed.
     * @param visitorViewData the new data to set
     */
    void handleColonyViewVisitorMessage(final FriendlyByteBuf visitorViewData, final boolean refresh);

    /**
     * Remove a citizen from the ColonyView.
     *
     * @param citizen citizen ID.
     * @return null == no response.
     */
    @Nullable
    IMessage handleColonyViewRemoveCitizenMessage(int citizen);

    /**
     * Remove a building from the ColonyView.
     *
     * @param buildingId location of the building.
     * @return null == no response.
     */
    @Nullable
    IMessage handleColonyViewRemoveBuildingMessage(BlockPos buildingId);

    /**
     * Remove a workOrder from the ColonyView.
     *
     * @param workOrderId id of the workOrder.
     * @return null == no response
     */
    @Nullable
    IMessage handleColonyViewRemoveWorkOrderMessage(int workOrderId);

    /**
     * Update a ColonyView's buildings given a network data ColonyView update packet. This uses a full-replacement - buildings do not get updated and are instead overwritten.
     *
     * @param buildingId location of the building.
     * @param buf        buffer containing ColonyBuilding information.
     * @return null == no response.
     */
    @Nullable
    IMessage handleColonyBuildingViewMessage(BlockPos buildingId, @NotNull FriendlyByteBuf buf);

    /**
     * Handle the colony view research manager updating.
     * @param compoundTag the tag to update the research manager with.
     */
    void handleColonyViewResearchManagerUpdate(CompoundTag compoundTag);

    /**
     * Update all field instances in the colony view.
     *
     * @param fields the list of fields.
     */
    void handleColonyFieldViewUpdateMessage(Set<IField> fields);

    /**
     * Get all fields.
     *
     * @param matcher the field matcher predicate.
     * @return a collection of fields.
     */
    @NotNull List<IField> getFields(Predicate<IField> matcher);

    /**
     * Get a specific field.
     *
     * @param matcher the field matcher predicate.
     * @return a field instance, or null.
     */
    @Nullable IField getField(Predicate<IField> matcher);

    /**
     * Update a players permissions.
     *
     * @param player player username.
     */
    void addPlayer(String player);

    /**
     * Remove player from colony permissions.
     *
     * @param player the UUID of the player to remove.
     */
    void removePlayer(UUID player);

    /**
     * Getter for the overall happiness.
     *
     * @return the happiness, a double.
     */
    double getOverallHappiness();

    @Override
    BlockPos getCenter();

    @Override
    String getName();

    /**
     * Sets the name of the view.
     *
     * @param name Name of the view.
     */
    void setName(String name);

    @NotNull
    @Override
    IPermissions getPermissions();

    @Override
    boolean isCoordInColony(@NotNull Level w, @NotNull BlockPos pos);

    @Override
    long getDistanceSquared(@NotNull BlockPos pos);

    @Override
    boolean hasTownHall();

    /**
     * Returns the ID of the view.
     *
     * @return ID of the view.
     */
    @Override
    int getID();

    @Override
    boolean hasWarehouse();

    @Override
    int getLastContactInHours();

    @Override
    Level getWorld();

    @NotNull
    @Override
    IRequestManager getRequestManager();

    @Override
    void markDirty();

    @Override
    boolean canBeAutoDeleted();

    @Nullable
    @Override
    IRequester getRequesterBuildingForPosition(@NotNull BlockPos pos);

    @Override
    void removeVisitingPlayer(Player player);

    @Override
    void addVisitingPlayer(Player player);

    /**
     * Get a list of all barb spawn positions in the colony view.
     *
     * @return a copy of the list.
     */
    List<BlockPos> getLastSpawnPoints();

    @Override
    boolean isRemote();

    /**
     * Get a list of all buildings.
     *
     * @return a list of their views.
     */
    List<IBuildingView> getBuildings();

    /**
     * Get the style of the colony.
     *
     * @return the current default style.
     */
    String getStructurePack();

    /**
     * If currently being raided.
     *
     * @return true if so.
     */
    boolean isRaiding();

    /**
     * Get a compact list of all allies.
     *
     * @return the list.
     */
    List<CompactColonyReference> getAllies();

    /**
     * Get a compact list of all feuds.
     *
     * @return the list.
     */
    List<CompactColonyReference> getFeuds();

    boolean areSpiesEnabled();

    /**
     * Gets the data view for a visitor
     *
     * @param citizenId id to query
     * @return citizen data for visitor
     */
    ICitizenDataView getVisitor(int citizenId);

    /**
     * Get a list of all available citizen name style options.
     * @return the list of options.
     */
    List<String> getNameFileIds();
}
