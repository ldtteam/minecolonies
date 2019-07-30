package com.minecolonies.coremod.blocks;

import com.minecolonies.coremod.blocks.interfaces.IBlockMinecolonies;
import com.minecolonies.coremod.blocks.types.BarrelType;
import com.minecolonies.coremod.tileentities.ITileEntityBarrel;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;

public interface IBlockBarrel<B extends IBlockBarrel<B>> extends IBlockMinecolonies<B>, ITileEntityProvider
{
    PropertyEnum<BarrelType> VARIANT = PropertyEnum.create("variant", BarrelType.class);
    /**
     * The position it faces.
     */
    PropertyDirection        FACING  = BlockHorizontal.FACING;

    static IBlockState changeStateOverFullness(
      ITileEntityBarrel entity,
      IBlockState blockState
    )
    {

        final ITileEntityBarrel te = entity;

        /**
         * 12.8 -> the number of items needed to go up on a state (having 6 filling states)
         * So items/12.8 -> meta of the state we should get
         */
        BarrelType type = BarrelType.byMetadata((int) Math.round(te.getItems()/12.8));

        /**
         * We check if the barrel is marked as empty but it have items inside. If so, means that it
         * does not have all the items needed to go on TWENTY state, but we need to mark it so the player
         * knows it have some items inside
         */

        if(type.equals(BarrelType.ZERO) && te.getItems() > 0)
        {
            type = BarrelType.TWENTY;
        }
        else if (te.getItems() == ITileEntityBarrel.MAX_ITEMS)
        {
            type = BarrelType.WORKING;
        }
        if(te.isDone())
        {
            type = BarrelType.DONE;
        }

        return blockState.withProperty(IBlockBarrel.VARIANT,
          type).withProperty(IBlockBarrel.FACING, blockState.getValue(IBlockBarrel.FACING));
    }
}
