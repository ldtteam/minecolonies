package com.minecolonies.api.colony;

import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHallView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.permissions.IPermissions;
import com.minecolonies.api.colony.permissions.Player;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.workorders.WorkOrderView;
import com.minecolonies.api.network.IMessage;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
    RegistryKey<World> getDimension();

    /**
     * Getter for the manual hiring or not.
     *
     * @return the boolean true or false.
     */
    boolean isManualHiring();

    /**
     * Sets if workers should be hired manually.
     *
     * @param manualHiring true if manually.
     */
    void setManualHiring(boolean manualHiring);

    /**
     * Getter for the manual housing or not.
     *
     * @return the boolean true or false.
     */
    boolean isManualHousing();

    /**
     * Sets if houses should be assigned manually.
     *
     * @param manualHousing true if manually.
     */
    void setManualHousing(boolean manualHousing);

    /**
     * Getter for letting citizens move in or not.
     *
     * @return the boolean true or false.
     */
    boolean canMoveIn();

    /**
     * Sets if citizens can move in.
     *
     * @param newMoveIn true if citizens can move in.
     */
    void setMoveIn(boolean newMoveIn);

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
    Map<UUID, Player> getPlayers();

    /**
     * Sets a specific permission to a rank. If the permission wasn't already set, it sends a message to the server.
     *
     * @param rank   Rank to get the permission.
     * @param action Permission to get.
     */
    void setPermission(Rank rank, @NotNull Action action);

    /**
     * removes a specific permission to a rank. If the permission was set, it sends a message to the server.
     *
     * @param rank   Rank to remove permission from.
     * @param action Action to remove permission of.
     */
    void removePermission(Rank rank, @NotNull Action action);

    /**
     * Toggles a specific permission to a rank. Sends a message to the server.
     *
     * @param rank   Rank to toggle permission of.
     * @param action Action to toggle permission of.
     */
    void togglePermission(Rank rank, @NotNull Action action);

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
    Collection<WorkOrderView> getWorkOrders();

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
     * @param buf               {@link PacketBuffer} to read from.
     * @param isNewSubscription Whether this is a new subscription of not.
     * @param world             the world it is in.
     * @return null == no response.
     */
    @Nullable
    IMessage handleColonyViewMessage(@NotNull PacketBuffer buf, @NotNull World world, boolean isNewSubscription);

    /**
     * Update permissions.
     *
     * @param buf buffer containing permissions.
     * @return null == no response
     */
    @Nullable
    IMessage handlePermissionsViewMessage(@NotNull PacketBuffer buf);

    /**
     * Update a ColonyView's workOrders given a network data ColonyView update packet. This uses a full-replacement - workOrders do not get updated and are instead overwritten.
     *
     * @param buf Network data.
     * @return null == no response.
     */
    @Nullable
    IMessage handleColonyViewWorkOrderMessage(PacketBuffer buf);

    /**
     * Update a ColonyView's citizens given a network data ColonyView update packet. This uses a full-replacement - citizens do not get updated and are instead overwritten.
     *
     * @param id  ID of the citizen.
     * @param buf Network data.
     * @return null == no response.
     */
    @Nullable
    IMessage handleColonyViewCitizensMessage(int id, PacketBuffer buf);

    /**
     * Handles visitor view messages
     *
     * @param refresh         whether to override old data
     * @param visitorViewData the new data to set
     */
    void handleColonyViewVisitorMessage(boolean refresh, Set<IVisitorViewData> visitorViewData);

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
    IMessage handleColonyBuildingViewMessage(BlockPos buildingId, @NotNull PacketBuffer buf);

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
     * Getter for the team colony color.
     *
     * @return the color.
     */
    TextFormatting getTeamColonyColor();

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
    boolean isCoordInColony(@NotNull World w, @NotNull BlockPos pos);

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
    ScorePlayerTeam getTeam();

    @Override
    int getLastContactInHours();

    @Override
    World getWorld();

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
    void removeVisitingPlayer(PlayerEntity player);

    @Override
    void addVisitingPlayer(PlayerEntity player);

    /**
     * Get a list of all barb spawn positions in the colony view.
     *
     * @return a copy of the list.
     */
    List<BlockPos> getLastSpawnPoints();

    /**
     * Get if progress should be printed.
     *
     * @return true if so.
     */
    boolean isPrintingProgress();

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
    String getStyle();

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
}
