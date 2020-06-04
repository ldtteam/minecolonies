package com.minecolonies.api.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockMinecoloniesContainer;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.state.DirectionProperty;

public abstract class AbstractBlockMinecoloniesDefault<B extends AbstractBlockMinecoloniesDefault<B>>
    extends AbstractBlockMinecoloniesContainer<B>
{
    /**
     * The position it faces.
     */
    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
    /**
     * Hardness of the block.
     */
    public static final float HARDNESS = 10F;
    /**
     * Resistance of the block.
     */
    public static final float RESISTANCE = 10F;
    /**
     * Start of the collision box at y.
     */
    public static final double BOTTOM_COLLISION = 0.0;
    /**
     * Start of the collision box at x and z.
     */
    public static final double START_COLLISION = 0.1;
    /**
     * End of the collision box.
     */
    public static final double END_COLLISION = 0.9;
    /**
     * Height of the collision box.
     */
    public static final double HEIGHT_COLLISION = 2.5;
    /**
     * Registry name for this block.
     */
    public static final String REGISTRY_NAME = "blockhutfield";

    public AbstractBlockMinecoloniesDefault(final Properties properties)
    {
        super(properties);
    }
}
