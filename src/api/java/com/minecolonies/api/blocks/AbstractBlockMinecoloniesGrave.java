package com.minecolonies.api.blocks;

import com.minecolonies.api.blocks.types.GraveType;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;

public abstract class AbstractBlockMinecoloniesGrave<B extends AbstractBlockMinecoloniesGrave<B>> extends AbstractBlockMinecolonies<B>
{
    public static final EnumProperty<GraveType> VARIANT = EnumProperty.create("variant", GraveType.class);

    /**
     * The position it faces.
     */
    public static final DirectionProperty      FACING       = HorizontalBlock.FACING;

    public AbstractBlockMinecoloniesGrave(final Properties properties)
    {
        super(properties.noOcclusion());
    }

}
