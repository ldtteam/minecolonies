package com.minecolonies.api.colony;

import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.handlers.ICombiningColonyEventHandler;
import com.minecolonies.api.colony.permissions.IPermissions;
import com.minecolonies.api.colony.requestsystem.IRequestManager;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.entity.ai.citizen.farmer.Field;
import com.minecolonies.api.entity.ai.citizen.farmer.IScarecrow;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.Achievement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface of the Colony and ColonyView which will have to implement the
 * following methods.
 */
public interface IColony<B extends IBuilding> extends ICombiningColonyEventHandler
{
    /**
     * Spawn a brand new Citizen.
     */
    void spawnCitizen();

    /**
     * Returns the position of the colony.
     *
     * @return pos of the colony.
     */
    BlockPos getCenter();

    /**
     * Returns the name of the colony.
     *
     * @return Name of the colony.
     */
    String getName();

    /**
     * Sets the name of the colony.
     * Marks dirty.
     *
     * @param n new name.
     */
    void setName(String n);

    /**
     * Marks the instance dirty.
     */
    void markDirty();

    /**
     * Returns the permissions of the colony.
     *
     * @return {@link IPermissions} of the colony.
     */
    IPermissions getPermissions();

    /**
     * Determine if a given chunk coordinate is considered to be within the
     * colony's bounds.
     *
     * @param w   World to check.
     * @param pos Block Position.
     * @return True if inside colony, otherwise false.
     */
    boolean isCoordInColony(World w, BlockPos pos);

    /**
     * Returns the squared (x, z) distance to the center.
     *
     * @param pos Block Position.
     * @return Squared distance to the center in (x, z) direction.
     */
    long getDistanceSquared(BlockPos pos);

    /**
     * Returns whether or not the colony has a town hall.
     *
     * @return whether or not the colony has a town hall.
     */
    boolean hasTownHall();

    /**
     * returns this colonies unique id.
     *
     * @return an int representing the id.
     */
    IToken getID();

    /**
     * Read colony from saved data.
     *
     * @param compound compound to read from.
     */
    void readFromNBT(@NotNull NBTTagCompound compound);

    /**
     * Add a AbstractBuilding to the Colony.
     *
     * @param building AbstractBuilding to add to the colony.
     */
    void addBuilding(@NotNull B building);

    /**
     * Add a Building to the Colony.
     *
     * @param field Field to add to the colony.
     */
    void addField(@NotNull Field field);

    /**
     * Write colony to save data.
     *
     * @param compound compound to write to.
     */
    void writeToNBT(@NotNull NBTTagCompound compound);

    /**
     * Returns the dimension ID.
     *
     * @return Dimension ID.
     */
    int getDimension();

    /**
     * Increment the statistic amount and trigger achievement.
     *
     * @param statistic the statistic.
     */
    void incrementStatistic(@NotNull String statistic);

    /**
     * Get the amount of statistic.
     *
     * @param statistic the statistic.
     * @return amount of statistic.
     */
    int getStatisticAmount(@NotNull String statistic);

    /**
     * increment statistic amount.
     *
     * @param statistic the statistic.
     */
    void incrementStatisticAmount(@NotNull String statistic);

    /**
     * Marks building data dirty.
     */
    void markBuildingsDirty();

    /**
     * Update Subscribers with Colony, Citizen, and AbstractBuilding Views.
     */
    void updateSubscribers();

    void sendColonyViewPackets(@NotNull Set<EntityPlayerMP> oldSubscribers, boolean hasNewSubscribers);

    /**
     * Sends packages to update the permissions.
     *
     * @param oldSubscribers    the existing subscribers.
     * @param hasNewSubscribers the new subscribers.
     */
    void sendPermissionsPackets(@NotNull Set<EntityPlayerMP> oldSubscribers, boolean hasNewSubscribers);

    /**
     * Sends packages to update the workOrders.
     *
     * @param oldSubscribers    the existing subscribers.
     * @param hasNewSubscribers the new subscribers.
     */
    void sendWorkOrderPackets(@NotNull Set<EntityPlayerMP> oldSubscribers, boolean hasNewSubscribers);

    /**
     * Sends packages to update the citizens.
     *
     * @param oldSubscribers    the existing subscribers.
     * @param hasNewSubscribers the new subscribers.
     */
    void sendCitizenPackets(@NotNull Set<EntityPlayerMP> oldSubscribers, boolean hasNewSubscribers);

    /**
     * Sends packages to update the buildings.
     *
     * @param oldSubscribers    the existing subscribers.
     * @param hasNewSubscribers the new subscribers.
     */
    void sendBuildingPackets(@NotNull Set<EntityPlayerMP> oldSubscribers, boolean hasNewSubscribers);

    /**
     * Sends packages to update the schematics.
     *
     * @param hasNewSubscribers the new subscribers.
     */
    void sendSchematicsPackets(boolean hasNewSubscribers);

    /**
     * Sends packages to update the fields.
     *
     * @param hasNewSubscribers the new subscribers.
     */
    void sendFieldPackets(boolean hasNewSubscribers);

    /**
     * Get the Work Manager for the Colony.
     *
     * @return WorkManager for the Colony.
     */
    @NotNull
    IWorkManager getWorkManager();

    /**
     * Get a copy of the freePositions list.
     *
     * @return the list of free to interact positions.
     */
    Set<BlockPos> getFreePositions();

    /**
     * Get a copy of the freeBlocks list.
     *
     * @return the list of free to interact blocks.
     */
    Set<Block> getFreeBlocks();

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

    void updateOverallHappiness();

    /**
     * Update the waypoints after worldTicks.
     */
    void updateWayPoints();

    /**
     * returns the World the colony is in.
     *
     * @return the World the colony is in.
     */
    @Nullable
    World getWorld();

    /**
     * Updates all subscribers of fields etc.
     */
    void markFieldsDirty();

    /**
     * Spawn a citizen with specific citizen data.
     *
     * @param data Data to use to spawn citizen.
     */
    void spawnCitizen(ICitizenData data);

    /**
     * Returns the max amount of citizens in the colony.
     *
     * @return Max amount of citizens.
     */
    int getMaxCitizens();

    /**
     * Returns a map of citizens in the colony.
     * The map has ID as key, and citizen data as value.
     *
     * @return Map of citizens in the colony, with as key the citizen ID, and as
     * value the citizen data.
     */
    @NotNull
    Map<Integer, ICitizenData> getCitizens();

    @NotNull
    List<EntityPlayer> getMessageEntityPlayers();

    /**
     * Checks if the achievements are valid.
     */
    void checkAchievements();

    /**
     * Marks citizen data dirty.
     */
    void markCitizensDirty();

    /**
     * Triggers an achievement on this colony.
     * <p>
     * Will automatically sync to all players.
     *
     * @param achievement The achievement to trigger
     */
    void triggerAchievement(@NotNull Achievement achievement);

    /**
     * Spawn citizen if his entity is null.
     *
     * @param data his data
     */
    void spawnCitizenIfNull(@NotNull ICitizenData data);

    /**
     * Gets the town hall of the colony.
     *
     * @return Town hall of the colony.
     */
    @Nullable
    B getTownHall();

    /**
     * Getter of a unmodifiable version of the farmerFields map.
     *
     * @return map of fields and their id.
     */
    @NotNull
    Map<BlockPos, Field> getFields();

    /**
     * Get field in Colony by ID.
     *
     * @param fieldId ID (coordinates) of the field to get.
     * @return field belonging to the given ID.
     */
    Field getField(BlockPos fieldId);

    /**
     * Returns a field which has not been taken yet.
     *
     * @param owner name of the owner of the field.
     * @return a field if there is one available, else null.
     */
    @Nullable
    Field getFreeField(String owner);

    B getBuilding(BlockPos pos);

    /**
     * Get citizen by ID.
     *
     * @param citizenId ID of the Citizen.
     * @return CitizenData associated with the ID, or null if it was not found.
     */
    ICitizenData getCitizen(int citizenId);

    /**
     * Get building in Colony by ID. The building will be casted to the provided
     * type.
     *
     * @param buildingId ID (coordinates) of the building to get.
     * @param type       Type of building.
     * @param <S>        Building class.
     * @return the building with the specified id.
     */
    @Nullable
    <S extends B> S getBuilding(BlockPos buildingId, @NotNull Class<S> type);

    /**
     * Creates a field from a tile entity and adds it to the colony.
     *
     * @param tileEntity      the scarecrow which contains the inventory.
     * @param inventoryPlayer the inventory of the player.
     * @param pos             Position where the field has been placed.
     * @param world           the world of the field.
     */
    void addNewField(IScarecrow tileEntity, InventoryPlayer inventoryPlayer, BlockPos pos, World world);

    /**
     * Creates a building from a tile entity and adds it to the colony.
     *
     * @param tileEntity Tile entity to build a building from.
     * @return AbstractBuilding that was created and added.
     */
    @Nullable
    B addNewBuilding(@NotNull TileEntityColonyBuilding tileEntity);

    /**
     * Recalculates how many citizen can be in the colony.
     */
    void calculateMaxCitizens();

    /**
     * Remove a AbstractBuilding from the Colony (when it is destroyed).
     *
     * @param building AbstractBuilding to remove.
     */
    void removeBuilding(@NotNull B building);

    /**
     * Getter which checks if jobs should be manually allocated.
     *
     * @return true of false.
     */
    boolean isManualHiring();

    /**
     * Setter to set the job allocation manual or automatic.
     *
     * @param manualHiring true if manual, false if automatic.
     */
    void setManualHiring(boolean manualHiring);

    /**
     * Removes a citizen from the colony.
     *
     * @param citizen Citizen data to remove.
     */
    void removeCitizen(@NotNull ICitizenData citizen);

    /**
     * Send the message of a removed workOrder to the client.
     *
     * @param orderId the workOrder to remove.
     */
    void removeWorkOrder(int orderId);

    /**
     * Get the first unemployed citizen.
     *
     * @return Citizen with no current job.
     */
    @Nullable
    ICitizenData getJoblessCitizen();

    List<BlockPos> getDeliverymanRequired();

    /**
     * Performed when a building of this colony finished his upgrade state.
     *
     * @param building The upgraded building.
     * @param level    The new level.
     */
    void onBuildingUpgradeComplete(@NotNull IBuilding building, int level);

    /**
     * Method to get the achievements of this colony.
     *
     * @return The achievements achieved by this colony.
     */
    @NotNull
    List<Achievement> getAchievements();

    /**
     * Removes a field from the farmerFields list.
     *
     * @param pos the position-id.
     */
    void removeField(BlockPos pos);

    /**
     * Adds a waypoint to the colony.
     *
     * @param point the waypoint to add.
     * @param block the block at the waypoint.
     */
    void addWayPoint(BlockPos point, IBlockState block);

    /**
     * Returns a list of all wayPoints of the colony.
     *
     * @param position start position.
     * @param target   end position.
     * @return list of wayPoints.
     */
    @NotNull
    List<BlockPos> getWayPoints(@NotNull BlockPos position, @NotNull BlockPos target);

    /**
     * Getter for overall happiness.
     *
     * @return the overall happiness.
     */
    double getOverallHappiness();

    /**
     * Increase the overall happiness by an amount, cap at max.
     *
     * @param amount the amount.
     */
    void increaseOverallHappiness(double amount);

    /**
     * Decrease the overall happiness by an amount, cap at min.
     *
     * @param amount the amount.
     */
    void decreaseOverallHappiness(double amount);

    /**
     * Returns the buildings in the colony
     *
     * @return The buildings in the colony
     */
    @NotNull
    ImmutableMap<BlockPos, IBuilding> getBuildings();

    /**
     * Returns the request manager for the colony.
     * @return The request manager.
     */
    @NotNull
    IRequestManager getRequestManager();

    /**
     * Method to get the factory controller for a given colony
     *
     * @return The factory controller.
     */
    @NotNull
    IFactoryController getFactoryController();

    /**
     * Is called when the colony is deleted. Should:
     *      * remove all citizens
     *      * destroy all buildings
     */
    void OnDeletion();
}
