package com.minecolonies.coremod.placementhandlers;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Handler for all kinds of placements.
 */
public interface IPlacementHandler
{
    /**
     * Check if a placement handler can handle a certain block.
     *
     * @param world      the world.
     * @param pos        the position.
     * @param blockState the blockState.
     * @return true if so.
     */
    boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState);

    /**
     * Method used to handle the processing of a Placement of a block.
     *
     * @param world          receives the world.
     * @param pos            the position.
     * @param blockState     the blockState.
     * @param tileEntityData the placer of the block.
     * @param complete       place it complete (with or without substitution blocks etc).
     * @param centerPos
     * @return ACCEPT, DENY or IGNORE.
     */
    Object handle(
      @NotNull final World world,
      @NotNull final BlockPos pos,
      @NotNull final IBlockState blockState,
      @Nullable final NBTTagCompound tileEntityData,
      final boolean complete, final BlockPos centerPos);

    /**
     * Method used to get the required items to place a block.
     *
     * @param world          receives the world.
     * @param pos            the position.
     * @param blockState     the blockState.
     * @param tileEntityData the placer of the block.
     * @param complete       place it complete (with or without substitution blocks etc).
     * @return the list of items.
     */
    List<ItemStack> getRequiredItems(
      @NotNull final World world,
      @NotNull final BlockPos pos,
      @NotNull final IBlockState blockState,
      @Nullable final NBTTagCompound tileEntityData,
      final boolean complete);

    /**
     * Possible result of an IPlacementHandler call.
     */
    enum ActionProcessingResult
    {
        ACCEPT,
        DENY
    }
}
