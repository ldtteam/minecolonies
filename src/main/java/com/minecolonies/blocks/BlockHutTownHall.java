package com.minecolonies.blocks;

import com.minecolonies.configuration.Configurations;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the town hall.
 * Sets the working range for the town hall in the constructor
 */
public class BlockHutTownHall extends AbstractBlockHut
{
    protected BlockHutTownHall()
    {
        super();
        //Sets the working range to whatever the config is set to
        this.workingRange = Configurations.workingRangeTownHall;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutTownHall";
    }

    /**
     * Event-Handler for placement of this block.
     * <p>
     * Award the townhall placed achievement
     *
     * @param worldIn the word we are in
     * @param pos     the position where the block was placed
     * @param state   the state the placed block is in
     * @param placer  the player placing the block
     * @param stack   the itemstack from where the block was placed
     * @see Block#onBlockPlacedBy(World, BlockPos, IBlockState, EntityLivingBase, ItemStack)
     */
    @Override
    public void onBlockPlacedBy(@NotNull final World worldIn, @NotNull final BlockPos pos, final IBlockState state, final EntityLivingBase placer, final ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }
}
