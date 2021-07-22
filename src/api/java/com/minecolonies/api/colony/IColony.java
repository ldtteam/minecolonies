package com.minecolonies.api.colony;

import com.minecolonies.api.colony.managers.interfaces.*;
import com.minecolonies.api.colony.permissions.IPermissions;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.workorders.IWorkManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.research.IResearchManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.util.constant.ColonyConstants.TEAM_COLONY_NAME;

/**
 * Interface of the Colony and ColonyView which will have to implement the following methods.
 */
public interface IColony
{

    void onWorldLoad(@NotNull World w);

    void onWorldUnload(@NotNull World w);

    void onServerTick(@NotNull TickEvent.ServerTickEvent event);

    @NotNull
    IWorkManager getWorkManager();

    void onWorldTick(@NotNull TickEvent.WorldTickEvent event);

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
     * Determine if a given chunk coordinate is considered to be within the colony's bounds.
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
     * Check if the colony has a building type at a specific level or higher.
     * @param building       The string identifier for the building, based on schematic name.
     * @param level          The level requirement.
     * @param singleBuilding If true, requires that a single building meet the minimum requirement.
     * @return true if at least one building of at least the target level is present.
     */
    boolean hasBuilding(final String building, final int level, final boolean singleBuilding);

    /**
     * Defines the team name for all colonies (both Colony and ColonyView)
     *
     * @return The team name
     */
    default String getTeamName()
    {
        final String dim = getDimension().location().getPath();
        return TEAM_COLONY_NAME + "_" + (dim.length() > 10 ? dim.hashCode() : dim) + "_" + getID();
    }

    /**
     * Returns this colony's banner patterns, as a List
     *
     * @return a list of pattern-color pairs
     */
    ListNBT getColonyFlag();

    /**
     * Whether it is day for the colony
     *
     * @return true if it is day
     */
    boolean isDay();

    /**
     * Retrieves the team of the colony
     *
     * @return Team of the colony
     */
    ScorePlayerTeam getTeam();

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
     * Get the current {@link IRequestManager} for this Colony. Returns null if the current Colony does not support the request system.
     *
     * @return the {@link IRequestManager} for this colony, null if not supported.
     */
    @NotNull
    IRequestManager getRequestManager();

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
     * Method used to get a {@link IRequester} from a given Position. Is always a Building.
     *
     * @param pos The position to get the Building that acts as a requester.
     * @return The {@link IRequester} from the position, or null.
     */
    @Nullable
    IRequester getRequesterBuildingForPosition(@NotNull final BlockPos pos);

    /**
     * Remove a visiting player.
     *
     * @param player the player.
     */
    void removeVisitingPlayer(final PlayerEntity player);

    /**
     * Get the players in the colony which should receive the message.
     *
     * @return list of players
     */
    @NotNull
    List<PlayerEntity> getMessagePlayerEntities();

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

    Map<BlockPos, BlockState> getWayPoints();

    String getStyle();

    void setStyle(String style);

    IBuildingManager getBuildingManager();

    ICitizenManager getCitizenManager();

    IGraveManager getGraveManager();

    /**
     * Gets the visitor manager
     *
     * @return manager
     */
    IVisitorManager getVisitorManager();

    IRaiderManager getRaiderManager();

    /**
     * Get the event manager of the colony.
     *
     * @return the event manager.
     */
    IEventManager getEventManager();

    /**
     * Get the reproduction manager of the colony.
     *
     * @return the reproduction manager.
     */
    IReproductionManager getReproductionManager();

    /**
     * Get the event description manager of the colony.
     *
     * @return the event description manager.
     */
    IEventDescriptionManager getEventDescriptionManager();

    IColonyPackageManager getPackageManager();

    IProgressManager getProgressManager();

    /**
     * Add a visiting player.
     *
     * @param player the player.
     */
    void addVisitingPlayer(final PlayerEntity player);

    /**
     * Get the colony dimension.
     *
     * @return the dimension id.
     */
    RegistryKey<World> getDimension();

    /**
     * Check if the colony is on the server or client.
     *
     * @return true if so.
     */
    boolean isRemote();

    /**
     * Get the research manager.
     *
     * @return the research manager object.
     */
    IResearchManager getResearchManager();

    /**
     * Save the time when mercenaries are used, to set a cooldown.
     */
    void usedMercenaries();

    /**
     * Get the last time mercenaries were used.
     *
     * @return the mercenary use time.
     */
    long getMercenaryUseTime();

    CompoundNBT getColonyTag();

    boolean isColonyUnderAttack();

    boolean isValidAttackingPlayer(PlayerEntity entity);

    boolean isValidAttackingGuard(AbstractEntityCitizen entity);

    void setColonyColor(TextFormatting color);

    void setColonyFlag(ListNBT patterns);

    void setManualHousing(boolean manualHousing);

    void addWayPoint(BlockPos pos, BlockState newWayPointState);

    void addGuardToAttackers(AbstractEntityCitizen entityCitizen, PlayerEntity followPlayer);

    void addFreePosition(BlockPos pos);

    void addFreeBlock(Block block);

    void removeFreePosition(BlockPos pos);

    void removeFreeBlock(Block block);

    void setCanBeAutoDeleted(boolean canBeDeleted);

    void setManualHiring(boolean manualHiring);

    CompoundNBT write(CompoundNBT colonyCompound);

    void read(CompoundNBT compound);

    void setMoveIn(boolean newMoveIn);

    /**
     * Returns a set of players receiving important messages for the colony.
     *
     * @return set of players.
     */
    @NotNull
    List<PlayerEntity> getImportantMessageEntityPlayers();

    boolean isManualHiring();

    boolean isManualHousing();

    boolean canMoveIn();

    /**
     * Tries to use a given amount of additional growth-time for childs.
     *
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
    void addLoadedChunk(long chunkPos, final Chunk chunk);

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

    /**
     * Returns the colonies current state.
     *
     * @return the state.
     */
    ColonyState getState();

    /**
     * Is the colony active currently.
     *
     * @return true if so.
     */
    boolean isActive();

    /**
     * Get the set of chunk positions which the colony is loading through tickets
     *
     * @return set of positions
     */
    Set<Long> getTicketedChunks();

    /**
     * Set the texture style of the colony.
     * @param style the style to set.
     */
    void setTextureStyle(String style);

    /**
     * Get the colony style.
     * @return the string id of the style.
     */
    String getTextureStyleId();
}
