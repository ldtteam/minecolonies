package com.minecolonies.coremod.blocks;

import com.minecolonies.coremod.blocks.interfaces.IBlockMinecolonies;
import com.minecolonies.coremod.blocks.types.BarrelType;
import com.minecolonies.coremod.tileentities.ITileEntityBarrel;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.state.EnumProperty;

public interface IBlockBarrel<B extends IBlockBarrel<B>> extends IBlockMinecolonies<B>
{
    EnumProperty<BarrelType> VARIANT = EnumProperty.create("variant", BarrelType.class);

    static BlockState changeStateOverFullness(ITileEntityBarrel entity, BlockState blockState)
    {

        final ITileEntityBarrel te = entity;

        /*
         * 12.8 -> the number of items needed to go up on a state (having 6 filling states)
         * So items/12.8 -> meta of the state we should get
         */
        BarrelType type = BarrelType.byMetadata((int) Math.round(te.getItems() / 12.8));

        /*
         * We check if the barrel is marked as empty but it have items inside. If so, means that it
         * does not have all the items needed to go on TWENTY state, but we need to mark it so the player
         * knows it have some items inside
         */

        if (type.equals(BarrelType.ZERO) && te.getItems() > 0)
        {
            type = BarrelType.TWENTY;
        }
        else if (te.getItems() == ITileEntityBarrel.MAX_ITEMS)
        {
            type = BarrelType.WORKING;
        }
        if (te.isDone())
        {
            type = BarrelType.DONE;
        }

        return blockState.with(IBlockBarrel.VARIANT,
          type).with(HorizontalBlock.HORIZONTAL_FACING, blockState.get(HorizontalBlock.HORIZONTAL_FACING));
    }
}
