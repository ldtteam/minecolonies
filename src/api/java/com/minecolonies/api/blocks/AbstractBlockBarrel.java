package com.minecolonies.api.blocks;

import com.minecolonies.api.blocks.interfaces.IBlockMinecolonies;
import com.minecolonies.api.blocks.types.BarrelType;
import com.minecolonies.api.tileentities.AbstractTileEntityBarrel;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;

public abstract class AbstractBlockBarrel<B extends AbstractBlockBarrel<B>> extends AbstractBlockMinecoloniesHorizontal<B> implements IBlockMinecolonies<B>, ITileEntityProvider
{
    public static final PropertyEnum<BarrelType> VARIANT = PropertyEnum.create("variant", BarrelType.class);
    /**
     * The position it faces.
     */
    public static final PropertyDirection        FACING  = BlockHorizontal.FACING;

    public AbstractBlockBarrel(final Material blockMaterialIn)
    {
        super(blockMaterialIn);
    }

    public static IBlockState changeStateOverFullness(
      final AbstractTileEntityBarrel entity,
      final IBlockState blockState
    )
    {

        final AbstractTileEntityBarrel te = entity;

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
        else if (te.getItems() == AbstractTileEntityBarrel.MAX_ITEMS)
        {
            type = BarrelType.WORKING;
        }
        if(te.isDone())
        {
            type = BarrelType.DONE;
        }

        return blockState.withProperty(AbstractBlockBarrel.VARIANT,
          type).withProperty(AbstractBlockBarrel.FACING, blockState.getValue(AbstractBlockBarrel.FACING));
    }
}
