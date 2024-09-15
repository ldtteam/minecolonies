package com.minecolonies.core.blocks.schematic;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/**
 * This block is a waypoint, which makes citizens path to it.
 */
public class BlockWaypoint extends Block
{
    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 0.0F;

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 1F;

    /**
     * Constructor for the waypoint. Sets the creative tab, as well as the resistance and the hardness.
     */
    public BlockWaypoint()
    {
        super(Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(BLOCK_HARDNESS, RESISTANCE).noCollission());
    }
}
