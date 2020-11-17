package com.minecolonies.api.items;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;

public class ModTags
{
    /**
     * Flag to check if tags are already loaded.
     */
    public static boolean tagsLoaded = false;

    public static ITag<Block> decorationItems;
    public static ITag<Item>  concretePowder;
    public static ITag<Block>    concreteBlock;
    public static ITag<Block>    pathingBlocks;

    public static ITag<Block>    oreChanceBlocks;
    public static ITag<Item>     compostables;

    public static final Map<String, ITag<Item>> crafterProduct = new HashMap<>();
    public static final Map<String, ITag<Item>> crafterProductExclusions = new HashMap<>();
    public static final Map<String, ITag<Item>> crafterIngredient = new HashMap<>();
    public static final Map<String, ITag<Item>> crafterIngredientExclusions = new HashMap<>();
}
