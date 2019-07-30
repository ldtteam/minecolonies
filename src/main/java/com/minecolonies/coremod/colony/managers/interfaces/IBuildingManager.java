package com.minecolonies.coremod.colony.managers.interfaces;

import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.gameevent.TickEvent;
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
    void readFromNBT(@NotNull final NBTTagCompound compound);

    /**
     * Write the buildings to NBT.
     * @param compound the compound.
     */
    void writeToNBT(@NotNull final NBTTagCompound compound);

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
    void sendPackets(Set<EntityPlayerMP> oldSubscribers, boolean hasNewSubscribers, final Set<EntityPlayerMP> subscribers);

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
    AbstractBuilding getBuilding(BlockPos pos);

    /**
     * Returns a map with all buildings within the colony.
     * Key is ID (Coordinates), value is building object.
     *
     * @return Map with ID (coordinates) as key, and buildings as value.
     */
    @NotNull
    Map<BlockPos, AbstractBuilding> getBuildings();

    /**
     * Get the townhall from the colony.
     * @return the townhall building.
     */
    BuildingTownHall getTownHall();

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
    <B extends AbstractBuilding> B getBuilding(final BlockPos buildingId, @NotNull final Class<B> type);

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
    void addNewField(final ScarecrowTileEntity tileEntity, final BlockPos pos, final World world);

    /**
     * Returns a field which has not been taken yet.
     *
     * @param owner id of the owner of the field.
     * @return a field if there is one available, else null.
     */
    @Nullable
    ScarecrowTileEntity getFreeField(final int owner, final World world);

    /**
     * Remove a AbstractBuilding from the Colony (when it is destroyed).
     *
     * @param building AbstractBuilding to remove.
     */
    void removeBuilding(@NotNull final AbstractBuilding building, final Set<EntityPlayerMP> subscribers);

    /**
     * Marks building data dirty.
     */
    void markBuildingsDirty();

    /**
     * Creates a building from a tile entity and adds it to the colony.
     *
     * @param tileEntity Tile entity to build a building from.
     * @return AbstractBuilding that was created and added.
     */
    @Nullable
    AbstractBuilding addNewBuilding(@NotNull final TileEntityColonyBuilding tileEntity, final World world);

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
    BlockPos getBestRestaurant(final EntityCitizen citizen);

    /**
     * Set the townhall building.
     * @param building the building to set.
     */
    void setTownHall(@Nullable final BuildingTownHall building);

    /**
     * Set the warehouse building.
     * @param building the building to set.
     */
    void setWareHouse(@Nullable final BuildingWareHouse building);
}
