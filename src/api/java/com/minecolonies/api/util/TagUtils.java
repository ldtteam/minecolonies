package com.minecolonies.api.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.ResourceLocation;

/**
 * Class for specific minecraft tag utilities.
 */
public final class TagUtils
{
    private TagUtils()
    {
        throw new IllegalStateException("Tried to initialize: TagUtils but this is a Utility class.");
    }

    /**
     * Get a tag for items.
     * @param resourceLocation the unique id.
     * @return the tag or an empty placeholder if not existant.
     */
    public static Tag<Item> getItem(final ResourceLocation resourceLocation)
    {
        return ItemTags.getAllTags().getTagOrEmpty(resourceLocation);
    }

    /**
     * Get a tag for items.
     * @param resourceLocation the unique id.
     * @return the tag or an empty placeholder if not existant.
     */
    public static Tag<Block> getBlock(final ResourceLocation resourceLocation)
    {
        return BlockTags.getAllTags().getTagOrEmpty(resourceLocation);
    }
}
