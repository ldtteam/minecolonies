package com.minecolonies.coremod.event;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.TableLootEntry;
import net.minecraftforge.event.LootTableLoadEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public class LootTableEventHandler {
    public static List<String> mobs = Arrays.asList(
            "minecraft:entities/skeleton",
            "minecraft:entities/wither_skeleton",
            "minecraft:entities/enderman",
            "minecraft:entities/zombie",
            "minecraft:entities/pillager",
            "minecraft:entities/vex",
            "minecraft:entities/vindicator",
            "minecraft:entities/drowned",
            "minecraft:entities/illusioner",
            "minecraft:entities/zombie",
            "minecraft:entities/husk",
            "minecraft:entities/evoker",
            "minecraft:entities/witch",
            "minecraft:entities/zombie_villager",
            "minecraft:entities/zombie_pigman");

    static void addTomeToMobs(LootTableLoadEvent event) {
        if (mobs.contains(event.getName().toString())) {
            event.getTable().addPool(LootPool.builder().name("minecolonies:inject/tome").addEntry(TableLootEntry.builder(new ResourceLocation(MOD_ID, "inject/mob"))).build());
        }
    }

}
