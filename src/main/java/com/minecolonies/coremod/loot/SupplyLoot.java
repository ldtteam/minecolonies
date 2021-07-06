package com.minecolonies.coremod.loot;

import com.minecolonies.coremod.MineColonies;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.TableLootEntry;
import net.minecraft.util.ResourceLocation;
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
        lootTables.put(LootTables.SPAWN_BONUS_CHEST, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.SIMPLE_DUNGEON, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.VILLAGE_CARTOGRAPHER, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.VILLAGE_MASON, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.VILLAGE_DESERT_HOUSE, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.ABANDONED_MINESHAFT, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.STRONGHOLD_LIBRARY, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.STRONGHOLD_CROSSING, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.STRONGHOLD_CORRIDOR, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.DESERT_PYRAMID, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.JUNGLE_TEMPLE, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.IGLOO_CHEST, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.WOODLAND_MANSION, SUPPLY_CAMP_LT);
        lootTables.put(LootTables.PILLAGER_OUTPOST, SUPPLY_CAMP_LT);

        // Ship
        lootTables.put(LootTables.UNDERWATER_RUIN_SMALL, SUPPLY_SHIP_LT);
        lootTables.put(LootTables.UNDERWATER_RUIN_BIG, SUPPLY_SHIP_LT);
        lootTables.put(LootTables.BURIED_TREASURE, SUPPLY_SHIP_LT);
        lootTables.put(LootTables.SHIPWRECK_MAP, SUPPLY_SHIP_LT);
        lootTables.put(LootTables.SHIPWRECK_SUPPLY, SUPPLY_SHIP_LT);
        lootTables.put(LootTables.SHIPWRECK_TREASURE, SUPPLY_SHIP_LT);
        lootTables.put(LootTables.VILLAGE_FISHER, SUPPLY_SHIP_LT);
        lootTables.put(LootTables.VILLAGE_ARMORER, SUPPLY_SHIP_LT);
        lootTables.put(LootTables.VILLAGE_TEMPLE, SUPPLY_SHIP_LT);
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
     *
     * @param event the event.
     */
    public void addLootToEvent(LootTableLoadEvent event)
    {
        if (MineColonies.getConfig().getServer().generateSupplyLoot.get() && lootTables.containsKey(event.getName()))
        {
            event.getTable().addPool(LootPool.lootPool().add(TableLootEntry.lootTableReference(lootTables.get(event.getName()))).name(MOD_ID + ":loot:" + event.getName()).build());
        }
    }
}
