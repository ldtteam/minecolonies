package com.minecolonies.api.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

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
    public static ITag<Item> getItem(final ResourceLocation resourceLocation)
    {
        return ItemTags.getAllTags().getTagOrEmpty(resourceLocation);
    }

    /**
     * Get a tag for items.
     * @param resourceLocation the unique id.
     * @return the tag or an empty placeholder if not existant.
     */
    public static ITag<Block> getBlock(final ResourceLocation resourceLocation)
    {
        return BlockTags.getAllTags().getTagOrEmpty(resourceLocation);
    }
}
