package com.minecolonies.core.blocks.schematic;

import com.minecolonies.api.blocks.interfaces.IMinecoloniesBlock;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/**
 * This block is a waypoint, which makes citizens path to it.
 */
public class BlockWaypoint extends Block implements IMinecoloniesBlock<BlockItem>
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
        super(Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(BLOCK_HARDNESS, RESISTANCE).noCollission());
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return new ResourceLocation(Constants.MOD_ID, BLOCK_NAME);
    }

    @Override
    public BlockItem createBlockItem()
    {
        return new BlockItem(this, new Item.Properties());
    }
}
