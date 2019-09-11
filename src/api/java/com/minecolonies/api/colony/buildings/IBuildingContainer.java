package com.minecolonies.api.colony.buildings;

import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public interface IBuildingContainer extends ISchematicProvider, ICitizenAssignable, ICapabilityProvider
{
    @Override
    void deserializeNBT(NBTTagCompound compound);

    @Override
    NBTTagCompound serializeNBT();

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
     * Check if the priority is static and it shouldn't change.
     *
     * @return the priority state, a boolean.
     */
    boolean isPriorityStatic();

    /**
     * Change the current priority state.
     */
    void alterPriorityState();

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
     * @param pos   of the blockState
     */
    void registerBlockPosition(@NotNull IBlockState blockState, @NotNull BlockPos pos, @NotNull World world);

    /**
     * Register a block and position.
     * We suppress this warning since this parameter will be used in child classes which override this method.
     *
     * @param block to be registered
     * @param pos   of the block
     */
    @SuppressWarnings("squid:S1172")
    void registerBlockPosition(@NotNull Block block, @NotNull BlockPos pos, @NotNull World world);

    /**
     * Try to transfer a stack to one of the inventories of the building.
     *
     * @param stack the stack to transfer.
     * @param world the world to do it in.
     * @return The {@link ItemStack} as that is left over, might be {@link ItemStackUtils#EMPTY} if the stack was completely accepted
     */
    ItemStack transferStack(@NotNull ItemStack stack, @NotNull World world);

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

    @Override
    boolean hasCapability(
      @Nonnull Capability<?> capability, @Nullable EnumFacing facing);

    @Nullable
    @Override
    <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing);
}
