package com.minecolonies.coremod.items;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Class used to handle the compost item.
 */
public class ItemCompost extends AbstractItemMinecolonies
{
    /**
     * Max stacksize of the item.
     */
    private static final int MAX_STACK_SIZE = 64;

    /**
     * Compost constructor, Set max stack size to 64 like all other items.
     */
    public ItemCompost()
    {
        super("compost");

        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        maxStackSize = MAX_STACK_SIZE;
    }

    /**
     * /whenever player right click a block with this "compost item", call this method.
     * @param stack the right clicked stack.
     * @param playerIn the player doing it.
     * @param worldIn the world.
     * @param pos the position.
     * @param side the side he clicks it.
     * @param hitX the x hit position.
     * @param hitY the y hit position.
     * @param hitZ the z hit position.
     * @return true if succesful.
     */
    public boolean onItemUse(
                              final ItemStack stack,
                              final EntityPlayer playerIn,
                              final World worldIn,
                              final BlockPos pos,
                              final EnumFacing side,
                              final float hitX,
                              final float hitY,
                              final float hitZ)
    {

        final IBlockState iblockstate = worldIn.getBlockState(pos);

        if (iblockstate.getBlock() instanceof IGrowable)
        {
            final IGrowable igrowable = (IGrowable) iblockstate.getBlock();

            if (igrowable.canGrow(worldIn, pos, iblockstate, worldIn.isRemote))
            {
                if (!worldIn.isRemote)
                {
                    if (igrowable.canUseBonemeal(worldIn, worldIn.rand, pos, iblockstate))
                    {
                        igrowable.grow(worldIn, worldIn.rand, pos, iblockstate);
                    }

                    ItemStackUtils.changeSize(stack, -1);
                }
            }
        }
        return true;
    }
}


