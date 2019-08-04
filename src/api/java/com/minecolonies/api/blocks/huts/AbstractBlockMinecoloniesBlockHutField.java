package com.minecolonies.api.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockMinecoloniesContainer;
import com.minecolonies.api.blocks.interfaces.IBlockMinecolonies;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;

public abstract class AbstractBlockMinecoloniesBlockHutField<B extends AbstractBlockMinecoloniesBlockHutField<B>> extends AbstractBlockMinecoloniesContainer<B> implements IBlockMinecolonies<B>
{
    /**
     * The position it faces.
     */
    public static final PropertyDirection FACING           = BlockHorizontal.FACING;
    /**
     * Hardness of the block.
     */
    public static final float             HARDNESS         = 10F;
    /**
     * Resistance of the block.
     */
    public static final float             RESISTANCE       = 10F;
    /**
     * Start of the collision box at y.
     */
    public static final double            BOTTOM_COLLISION = 0.0;
    /**
     * Start of the collision box at x and z.
     */
    public static final double            START_COLLISION  = 0.1;
    /**
     * End of the collision box.
     */
    public static final double            END_COLLISION    = 0.9;
    /**
     * Height of the collision box.
     */
    public static final double            HEIGHT_COLLISION = 2.5;
    /**
     * Registry name for this block.
     */
    public static final String            REGISTRY_NAME    = "blockHutField";

    public AbstractBlockMinecoloniesBlockHutField(final Material blockMaterialIn, final MapColor blockMapColorIn)
    {
        super(blockMaterialIn, blockMapColorIn);
    }

    public AbstractBlockMinecoloniesBlockHutField(final Material materialIn)
    {
        super(materialIn);
    }
}
