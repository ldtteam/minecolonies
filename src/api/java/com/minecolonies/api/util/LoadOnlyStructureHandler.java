package com.minecolonies.api.util;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structurize.placement.structure.CreativeStructureHandler;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Load only structure handler just to get dimensions etc from structures, not for placement.
 */
public final class LoadOnlyStructureHandler extends CreativeStructureHandler
{
    /**
     * The minecolonies specific creative structure placer.
     * @param world the world.
     * @param pos the pos it is placed at.
     * @param structureName the name of the structure.
     * @param settings the placement settings.
     * @param fancyPlacement if fancy or complete.
     */
    public LoadOnlyStructureHandler(final World world, final BlockPos pos, final String structureName, final PlacementSettings settings, final boolean fancyPlacement)
    {
        super(world, pos, structureName, settings, fancyPlacement);
    }

    /**
     * The minecolonies specific creative structure placer.
     * @param world the world.
     * @param pos the pos it is placed at.
     * @param blueprint the blueprint.
     * @param settings the placement settings.
     * @param fancyPlacement if fancy or complete.
     */
    public LoadOnlyStructureHandler(final World world, final BlockPos pos, final Blueprint blueprint, final PlacementSettings settings, final boolean fancyPlacement)
    {
        super(world, pos, blueprint, settings, fancyPlacement);
    }

    @Override
    public void triggerSuccess(final BlockPos pos, final List<ItemStack> list, final boolean placement)
    {
        // DO nothing
    }

    @Override
    public boolean shouldBlocksBeConsideredEqual(final BlockState state1, final BlockState state2)
    {
        final Block block1 = state1.getBlock();
        final Block block2 = state2.getBlock();

        if (block1 == Blocks.FLOWER_POT || block2 == Blocks.FLOWER_POT)
        {
            return block1 == block2;
        }

        if (block1 == Blocks.GRASS_BLOCK && block2 == Blocks.DIRT || block2 == Blocks.GRASS_BLOCK && block1 == Blocks.DIRT)
        {
            return true;
        }


        return super.shouldBlocksBeConsideredEqual(state1, state2);
    }

    @Override
    public boolean isStackFree(@Nullable final ItemStack itemStack)
    {
        return itemStack == null
                 ||itemStack.isEmpty()
                 || itemStack.getItem().isIn(ItemTags.LEAVES)
                 || itemStack.getItem() == new ItemStack(ModBlocks.blockDecorationPlaceholder, 1).getItem();
    }
}
