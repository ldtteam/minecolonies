package com.minecolonies.api.items;

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

    public static Map<String, Tag<Item>> crafterProduct;
    public static Map<String, Tag<Item>> crafterProductExclusions;
    public static Map<String, Tag<Item>> crafterIngredient;
    public static Map<String, Tag<Item>> crafterIngredientExclusions;
}
