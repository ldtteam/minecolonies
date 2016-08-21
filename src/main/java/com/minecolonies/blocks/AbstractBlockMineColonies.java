package com.minecolonies.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

/**
 * Abstract {@link Block} wrapper.
 * <p>
 * Used for mode related centralized code.
 *
 * @since 0.1
 */
public abstract class AbstractBlockMineColonies extends Block
{

    /**
     * Create a new Block for the mod.
     *
     * @param materialIn the material to use
     * @see Block#Block(Material)
     */
    public AbstractBlockMineColonies(Material materialIn)
    {
        super(materialIn);
    }

    /**
     * Create a new Block for the mod.
     *
     * @param blockMaterialIn the material to use
     * @param blockMapColorIn The color to show on the map
     * @see Block#Block(Material, MapColor)
     */
    public AbstractBlockMineColonies(Material blockMaterialIn, MapColor blockMapColorIn)
    {
        super(blockMaterialIn, blockMapColorIn);
    }

}
