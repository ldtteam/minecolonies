package com.minecolonies.coremod.blocks.schematic;

import com.minecolonies.api.blocks.AbstractBlockMinecolonies;
import net.minecraft.world.level.material.Material;

/**
 * This block is a gatemarker, this marks positions which citizens consider a colony gate
 */
public class BlockGateMarker extends AbstractBlockMinecolonies<BlockGateMarker>
{

    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 0.0F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "blockgatemarker";

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 1F;

    /**
     * Constructor for the waypoint. Sets the creative tab, as well as the resistance and the hardness.
     */
    public BlockGateMarker()
    {
        super(Properties.of(Material.WOOD).strength(BLOCK_HARDNESS, RESISTANCE).noCollission());
        setRegistryName(BLOCK_NAME);
    }
}
