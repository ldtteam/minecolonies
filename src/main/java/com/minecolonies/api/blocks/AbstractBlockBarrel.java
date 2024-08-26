package com.minecolonies.api.blocks;

import com.minecolonies.api.blocks.interfaces.ITickableBlockMinecolonies;
import com.minecolonies.api.blocks.types.BarrelType;
import com.minecolonies.api.tileentities.AbstractTileEntityBarrel;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractBlockBarrel<B extends AbstractBlockBarrel<B>> extends HorizontalDirectionalBlock implements ITickableBlockMinecolonies
{
    public static final EnumProperty<BarrelType> VARIANT = EnumProperty.create("variant", BarrelType.class);

    /**
     * The position it faces.
     */
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public AbstractBlockBarrel(final Properties properties)
    {
        super(properties);
    }

    public static BlockState changeStateOverFullness(@NotNull final AbstractTileEntityBarrel te, @NotNull final BlockState blockState)
    {
        /*
         * 12.8 -> the number of items needed to go up on a state (having 6 filling states)
         * So item/12.8 -> meta of the state we should get
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
        else if (te.getItems() == AbstractTileEntityBarrel.MAX_ITEMS)
        {
            type = BarrelType.WORKING;
        }
        if (te.isDone())
        {
            type = BarrelType.DONE;
        }

        return blockState.setValue(AbstractBlockBarrel.VARIANT, type).setValue(AbstractBlockBarrel.FACING, blockState.getValue(AbstractBlockBarrel.FACING));
    }
}
