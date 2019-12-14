package com.minecolonies.api.colony.buildings;

import com.minecolonies.api.colony.buildings.registry.BuildingEntry;

public final class ModBuildings
{

    public static final String        ARCHERY_ID        = "Archery";
    public static final String        BAKERY_ID         = "Baker";
    public static final String        BARRACKS_ID       = "Barracks";
    public static final String        BARRACKS_TOWER_ID = "BarracksTower";
    public static final String        BLACKSMITH_ID     = "Blacksmith";
    public static final String        BUILDER_ID        = "Builder";
    public static final String        CHICKENHERDER_ID  = "ChickenHerder";
    public static final String        COMBAT_ACADEMY_ID = "CombatAcademy";
    public static final String        COMPOSTER_ID      = "Composter";
    public static final String        COOK_ID           = "Cook";
    public static final String        COWBOY_ID         = "Cowboy";
    public static final String        CRUSHER_ID        = "Crusher";
    public static final String        DELIVERYMAN_ID    = "Deliveryman";
    public static final String        FARMER_ID         = "Farmer";
    public static final String        FISHERMAN_ID      = "Fisherman";
    public static final String        GUARD_TOWER_ID    = "GuardTower";
    public static final String        HOME_ID           = "Home";
    public static final String        LIBRARY_ID        = "Library";
    public static final String        LUMBERJACK_ID     = "Lumberjack";
    public static final String        MINER_ID          = "Miner";
    public static final String        SAWMILL_ID        = "Sawmill";
    public static final String        SHEPHERD_ID       = "Shepherd";
    public static final String        SIFTER_ID         = "Sifter";
    public static final String        SMELTERY_ID       = "Smeltery";
    public static final String        STONE_MASON_ID    = "Stonemason";
    public static final String        STONE_SMELTERY_ID = "StoneSmeltery";
    public static final String        SWINE_HERDER_ID   = "SwingHerder"; //TODO: Seriously!?!? That mapping name!
    public static final String        TOWNHALL_ID       = "TownHall";
    public static final String        WAREHOUSE_ID      = "WareHouse";
    public static final String        POSTBOX_ID        = "Postbox";
    public static final String        FLORIST_ID        = "Florist";
    public static final String        ENCHANTER_ID      = "enchanter";

    public static       BuildingEntry archery;
    public static       BuildingEntry bakery;
    public static       BuildingEntry barracks;
    public static       BuildingEntry barracksTower;
    public static       BuildingEntry blacksmith;
    public static       BuildingEntry builder;
    public static       BuildingEntry chickenHerder;
    public static       BuildingEntry combatAcademy;
    public static       BuildingEntry composter;
    public static       BuildingEntry cook;
    public static       BuildingEntry cowboy;
    public static       BuildingEntry crusher;
    public static       BuildingEntry deliveryman;
    public static       BuildingEntry farmer;
    public static       BuildingEntry fisherman;
    public static       BuildingEntry guardTower;
    public static       BuildingEntry home;
    public static       BuildingEntry library;
    public static       BuildingEntry lumberjack;
    public static       BuildingEntry miner;
    public static       BuildingEntry sawmill;
    public static       BuildingEntry shepherd;
    public static       BuildingEntry sifter;
    public static       BuildingEntry smeltery;
    public static       BuildingEntry stoneMason;
    public static       BuildingEntry stoneSmelter;
    public static       BuildingEntry swineHerder;
    public static       BuildingEntry townHall;
    public static       BuildingEntry wareHouse;
    public static       BuildingEntry postBox;
    public static       BuildingEntry florist;
    public static       BuildingEntry enchanter;

    private ModBuildings()
    {
        throw new IllegalStateException("Tried to initialize: ModBuildings but this is a Utility class.");
    }
}
