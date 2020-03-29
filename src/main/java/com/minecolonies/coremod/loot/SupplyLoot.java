package com.minecolonies.coremod.loot;

import com.minecolonies.coremod.MineColonies;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraft.world.storage.loot.TableLootEntry;
import net.minecraftforge.event.LootTableLoadEvent;

import java.util.HashMap;
import java.util.Map;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Helper class for supply camp loot
 */
public class SupplyLoot
{
    /**
     * Resource locations, path and names must fit the existing json file.
     */
    public final static ResourceLocation SUPPLY_CAMP_LT = new ResourceLocation(MOD_ID, "chests/supplycamp");
    public final static ResourceLocation SUPPLY_SHIP_LT = new ResourceLocation(MOD_ID, "chests/supplyship");

    /**
     * The instance
     */
    private static SupplyLoot instance;

    /**
     * Maps vanilla lootable resource location to our loot pool to add.
     */
    private Map<ResourceLocation, ResourceLocation> lootTables = new HashMap<>();

    private SupplyLoot()
    {
        init();
    }

    /**
     * Initializes the loot tables to attach to
     */
    protected void init()
    {
        // Camp
        lootTables.put(LootTables.CHESTS_SPAWN_BONUS_CHEST, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.CHESTS_SIMPLE_DUNGEON, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.CHESTS_VILLAGE_VILLAGE_CARTOGRAPHER, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.CHESTS_VILLAGE_VILLAGE_MASON, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.CHESTS_VILLAGE_VILLAGE_DESERT_HOUSE, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.CHESTS_ABANDONED_MINESHAFT, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.CHESTS_STRONGHOLD_LIBRARY, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.CHESTS_STRONGHOLD_CROSSING, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.CHESTS_STRONGHOLD_CORRIDOR, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.CHESTS_DESERT_PYRAMID, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.CHESTS_JUNGLE_TEMPLE, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.CHESTS_IGLOO_CHEST, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.CHESTS_WOODLAND_MANSION, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.CHESTS_PILLAGER_OUTPOST, SUPPLY_CAMP_LT);

        // Ship
        lootTables.put(LootTables.CHESTS_UNDERWATER_RUIN_SMALL, SUPPLY_SHIP_LT);
        lootTables.put(LootTables.CHESTS_UNDERWATER_RUIN_BIG, SUPPLY_SHIP_LT);
        lootTables.put(LootTables.CHESTS_BURIED_TREASURE, SUPPLY_SHIP_LT);
        lootTables.put(LootTables.CHESTS_SHIPWRECK_MAP, SUPPLY_SHIP_LT);
        lootTables.put(LootTables.CHESTS_SHIPWRECK_SUPPLY, SUPPLY_SHIP_LT);
        lootTables.put(LootTables.CHESTS_SHIPWRECK_TREASURE, SUPPLY_SHIP_LT);
        lootTables.put(LootTables.CHESTS_VILLAGE_VILLAGE_FISHER, SUPPLY_SHIP_LT);
        lootTables.put(LootTables.CHESTS_VILLAGE_VILLAGE_ARMORER, SUPPLY_SHIP_LT);
        lootTables.put(LootTables.CHESTS_VILLAGE_VILLAGE_TEMPLE, SUPPLY_SHIP_LT);
    }

    public static SupplyLoot getInstance()
    {
        if (instance == null)
        {
            instance = new SupplyLoot();
        }
        return instance;
    }

    /**
     * Adds loot to the given event, if the name fits
     */
    public void addLootToEvent(LootTableLoadEvent event)
    {
        if (MineColonies.getConfig().getCommon().generateSupplyLoot.get() && lootTables.containsKey(event.getName()))
        {
            event.getTable().addPool(LootPool.builder().addEntry(TableLootEntry.builder(lootTables.get(event.getName()))).build());
        }
    }
}
