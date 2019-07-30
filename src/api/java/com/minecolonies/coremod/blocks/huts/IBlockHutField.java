package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.coremod.blocks.interfaces.IBlockMinecolonies;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface IBlockHutField<B extends IBlockHutField<B>> extends IBlockMinecolonies<B>
{
    /**
     * The position it faces.
     */
    PropertyDirection FACING           = BlockHorizontal.FACING;
    /**
     * Hardness of the block.
     */
    float             HARDNESS         = 10F;
    /**
     * Resistance of the block.
     */
    float             RESISTANCE       = 10F;
    /**
     * Start of the collision box at y.
     */
    double            BOTTOM_COLLISION = 0.0;
    /**
     * Start of the collision box at x and z.
     */
    double            START_COLLISION  = 0.1;
    /**
     * End of the collision box.
     */
    double            END_COLLISION    = 0.9;
    /**
     * Height of the collision box.
     */
    double            HEIGHT_COLLISION = 2.5;
    /**
     * Registry name for this block.
     */
    String            REGISTRY_NAME    = "blockHutField";
}
