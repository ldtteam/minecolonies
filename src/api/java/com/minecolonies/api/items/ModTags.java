package com.minecolonies.api.items;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tags.Tag;

public class ModTags
{
    public static Tag<Block>    decorationItems;
    public static Tag<Item>     concretePowder;
    public static Tag<Block>    concreteBlock;
    public static Tag<Block>    pathingBlocks;

    public static Tag<Block>    oreChanceBlocks;
    public static Tag<Item>     compostables;

    public static final Map<String, Tag<Item>> crafterProduct = new HashMap<>();
    public static final Map<String, Tag<Item>> crafterProductExclusions = new HashMap<>();
    public static final Map<String, Tag<Item>> crafterIngredient = new HashMap<>();
    public static final Map<String, Tag<Item>> crafterIngredientExclusions = new HashMap<>();
}
