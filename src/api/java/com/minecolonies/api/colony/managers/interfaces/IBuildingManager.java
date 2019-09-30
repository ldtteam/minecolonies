package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHall;
import com.minecolonies.api.colony.buildings.workerbuildings.IWareHouse;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.tileentities.AbstractScarescrowTileEntity;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for building managers.
 */
public interface IBuildingManager
{
    /**
     * Read the buildings from NBT.
     * @param compound the compound.
     */
    void read(@NotNull final CompoundNBT compound);

    /**
     * Write the buildings to NBT.
     * @param compound the compound.
     */
    void write(@NotNull final CompoundNBT compound);

    /**
     * Tick the buildings on server tick.
     * @param event the event.
     */
    void tick(TickEvent.ServerTickEvent event);

    /**
     * Clear the isDirty of the buildings.
     */
    void clearDirty();

    /**
     * Send packets of the buildings to the subscribers.
     * @param oldSubscribers the old subs.
     * @param hasNewSubscribers if there are new ones.
     * @param subscribers all the subs.
     */
    void sendPackets(Set<ServerPlayerEntity> oldSubscribers, boolean hasNewSubscribers, final Set<ServerPlayerEntity> subscribers);

    /**
     * Tick the buildings on world tick.
     * @param event the event.
     */
    void onWorldTick(TickEvent.WorldTickEvent event);

    /**
     * Clean up the buildings.
     * @param event at the worldTick event.
     */
    void cleanUpBuildings(final TickEvent.WorldTickEvent event);

    /**
     * Get a certain building.
     * @param pos the id of the building.
     * @return the building.
     */
    IBuilding getBuilding(BlockPos pos);

    /**
     * Returns a map with all buildings within the colony.
     * Key is ID (Coordinates), value is building object.
     *
     * @return Map with ID (coordinates) as key, and buildings as value.
     */
    @NotNull
    Map<BlockPos, IBuilding> getBuildings();

    /**
     * Get the townhall from the colony.
     * @return the townhall building.
     */
    ITownHall getTownHall();

    /**
     * Check if the colony has a placed warehouse.
     * @return true if so.
     */
    boolean hasWarehouse();

    /**
     * Check if the colony has a placed townhall.
     * @return true if so.
     */
    boolean hasTownHall();

    /**
     * Get building in Colony by ID. The building will be casted to the provided
     * type.
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
     * @param tileEntity      the scarecrow which contains the inventory.
     * @param pos             Position where the field has been placed.
     * @param world           the world of the field.
     */
    void addNewField(final AbstractScarescrowTileEntity tileEntity, final BlockPos pos, final World world);

    /**
     * Returns a field which has not been taken yet.
     *
     * @param owner id of the owner of the field.
     * @return a field if there is one available, else null.
     */
    @Nullable
    AbstractScarescrowTileEntity getFreeField(final int owner, final World world);

    /**
     * Remove a IBuilding from the Colony (when it is destroyed).
     *
     * @param building IBuilding to remove.
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
     * @param citizen the citizen.
     * @return the Position of it.
     */
    BlockPos getBestRestaurant(final AbstractEntityCitizen citizen);

    /**
     * Set the townhall building.
     * @param building the building to set.
     */
    void setTownHall(@Nullable final ITownHall building);

    /**
     * Removes a warehouse from the BuildingManager
     */
    void removeWareHouse(final IWareHouse wareHouse);

    /**
     * Get a list of the warehouses in this colony.
     * @return
     */
    List<IWareHouse> getWareHouses();
}
