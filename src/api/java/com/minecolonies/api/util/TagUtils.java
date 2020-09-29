package com.minecolonies.api.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;

public final class TagUtils
{

    private TagUtils()
    {
        throw new IllegalStateException("Tried to initialize: TagUtils but this is a Utility class.");
    }

    public static Optional<ITag<Item>> getItem(final ResourceLocation resourceLocation) {
        return Optional.ofNullable(ItemTags.getCollection().getIDTagMap().getOrDefault(resourceLocation, null));
    }

    public static Optional<ITag<Block>> getBlock(final ResourceLocation resourceLocation) {
        return Optional.ofNullable(BlockTags.getCollection().getIDTagMap().getOrDefault(resourceLocation, null));
    }
}
