package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IMysticalSite;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHall;
import com.minecolonies.api.colony.buildings.workerbuildings.IWareHouse;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.tileentities.AbstractScarecrowTileEntity;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.AbstractTileEntityGrave;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Interface for building managers.
 */
public interface IBuildingManager
{
    /**
     * Read the buildings from NBT.
     *
     * @param compound the compound.
     */
    void read(@NotNull final CompoundNBT compound);

    /**
     * Write the buildings to NBT.
     *
     * @param compound the compound.
     */
    void write(@NotNull final CompoundNBT compound);

    /**
     * Clear the isDirty of the buildings.
     */
    void clearDirty();

    /**
     * Send packets of the buildings to the subscribers.
     *
     * @param closeSubscribers the old subs.
     * @param newSubscribers   new subs.
     */
    void sendPackets(Set<ServerPlayerEntity> closeSubscribers, final Set<ServerPlayerEntity> newSubscribers);

    /**
     * Tick the buildings on colony tick.
     *
     * @param colony the event.
     */
    void onColonyTick(IColony colony);

    /**
     * Clean up the buildings.
     *
     * @param colony at the worldTick event.
     */
    void cleanUpBuildings(final IColony colony);

    /**
     * Get a certain building.
     *
     * @param pos the id of the building.
     * @return the building.
     */
    IBuilding getBuilding(BlockPos pos);

    /**
     * Get the closest warehouse relative to a position.
     * @param pos the position,.
     * @return the closest warehouse.
     */
    @Nullable
    IWareHouse getClosestWarehouseInColony(BlockPos pos);

    /**
     * Returns a map with all buildings within the colony. Key is ID (Coordinates), value is building object.
     *
     * @return Map with ID (coordinates) as key, and buildings as value.
     */
    @NotNull
    Map<BlockPos, IBuilding> getBuildings();

    /**
     * Get the townhall from the colony.
     *
     * @return the townhall building.
     */
    ITownHall getTownHall();

    /**
     * Get the maximum level among built mystical sites
     *
     * @return the max level among all mystical sites or zero if no mystical site built
     */
    int getMysticalSiteMaxBuildingLevel();

    /**
     * Check if the colony has a placed warehouse.
     *
     * @return true if so.
     */
    boolean hasWarehouse();

    /**
     * Check if the colony has a placed mystical site.
     *
     * @return true if so.
     */
    boolean hasMysticalSite();

    /**
     * Check if the colony has a placed townhall.
     *
     * @return true if so.
     */
    boolean hasTownHall();

    /**
     * Get building in Colony by ID. The building will be casted to the provided type.
     *
     * @param buildingId ID (coordinates) of the building to get.
     * @param type       Type of building.
     * @param <B>        Building class.
     * @return the building with the specified id.
     */
    @Nullable
    <B extends IBuilding> B getBuilding(final BlockPos buildingId, @NotNull final Class<B> type);

    /**
     * Getter for a unmodifiable version of the farmerFields list.
     *
     * @return list of fields and their id.
     */
    @NotNull
    List<BlockPos> getFields();

    /**
     * Creates a field from a tile entity and adds it to the colony.
     *
     * @param tileEntity the scarecrow which contains the inventory.
     * @param pos        Position where the field has been placed.
     * @param world      the world of the field.
     */
    void addNewField(final AbstractScarecrowTileEntity tileEntity, final BlockPos pos, final World world);

    /**
     * Returns a field which has not been taken yet.
     *
     * @param owner id of the owner of the field.
     * @param world the world it is in.
     * @return a field if there is one available, else null.
     */
    @Nullable
    AbstractScarecrowTileEntity getFreeField(final int owner, final World world);

    /**
     * Remove a IBuilding from the Colony (when it is destroyed).
     *
     * @param subscribers the subscribers of the colony to message.
     * @param building    IBuilding to remove.
     */
    void removeBuilding(@NotNull final IBuilding building, final Set<ServerPlayerEntity> subscribers);

    /**
     * Marks building data dirty.
     */
    void markBuildingsDirty();

    /**
     * Creates a building from a tile entity and adds it to the colony.
     *
     * @param tileEntity Tile entity to build a building from.
     * @param world      the world to add it to.
     * @return IBuilding that was created and added.
     */
    @Nullable
    IBuilding addNewBuilding(@NotNull final AbstractTileEntityColonyBuilding tileEntity, final World world);

    /**
     * Removes a field from the farmerFields list.
     *
     * @param pos the position-id.
     */
    void removeField(final BlockPos pos);

    /**
     * Calculate a good cook for a certain citizen.
     *
     * @param citizen the citizen.
     * @param building the type of building.
     * @return the Position of it.
     */
    BlockPos getBestBuilding(final AbstractEntityCitizen citizen, final Class<? extends IBuilding> building);

    /**
     * Calculate a good building for a certain pos.
     *
     * @param pos the pos.
     * @param building the building class type.
     * @return the Position of it.
     */
    BlockPos getBestBuilding(final BlockPos pos, final Class<? extends IBuilding> building);

    /**
     * Returns a random building in the colony, matching the filter predicate.
     *
     * @param filterPredicate the filter to apply.
     * @return the random building. Returns null if no building matching the predicate was found.
     */
    BlockPos getRandomBuilding(Predicate<IBuilding> filterPredicate);

    /**
     * Finds whether there is a guard building close to the given building
     *
     * @param building the building to check for.
     * @return false if no guard tower close, true in other cases
     */
    boolean hasGuardBuildingNear(IBuilding building);

    /**
     * Event once a guard building changed at a certain level.
     * @param guardBuilding the guard building.
     * @param newLevel the level of it.
     */
    void guardBuildingChangedAt(IBuilding guardBuilding, int newLevel);

    /**
     * Set the townhall building.
     *
     * @param building the building to set.
     */
    void setTownHall(@Nullable final ITownHall building);

    /**
     * Removes a warehouse from the BuildingManager
     *
     * @param wareHouse the warehouse to remove.
     */
    void removeWareHouse(final IWareHouse wareHouse);

    /**
     * Get a list of the warehouses in this colony.
     *
     * @return the warehouse.
     */
    List<IWareHouse> getWareHouses();

    /**
     * Removes a warehouse from the BuildingManager
     *
     * @param mysticalSite the warehouse to remove.
     */
    void removeMysticalSite(final IMysticalSite mysticalSite);

    /**
     * Get a list of the mystical sites in this colony.
     *
     * @return the list of mistical sites.
     */
    List<IMysticalSite> getMysticalSites();

    /**
     * Checks whether we're allowed to place the block for a new building
     *
     * @param block  Block to check
     * @param pos    position
     * @param player the player trying to place
     * @return true if placement allowed
     */
    boolean canPlaceAt(Block block, BlockPos pos, PlayerEntity player);

    /**
     * Check if the chunk position it within of the building zone of the colony.
     * @param chunk the chunk to check
     * @return true if within.
     */
    boolean isWithinBuildingZone(final Chunk chunk);

    /**
     * Get a house with a spare bed.
     * @return the house or null.
     */
    IBuilding getHouseWithSpareBed();
}
