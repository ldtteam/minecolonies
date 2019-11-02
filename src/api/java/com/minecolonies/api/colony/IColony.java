package com.minecolonies.api.colony;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.managers.interfaces.*;
import com.minecolonies.api.colony.permissions.IPermissions;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.workorders.IWorkManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Interface of the Colony and ColonyView which will have to implement the
 * following methods.
 */
public interface IColony
{

    void onWorldLoad(@NotNull World w);

    void onWorldUnload(@NotNull World w);

    void onServerTick(@NotNull TickEvent.ServerTickEvent event);

    @NotNull
    IWorkManager getWorkManager();

    HappinessData getHappinessData();

    void onWorldTick(@NotNull TickEvent.WorldTickEvent event);

    /**
     * Returns whether the colony chunks are loaded
     *
     * @return true when loaded.
     */
    boolean areAllColonyChunksLoaded();

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

    void setName(String n);

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
    int getID();

    /**
     * Check if the colony has a warehouse.
     *
     * @return true if so.
     */
    boolean hasWarehouse();

    /**
     * Get the last contact of a player to the colony in hours.
     *
     * @return an integer with a describing value.
     */
    int getLastContactInHours();

    /**
     * Method to get the World this colony is in.
     *
     * @return the World the colony is in.
     */
    World getWorld();

    /**
     * Get the current {@link IRequestManager} for this Colony.
     * Returns null if the current Colony does not support the request system.
     *
     * @return the {@link IRequestManager} for this colony, null if not supported.
     */
    @NotNull
    IRequestManager getRequestManager();

    /**
     * Get whether there will be a raid in this colony tonight, or not.
     *
     * @return Boolean value true if raid, false if not
     */
    boolean hasWillRaidTonight();

    /**
     * Called to mark this colony dirty, and in need of syncing / saving.
     */
    void markDirty();

    /**
     * Called to check if the colony can be deleted by an automatic cleanup.
     *
     * @return true if so.
     */
    boolean canBeAutoDeleted();

    /**
     * return whether or not a colony is allowed to have barbarian events triggered.
     *
     * @return true if so.
     */
    boolean isCanHaveBarbEvents();

    /**
     * return whether or not the colony has had it's "RaidTonight" calculated yet.
     *
     * @return true if so.
     */
    boolean isHasRaidBeenCalculated();

    /**
     * Method used to get a {@link IRequester} from a given Position. Is always a Building.
     *
     * @param pos The position to get the Building that acts as a requester.
     * @return The {@link IRequester} from the position, or null.
     */
    @Nullable
    IRequester getRequesterBuildingForPosition(@NotNull final BlockPos pos);

    /**
     * Remove a visiting player.
     * @param player the player.
     */
    void removeVisitingPlayer(final EntityPlayer player);

    /**
     * Get the players in the colony which should receive the message.
     *
     * @return list of players
     */
    @NotNull
    Set<EntityPlayer> getMessageEntityPlayers();

    void onBuildingUpgradeComplete(@Nullable IBuilding building, int level);

    @NotNull
    default List<BlockPos> getWayPoints(@NotNull BlockPos position, @NotNull BlockPos target)
    {
        final List<BlockPos> tempWayPoints = new ArrayList<>();
        tempWayPoints.addAll(getWayPoints().keySet());
        tempWayPoints.addAll(getBuildingManager().getBuildings().keySet());

        final double maxX = Math.max(position.getX(), target.getX());
        final double maxZ = Math.max(position.getZ(), target.getZ());

        final double minX = Math.min(position.getX(), target.getX());
        final double minZ = Math.min(position.getZ(), target.getZ());

        final Iterator<BlockPos> iterator = tempWayPoints.iterator();
        while (iterator.hasNext())
        {
            final BlockPos p = iterator.next();
            final int x = p.getX();
            final int z = p.getZ();
            if (x < minX || x > maxX || z < minZ || z > maxZ)
            {
                iterator.remove();
            }
        }

        return tempWayPoints;
    }

    double getOverallHappiness();

    Map<BlockPos, IBlockState> getWayPoints();

    String getStyle();

    void setStyle(String style);

    IBuildingManager getBuildingManager();

    ICitizenManager getCitizenManager();

    IColonyHappinessManager getColonyHappinessManager();

    IStatisticAchievementManager getStatsManager();

    IRaiderManager getRaiderManager();

    IColonyPackageManager getPackageManager();

    IProgressManager getProgressManager();

    /**
     * Add a visiting player.
     * @param player the player.
     */
    void addVisitingPlayer(final EntityPlayer player);

    /**
     * Get the colony dimension.
     * @return the dimension id.
     */
    int getDimension();

    /**
     * Check if the colony is on the server or client.
     * @return true if so.
     */
    boolean isRemote();

    /**
     * Save the time when mercenaries are used, to set a cooldown.
     */
    void usedMercenaries();

    /**
     * Get the last time mercenaries were used.
     */
    long getMercenaryUseTime();


    NBTTagCompound getColonyTag();

    int getNightsSinceLastRaid();

    void setNightsSinceLastRaid(int nights);

    boolean isNeedToMourn();

    void setNeedToMourn(boolean needToMourn, String name);

    boolean isMourning();

    boolean isColonyUnderAttack();

    boolean isValidAttackingPlayer(EntityPlayer entity);

    boolean isValidAttackingGuard(AbstractEntityCitizen entity);

    void setColonyColor(TextFormatting color);

    void setManualHousing(boolean manualHousing);

    void addWayPoint(BlockPos pos, IBlockState newWayPointState);

    void addGuardToAttackers(AbstractEntityCitizen entityCitizen, EntityPlayer followPlayer);

    void addFreePosition(BlockPos pos);

    void addFreeBlock(Block block);

    void removeFreePosition(BlockPos pos);

    void removeFreeBlock(Block block);

    void setCanBeAutoDeleted(boolean canBeDeleted);

    void setManualHiring(boolean manualHiring);

    NBTTagCompound writeToNBT(NBTTagCompound colonyCompound);

    void readFromNBT(NBTTagCompound compound);

    void setMoveIn(boolean newMoveIn);

    int getBoughtCitizenCost();

    void increaseBoughtCitizenCost();

    /**
     * Returns a set of players receiving important messages for the colony.
     * @return set of players.
     */
    @NotNull
    Set<EntityPlayer> getImportantMessageEntityPlayers();

    boolean isManualHiring();

    boolean isManualHousing();

    boolean canMoveIn();

    /**
     * Tries to use a given amount of additional growth-time for childs.
     * @param amount amount to use
     * @return true if used up.
     */
    boolean useAdditionalChildTime(int amount);

    /**
     * Sets whether the colony has a child.
     */
    void updateHasChilds();

    /**
     * Adds a loaded chunk to the colony list
     *
     * @param chunkPos chunk to add
     */
    void addLoadedChunk(long chunkPos);

    /**
     * Adds a chunk from the colony list
     *
     * @param chunkPos chunk to remove
     */
    void removeLoadedChunk(long chunkPos);

    /**
     * Returns the amount of loaded chunks
     *
     * @return amount of chunks
     */
    int getLoadedChunkCount();
}
