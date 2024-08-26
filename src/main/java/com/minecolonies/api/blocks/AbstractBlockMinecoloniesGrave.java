package com.minecolonies.api.blocks;

import com.minecolonies.api.blocks.types.GraveType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public abstract class AbstractBlockMinecoloniesGrave<B extends AbstractBlockMinecoloniesGrave<B>> extends Block implements EntityBlock
{
    public static final EnumProperty<GraveType> VARIANT = EnumProperty.create("variant", GraveType.class);

    /**
     * The position it faces.
     */
    public static final DirectionProperty      FACING       = HorizontalDirectionalBlock.FACING;

    public AbstractBlockMinecoloniesGrave(final Properties properties)
    {
        super(properties.noOcclusion());
    }

}
