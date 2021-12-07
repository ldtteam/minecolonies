package com.minecolonies.api.items;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.tags.Tag;

import java.util.HashMap;
import java.util.Map;

public class ModTags
{
    /**
     * Flag to check if tags are already loaded.
     */
    public static boolean tagsLoaded = false;

    public static Tag<Block> decorationItems;
    public static Tag<Item>  concretePowder;
    public static Tag<Block> concreteBlock;
    public static Tag<Block> pathingBlocks;

    public static Tag<Block> colonyProtectionException;
    public static Tag<Block> indestructible;

    public static Tag<Block> oreChanceBlocks;

    public static Tag<Item> fungi;

    public static Tag<Item> meshes;

    public static Tag<Item> floristFlowers;
    public static Tag<Item> excludedFood;

    public static Tag<Item> breakable_ore;
    public static Tag<Item> raw_ore;

    public static Tag<EntityType<?>> hostile;

    public static final Map<String, Tag<Item>> crafterProduct              = new HashMap<>();
    public static final Map<String, Tag<Item>> crafterProductExclusions    = new HashMap<>();
    public static final Map<String, Tag<Item>> crafterIngredient           = new HashMap<>();
    public static final Map<String, Tag<Item>> crafterIngredientExclusions = new HashMap<>();
}
