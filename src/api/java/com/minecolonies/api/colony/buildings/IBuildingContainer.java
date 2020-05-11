package com.minecolonies.api.colony.buildings;

import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

public interface IBuildingContainer extends ISchematicProvider, ICitizenAssignable, ICapabilityProvider
{
    @Override
    void deserializeNBT(CompoundNBT compound);

    @Override
    CompoundNBT serializeNBT();

    /**
     * Get the pick up priority of the building.
     *
     * @return the priority, an integer.
     */
    int getPickUpPriority();

    /**
     * Increase or decrease the current pickup priority.
     *
     * @param value the new prio to add to.
     */
    void alterPickUpPriority(int value);

    /**
     * Sets the pickup priority to the given value.
     *
     * @param pickUpPriority The new pickup priority.
     */
    void setPickUpPriority(int pickUpPriority);

    /**
     * Get the current priority state
     */
    PickUpPriorityState getPriorityState();

    /**
     * Change the current priority state.
     */
    void setPriorityState(PickUpPriorityState state);

    /**
     * Add a new container to the building.
     *
     * @param pos position to add.
     */
    void addContainerPosition(@NotNull BlockPos pos);

    /**
     * Remove a container from the building.
     *
     * @param pos position to remove.
     */
    void removeContainerPosition(BlockPos pos);

    /**
     * Get all additional containers which belong to the building.
     *
     * @return a copy of the list to avoid currentModification exception.
     */
    List<BlockPos> getAdditionalCountainers();

    /**
     * Register a blockState and position.
     * We suppress this warning since this parameter will be used in child classes which override this method.
     *
     * @param blockState to be registered
     * @param pos        of the blockState
     * @param world      world to register it at.
     */
    void registerBlockPosition(@NotNull BlockState blockState, @NotNull BlockPos pos, @NotNull World world);

    /**
     * Register a block and position.
     * We suppress this warning since this parameter will be used in child classes which override this method.
     *
     * @param block to be registered
     * @param pos   of the block
     * @param world world to register it at.
     */
    @SuppressWarnings("squid:S1172")
    void registerBlockPosition(@NotNull Block block, @NotNull BlockPos pos, @NotNull World world);

    /**
     * Returns the tile entity that belongs to the colony building.
     *
     * @return {@link AbstractTileEntityColonyBuilding} object of the building.
     */
    AbstractTileEntityColonyBuilding getTileEntity();

    /**
     * Sets the tile entity for the building.
     *
     * @param te The tileentity
     */
    void setTileEntity(AbstractTileEntityColonyBuilding te);

    @Nonnull
    @Override
    <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final Direction direction);
}
