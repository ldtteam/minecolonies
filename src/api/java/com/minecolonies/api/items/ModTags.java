package com.minecolonies.api.items;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;

public class ModTags
{
    public static ITag<Block> decorationItems;
    public static ITag<Item>  concretePowder;
    public static ITag<Block>    concreteBlock;
    public static ITag<Block>    pathingBlocks;

    public static final Map<String, ITag<Item>> crafterProduct = new HashMap<>();
    public static final Map<String, ITag<Item>> crafterProductExclusions = new HashMap<>();
    public static final Map<String, ITag<Item>> crafterIngredient = new HashMap<>();
    public static final Map<String, ITag<Item>> crafterIngredientExclusions = new HashMap<>();
}
