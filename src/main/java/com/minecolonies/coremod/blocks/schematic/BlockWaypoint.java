package com.minecolonies.coremod.blocks.schematic;

import com.minecolonies.api.blocks.AbstractBlockMinecolonies;
import net.minecraft.block.material.Material;

import net.minecraft.block.AbstractBlock.Properties;

/**
 * This block is a waypoint, which makes citizens path to it.
 */
public class BlockWaypoint extends AbstractBlockMinecolonies<BlockWaypoint>
{

    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 0.0F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "blockwaypoint";

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 1F;

    /**
     * Constructor for the waypoint. Sets the creative tab, as well as the resistance and the hardness.
     */
    public BlockWaypoint()
    {
        super(Properties.of(Material.WOOD).strength(BLOCK_HARDNESS, RESISTANCE).noCollission());
        setRegistryName(BLOCK_NAME);
    }
}
