package com.minecolonies.coremod.proxy;

import com.minecolonies.api.lib.Constants;
import com.minecolonies.api.reference.ModAchievements;
import com.minecolonies.api.reference.ModBlocks;
import com.minecolonies.api.reference.ModItems;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.achievements.MineColoniesAchievement;
import com.minecolonies.coremod.achievements.MineColoniesAchievementPage;
import com.minecolonies.coremod.blocks.*;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.configuration.ConfigurationHandler;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.EntityFishHook;
import com.minecolonies.coremod.event.EventHandler;
import com.minecolonies.coremod.event.FMLEventHandler;
import com.minecolonies.coremod.inventory.GuiHandler;
import com.minecolonies.coremod.items.*;
import com.minecolonies.coremod.sounds.ModSoundEvents;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import com.minecolonies.coremod.tileentities.TileEntityWareHouse;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * CommonProxy of the minecolonies mod (Server and Client).
 */
public class CommonProxy implements IProxy
{
    /**
     * Used to store IExtendedEntityProperties data temporarily between player
     * death and respawn.
     */
    private static final Map<String, NBTTagCompound> playerPropertiesData = new HashMap<>();
    private              int                         nextEntityId         = 0;

    /**
     * Adds an entity's custom data to the map for temporary storage.
     *
     * @param name     player UUID + Properties name, HashMap key.
     * @param compound An NBT Tag Compound that stores the IExtendedEntityProperties
     *                 data only.
     */
    public static void storeEntityData(final String name, final NBTTagCompound compound)
    {
        playerPropertiesData.put(name, compound);
    }

    /**
     * Removes the compound from the map and returns the NBT tag stored for name
     * or null if none exists.
     *
     * @param name player UUID + Properties name, HashMap key.
     * @return NBTTagCompound PlayerProperties NBT compound.
     */
    public static NBTTagCompound getEntityData(final String name)
    {
        return playerPropertiesData.remove(name);
    }

    @Override
    public boolean isClient()
    {
        return false;
    }

    @Override
    public void registerAchievements()
    {
        ModAchievements.achievementGetSupply = new MineColoniesAchievement("supply", 0, -2, ModItems.supplyChest, null).registerStat();
        ModAchievements.achievementWandOfbuilding = new MineColoniesAchievement("wandofbuilding", 0, -2, ModItems.buildTool, null)
                                                      .registerStat();
        ModAchievements.achievementTownhall = new MineColoniesAchievement("townhall", 0, 0, ModBlocks.blockHutTownHall, null)
                                                .registerStat();
        ModAchievements.achievementBuildingTownhall = new MineColoniesAchievement("upgrade.townhall.first", 2, 0, ModBlocks.blockHutTownHall,
                                                                                   ModAchievements.achievementTownhall).registerStat();

        /**
         * Max out a townhall.
         */
        ModAchievements.achievementUpgradeTownhallMax = new MineColoniesAchievement("upgrade.townhall.max", 4, 0, ModBlocks.blockHutTownHall,
                                                                                     ModAchievements.achievementBuildingTownhall).registerStat();

        /**
         * Upgrade a builder to lv 1.
         */
        ModAchievements.achievementBuildingBuilder = new MineColoniesAchievement("upgrade.builder.first", 2, 2, ModBlocks.blockHutBuilder,
                                                                                  ModAchievements.achievementTownhall).registerStat();
        /**
         * Max out a builder.
         */
        ModAchievements.achievementUpgradeBuilderMax = new MineColoniesAchievement("upgrade.builder.max", 4, 2, ModBlocks.blockHutBuilder,
                                                                                    ModAchievements.achievementBuildingBuilder).registerStat();

        /**
         * Upgrade a builder to lv 1.
         */
        ModAchievements.achievementBuildingColonist = new MineColoniesAchievement("upgrade.colonist.first",
                                                                                   2,
                                                                                   18,
                                                                                   ModBlocks.blockHutCitizen,
                                                                                   ModAchievements.achievementTownhall).registerStat();
        /**
         * Max out a builder.
         */
        ModAchievements.achievementUpgradeColonistMax = new MineColoniesAchievement("upgrade.colonist.max", 4, 18, ModBlocks.blockHutCitizen,
                                                                                     ModAchievements.achievementBuildingColonist).registerStat();

        /**
         * Upgrade a lumberjack to lv 1.
         */
        ModAchievements.achievementBuildingLumberjack = new MineColoniesAchievement("upgrade.lumberjack.first",
                                                                                     2,
                                                                                     11,
                                                                                     ModBlocks.blockHutLumberjack,
                                                                                     ModAchievements.achievementTownhall).registerStat();
        /**
         * Max out a lumberjack.
         */
        ModAchievements.achievementUpgradeLumberjackMax = new MineColoniesAchievement("upgrade.lumberjack.max",
                                                                                       4,
                                                                                       11,
                                                                                       ModBlocks.blockHutLumberjack,
                                                                                       ModAchievements.achievementBuildingLumberjack).registerStat();

        /**
         * Upgrade a miner to lv 1.
         */
        ModAchievements.achievementBuildingMiner = new MineColoniesAchievement("upgrade.miner.first", 2, 8, ModBlocks.blockHutMiner,
                                                                                ModAchievements.achievementTownhall).registerStat();
        /**
         * Max out a miner.
         */
        ModAchievements.achievementUpgradeMinerMax = new MineColoniesAchievement("upgrade.miner.max", 4, 8, ModBlocks.blockHutMiner,
                                                                                  ModAchievements.achievementBuildingMiner).registerStat();

        /**
         * Upgrade a fisher to lv 1.
         */
        ModAchievements.achievementBuildingFisher = new MineColoniesAchievement("upgrade.fisher.first", 2, 4, ModBlocks.blockHutFisherman,
                                                                                 ModAchievements.achievementTownhall).registerStat();
        /**
         * Max out a fisher.
         */
        ModAchievements.achievementUpgradeFisherMax = new MineColoniesAchievement("upgrade.fisher.max", 4, 4, ModBlocks.blockHutFisherman,
                                                                                   ModAchievements.achievementBuildingFisher).registerStat();

        /**
         * Upgrade a farmer to lv 1.
         */
        ModAchievements.achievementBuildingFarmer = new MineColoniesAchievement("upgrade.farmer.first", 2, 14, ModBlocks.blockHutFarmer,
                                                                                 ModAchievements.achievementTownhall).registerStat();
        /**
         * Max out a farmer.
         */
        ModAchievements.achievementUpgradeFarmerMax = new MineColoniesAchievement("upgrade.farmer.max", 4, 14, ModBlocks.blockHutFarmer,
                                                                                   ModAchievements.achievementBuildingFarmer).registerStat();

        /**
         * Upgrade a guard to lv 1.
         */
        ModAchievements.achievementBuildingGuard = new MineColoniesAchievement("upgrade.guard.first", 2, 6, ModBlocks.blockHutGuardTower,
                                                                                ModAchievements.achievementTownhall).registerStat();
        /**
         * Max out a guard.
         */
        ModAchievements.achievementUpgradeGuardMax = new MineColoniesAchievement("upgrade.guard.max", 4, 6, ModBlocks.blockHutGuardTower,
                                                                                  ModAchievements.achievementBuildingGuard).registerStat();
        /**
         * Death achievements.
         */
        ModAchievements.achievementMinerDeathLava = new MineColoniesAchievement("miner.death.lava", -2, 8, Items.LAVA_BUCKET,
                                                                                 ModAchievements.achievementBuildingMiner).registerStat();
        ModAchievements.achievementMinerDeathFall = new MineColoniesAchievement("miner.death.fall", -4, 8, Items.FEATHER,
                                                                                 ModAchievements.achievementBuildingMiner).registerStat();
        ModAchievements.achievementLumberjackDeathTree = new MineColoniesAchievement("lumberjack.death.tree", -2, 11, Blocks.SAPLING,
                                                                                      ModAchievements.achievementBuildingLumberjack).registerStat();
        ModAchievements.achievementFisherDeathGuardian = new MineColoniesAchievement("fisher.death.guardian", -2, 4, Blocks.SEA_LANTERN,
                                                                                      ModAchievements.achievementBuildingFisher).registerStat();
        ModAchievements.achievementGuardDeathEnderman = new MineColoniesAchievement("guard.death.enderman", -2, 6, Items.ENDER_PEARL,
                                                                                     ModAchievements.achievementBuildingGuard).registerStat();
        ModAchievements.achievementPlayerDeathGuard = new MineColoniesAchievement("player.death.guard", -4, 6, Items.ARROW,
                                                                                   ModAchievements.achievementBuildingGuard).registerStat();

        /**
         * Do something for the first time.
         */
        ModAchievements.achievementBuildOneHut = new MineColoniesAchievement("builder.hutsBuilt.one", 4, 3, ModBlocks.blockHutCitizen,
                                                                              ModAchievements.achievementBuildingBuilder).registerStat();
        ModAchievements.achievementCatchOneFish = new MineColoniesAchievement("fisher.fishCaught.one", 4, 5, Items.FISH,
                                                                               ModAchievements.achievementBuildingFisher).registerStat();
        ModAchievements.achievementKillOneMob = new MineColoniesAchievement("guard.mobsKilled.one", 4, 7, Items.BONE,
                                                                             ModAchievements.achievementBuildingGuard).registerStat();
        ModAchievements.achievementMineOneOre = new MineColoniesAchievement("miner.oresMined.one", 4, 9, Blocks.COAL_ORE,
                                                                             ModAchievements.achievementBuildingMiner).registerStat();
        ModAchievements.achievementMineOneDiamond = new MineColoniesAchievement("miner.diamondsMined.one", 4, 10, Items.DIAMOND,
                                                                                 ModAchievements.achievementBuildingMiner).registerStat();
        ModAchievements.achievementFellOneTree = new MineColoniesAchievement("lumberjack.treesFelled.one", 4, 12, Blocks.LOG,
                                                                              ModAchievements.achievementBuildingLumberjack).registerStat();
        ModAchievements.achievementPlantOneSapling = new MineColoniesAchievement("lumberjack.saplingsPlanted.one", 4, 13, Blocks.SAPLING,
                                                                                  ModAchievements.achievementBuildingLumberjack).registerStat();
        ModAchievements.achievementHarvestOneCarrot = new MineColoniesAchievement("farmer.carrotsHarvested.one", 4, 15, Items.CARROT,
                                                                                   ModAchievements.achievementBuildingFarmer).registerStat();
        ModAchievements.achievementHarvestOnePotato = new MineColoniesAchievement("farmer.potatoesHarvested.one", 4, 16, Items.POTATO,
                                                                                   ModAchievements.achievementBuildingFarmer).registerStat();
        ModAchievements.achievementHarvestOneWheat = new MineColoniesAchievement("farmer.wheatHarvested.one", 4, 17, Items.WHEAT_SEEDS,
                                                                                  ModAchievements.achievementBuildingFarmer).registerStat();

        /**
         * Do something for the 25th time.
         */
        ModAchievements.achievementBuild25Huts = new MineColoniesAchievement("builder.hutsBuilt.25", 6, 3, ModBlocks.blockHutCitizen,
                                                                              ModAchievements.achievementBuildOneHut).registerStat();
        ModAchievements.achievementCatch25Fish = new MineColoniesAchievement("fisher.fishCaught.25", 6, 5, Items.FISH,
                                                                              ModAchievements.achievementCatchOneFish).registerStat();
        ModAchievements.achievementKill25Mobs = new MineColoniesAchievement("guard.mobsKilled.25", 6, 7, Items.ROTTEN_FLESH,
                                                                             ModAchievements.achievementKillOneMob).registerStat();
        ModAchievements.achievementMine25Ores = new MineColoniesAchievement("miner.oresMined.25", 6, 9, Blocks.IRON_ORE,
                                                                             ModAchievements.achievementMineOneOre).registerStat();
        ModAchievements.achievementMine25Diamonds = new MineColoniesAchievement("miner.diamondsMined.25", 6, 10, Items.DIAMOND,
                                                                                 ModAchievements.achievementMineOneDiamond).registerStat();
        ModAchievements.achievementFell25Trees = new MineColoniesAchievement("lumberjack.treesFelled.25", 6, 12, Blocks.LOG,
                                                                              ModAchievements.achievementFellOneTree).registerStat();
        ModAchievements.achievementPlant25Saplings = new MineColoniesAchievement("lumberjack.saplingsPlanted.25", 6, 13, Blocks.SAPLING,
                                                                                  ModAchievements.achievementPlantOneSapling).registerStat();
        ModAchievements.achievementHarvest25Carrots = new MineColoniesAchievement("farmer.carrotsHarvested.25", 6, 15, Items.CARROT,
                                                                                   ModAchievements.achievementHarvestOneCarrot).registerStat();
        ModAchievements.achievementHarvest25Potatoes = new MineColoniesAchievement("farmer.potatoesHarvested.25", 6, 16, Items.POTATO,
                                                                                    ModAchievements.achievementHarvestOnePotato).registerStat();
        ModAchievements.achievementHarvest25Wheat = new MineColoniesAchievement("farmer.wheatHarvested.25", 6, 17, Items.WHEAT,
                                                                                 ModAchievements.achievementHarvestOneWheat).registerStat();

        /**
         * Do something for the 100th time.
         */
        ModAchievements.achievementBuild100Huts = new MineColoniesAchievement("builder.hutsBuilt.100", 8, 3, ModBlocks.blockHutCitizen,
                                                                               ModAchievements.achievementBuild25Huts).registerStat();
        ModAchievements.achievementCatch100Fish = new MineColoniesAchievement("fisher.fishCaught.100", 8, 5, Items.FISH,
                                                                               ModAchievements.achievementCatch25Fish).registerStat();
        ModAchievements.achievementKill100Mobs = new MineColoniesAchievement("guard.mobsKilled.100", 8, 7, Items.GUNPOWDER,
                                                                              ModAchievements.achievementKill25Mobs).registerStat();
        ModAchievements.achievementMine100Ores = new MineColoniesAchievement("miner.oresMined.100", 8, 9, Blocks.REDSTONE_ORE,
                                                                              ModAchievements.achievementMine25Ores).registerStat();
        ModAchievements.achievementMine100Diamonds = new MineColoniesAchievement("miner.diamondsMined.100", 8, 10, Items.DIAMOND,
                                                                                  ModAchievements.achievementMine25Diamonds).registerStat();
        ModAchievements.achievementFell100Trees = new MineColoniesAchievement("lumberjack.treesFelled.100", 8, 12, Blocks.LOG,
                                                                               ModAchievements.achievementFell25Trees).registerStat();
        ModAchievements.achievementPlant100Saplings = new MineColoniesAchievement("lumberjack.saplingsPlanted.100", 8, 13, Blocks.SAPLING,
                                                                                   ModAchievements.achievementPlant25Saplings).registerStat();
        ModAchievements.achievementHarvest100Carrots = new MineColoniesAchievement("farmer.carrotsHarvested.100", 8, 15, Items.CARROT,
                                                                                    ModAchievements.achievementHarvest25Carrots).registerStat();
        ModAchievements.achievementHarvest100Potatoes = new MineColoniesAchievement("farmer.potatoesHarvested.100", 8, 16, Items.POTATO,
                                                                                     ModAchievements.achievementHarvest25Potatoes).registerStat();
        ModAchievements.achievementHarvest100Wheat = new MineColoniesAchievement("farmer.wheatHarvested.100", 8, 17, Items.WHEAT,
                                                                                  ModAchievements.achievementHarvest25Wheat).registerStat();

        /**
         * Do something for the 500th time.
         */
        ModAchievements.achievementBuild500Huts = new MineColoniesAchievement("builder.hutsBuilt.500", 10, 3, ModBlocks.blockHutCitizen,
                                                                               ModAchievements.achievementBuild100Huts).registerStat();
        ModAchievements.achievementCatch500Fish = new MineColoniesAchievement("fisher.fishCaught.500", 10, 5, Items.FISH,
                                                                               ModAchievements.achievementCatch100Fish).registerStat();
        ModAchievements.achievementKill500Mobs = new MineColoniesAchievement("guard.mobsKilled.500", 10, 7, Items.ENDER_PEARL,
                                                                              ModAchievements.achievementKill100Mobs).registerStat();
        ModAchievements.achievementMine500Ores = new MineColoniesAchievement("miner.oresMined.500", 10, 9, Blocks.GOLD_ORE,
                                                                              ModAchievements.achievementMine100Ores).registerStat();
        ModAchievements.achievementMine500Diamonds = new MineColoniesAchievement("miner.diamondsMined.500", 10, 10, Items.DIAMOND,
                                                                                  ModAchievements.achievementMine100Diamonds).registerStat();
        ModAchievements.achievementFell500Trees = new MineColoniesAchievement("lumberjack.treesFelled.500", 10, 12, Blocks.LOG,
                                                                               ModAchievements.achievementFell100Trees).registerStat();
        ModAchievements.achievementPlant500Saplings = new MineColoniesAchievement("lumberjack.saplingsPlanted.500", 10, 13, Blocks.SAPLING,
                                                                                   ModAchievements.achievementPlant100Saplings).registerStat();
        ModAchievements.achievementHarvest500Carrots = new MineColoniesAchievement("farmer.carrotsHarvested.500", 10, 15, Items.CARROT,
                                                                                    ModAchievements.achievementHarvest100Carrots).registerStat();
        ModAchievements.achievementHarvest500Potatoes = new MineColoniesAchievement("farmer.potatoesHarvested.500", 10, 16, Items.POTATO,
                                                                                     ModAchievements.achievementHarvest100Potatoes).registerStat();
        ModAchievements.achievementHarvest500Wheat = new MineColoniesAchievement("farmer.wheatHarvested.500", 10, 17, Items.WHEAT,
                                                                                  ModAchievements.achievementHarvest100Wheat).registerStat();

        /**
         * Do something for the 1000th time.
         */
        ModAchievements.achievementBuild1000Huts = new MineColoniesAchievement("builder.hutsBuilt.1000", 12, 3, ModBlocks.blockHutCitizen,
                                                                                ModAchievements.achievementBuild500Huts).registerStat();
        ModAchievements.achievementCatch1000Fish = new MineColoniesAchievement("fisher.fishCaught.1000", 12, 5, Items.FISH,
                                                                                ModAchievements.achievementCatch500Fish).registerStat();
        ModAchievements.achievementKill1000Mobs = new MineColoniesAchievement("guard.mobsKilled.1000", 12, 7, Items.ENDER_EYE,
                                                                               ModAchievements.achievementKill500Mobs).registerStat();
        ModAchievements.achievementMine1000Ores = new MineColoniesAchievement("miner.oresMined.1000", 12, 9, Blocks.LAPIS_ORE,
                                                                               ModAchievements.achievementMine500Ores).registerStat();
        ModAchievements.achievementMine1000Diamonds = new MineColoniesAchievement("miner.diamondsMined.1000", 12, 10, Items.DIAMOND,
                                                                                   ModAchievements.achievementMine500Diamonds).registerStat();
        ModAchievements.achievementFell1000Trees = new MineColoniesAchievement("lumberjack.treesFelled.1000", 12, 12, Blocks.LOG,
                                                                                ModAchievements.achievementFell500Trees).registerStat();
        ModAchievements.achievementPlant1000Saplings = new MineColoniesAchievement("lumberjack.saplingsPlanted.1000", 12, 13, Blocks.SAPLING,
                                                                                    ModAchievements.achievementPlant500Saplings).registerStat();
        ModAchievements.achievementHarvest1000Carrots = new MineColoniesAchievement("farmer.carrotsHarvested.1000", 12, 15, Items.GOLDEN_CARROT,
                                                                                     ModAchievements.achievementHarvest500Carrots).registerStat();
        ModAchievements.achievementHarvest1000Potatoes = new MineColoniesAchievement("farmer.potatoesHarvested.1000", 12, 16, Items.POTATO,
                                                                                      ModAchievements.achievementHarvest500Potatoes).registerStat();
        ModAchievements.achievementHarvest1000Wheat = new MineColoniesAchievement("farmer.wheatHarvested.1000", 12, 17, Blocks.HAY_BLOCK,
                                                                                   ModAchievements.achievementHarvest500Wheat).registerStat();

        // Sizes
        /**
         * Reach {@link Constants#ACHIEVEMENT_SIZE_SETTLEMENT} citizens.
         */
        ModAchievements.achievementSizeSettlement = new MineColoniesAchievement("size.settlement", 4, 1, ModItems.itemAchievementProxySettlement,
                                                                                 ModAchievements.achievementTownhall).registerStat();
        /**
         * Reach {@link Constants#ACHIEVEMENT_SIZE_TOWN} citizens.
         */
        ModAchievements.achievementSizeTown = new MineColoniesAchievement("size.town", 6, 1, ModItems.itemAchievementProxyTown, ModAchievements.achievementSizeSettlement)
                                                .registerStat();
        /**
         * Reach {@link Constants#ACHIEVEMENT_SIZE_CITY} citizens.
         */
        ModAchievements.achievementSizeCity = new MineColoniesAchievement("size.city", 8, 1, ModItems.itemAchievementProxyCity, ModAchievements.achievementSizeTown)
                                                .registerStat();

        /**
         * Reach {@link Constants#ACHIEVEMENT_SIZE_METROPOLIS} citizens.
         */
        ModAchievements.achievementSizeMetropolis =
          new MineColoniesAchievement("size.metropolis", 10, 1, ModItems.itemAchievementProxyMetropolis, ModAchievements.achievementSizeCity)
            .registerStat();

        // Achievement pages
        /**
         * The MineColonies achievement page.
         */
        ModAchievements.achievementPageMineColonies = new MineColoniesAchievementPage(
                                                                                       Constants.MOD_NAME,
                                                                                       ModAchievements.achievementGetSupply,
                                                                                       ModAchievements.achievementWandOfbuilding,
                                                                                       ModAchievements.achievementTownhall,
                                                                                       ModAchievements.achievementBuildingTownhall,
                                                                                       ModAchievements.achievementUpgradeTownhallMax,
                                                                                       ModAchievements.achievementBuildingBuilder,
                                                                                       ModAchievements.achievementBuildingColonist,
                                                                                       ModAchievements.achievementBuildingLumberjack,
                                                                                       ModAchievements.achievementBuildingMiner,
                                                                                       ModAchievements.achievementBuildingFisher,
                                                                                       ModAchievements.achievementSizeSettlement,
                                                                                       ModAchievements.achievementSizeTown,
                                                                                       ModAchievements.achievementSizeCity,
                                                                                       ModAchievements.achievementUpgradeColonistMax,
                                                                                       ModAchievements.achievementUpgradeBuilderMax,
                                                                                       ModAchievements.achievementUpgradeLumberjackMax,
                                                                                       ModAchievements.achievementUpgradeMinerMax,
                                                                                       ModAchievements.achievementUpgradeFisherMax,
                                                                                       ModAchievements.achievementSizeMetropolis,
                                                                                       ModAchievements.achievementBuildingFarmer,
                                                                                       ModAchievements.achievementUpgradeFarmerMax,
                                                                                       ModAchievements.achievementBuildingGuard,
                                                                                       ModAchievements.achievementUpgradeGuardMax,
                                                                                       ModAchievements.achievementKillOneMob,
                                                                                       ModAchievements.achievementKill25Mobs,
                                                                                       ModAchievements.achievementKill100Mobs,
                                                                                       ModAchievements.achievementKill500Mobs,
                                                                                       ModAchievements.achievementKill1000Mobs,
                                                                                       ModAchievements.achievementMineOneOre,
                                                                                       ModAchievements.achievementMine25Ores,
                                                                                       ModAchievements.achievementMine100Ores,
                                                                                       ModAchievements.achievementMine500Ores,
                                                                                       ModAchievements.achievementMine1000Ores,
                                                                                       ModAchievements.achievementMineOneDiamond,
                                                                                       ModAchievements.achievementMine25Diamonds,
                                                                                       ModAchievements.achievementMine100Diamonds,
                                                                                       ModAchievements.achievementMine500Diamonds,
                                                                                       ModAchievements.achievementMine1000Diamonds,
                                                                                       ModAchievements.achievementBuildOneHut,
                                                                                       ModAchievements.achievementBuild25Huts,
                                                                                       ModAchievements.achievementBuild100Huts,
                                                                                       ModAchievements.achievementBuild500Huts,
                                                                                       ModAchievements.achievementBuild1000Huts,
                                                                                       ModAchievements.achievementCatchOneFish,
                                                                                       ModAchievements.achievementCatch25Fish,
                                                                                       ModAchievements.achievementCatch100Fish,
                                                                                       ModAchievements.achievementCatch500Fish,
                                                                                       ModAchievements.achievementCatch1000Fish,
                                                                                       ModAchievements.achievementHarvestOneCarrot,
                                                                                       ModAchievements.achievementHarvest25Carrots,
                                                                                       ModAchievements.achievementHarvest100Carrots,
                                                                                       ModAchievements.achievementHarvest500Carrots,
                                                                                       ModAchievements.achievementHarvest1000Carrots,
                                                                                       ModAchievements.achievementHarvestOnePotato,
                                                                                       ModAchievements.achievementHarvest25Potatoes,
                                                                                       ModAchievements.achievementHarvest100Potatoes,
                                                                                       ModAchievements.achievementHarvest500Potatoes,
                                                                                       ModAchievements.achievementHarvest1000Potatoes,
                                                                                       ModAchievements.achievementHarvestOneWheat,
                                                                                       ModAchievements.achievementHarvest25Wheat,
                                                                                       ModAchievements.achievementHarvest100Wheat,
                                                                                       ModAchievements.achievementHarvest500Wheat,
                                                                                       ModAchievements.achievementHarvest1000Wheat,
                                                                                       ModAchievements.achievementFellOneTree,
                                                                                       ModAchievements.achievementFell25Trees,
                                                                                       ModAchievements.achievementFell100Trees,
                                                                                       ModAchievements.achievementFell500Trees,
                                                                                       ModAchievements.achievementFell1000Trees,
                                                                                       ModAchievements.achievementPlantOneSapling,
                                                                                       ModAchievements.achievementPlant25Saplings,
                                                                                       ModAchievements.achievementPlant100Saplings,
                                                                                       ModAchievements.achievementPlant500Saplings,
                                                                                       ModAchievements.achievementPlant1000Saplings,
                                                                                       ModAchievements.achievementMinerDeathLava,
                                                                                       ModAchievements.achievementMinerDeathFall,
                                                                                       ModAchievements.achievementLumberjackDeathTree,
                                                                                       ModAchievements.achievementFisherDeathGuardian,
                                                                                       ModAchievements.achievementGuardDeathEnderman,
                                                                                       ModAchievements.achievementPlayerDeathGuard
        );

        ModAchievements.init();
    }

    @Override
    public void registerBlocks()
    {
        ModBlocks.blockHutTownHall = new BlockHutTownHall();
        ModBlocks.blockHutCitizen = new BlockHutCitizen();
        ModBlocks.blockHutMiner = new BlockHutMiner();
        ModBlocks.blockHutLumberjack = new BlockHutLumberjack();
        ModBlocks.blockHutBaker = new BlockHutBaker();
        ModBlocks.blockHutBuilder = new BlockHutBuilder();
        ModBlocks.blockHutDeliveryman = new BlockHutDeliveryman();
        ModBlocks.blockHutBlacksmith = new BlockHutBlacksmith();
        ModBlocks.blockHutStonemason = new BlockHutStonemason();
        ModBlocks.blockHutFarmer = new BlockHutFarmer();
        ModBlocks.blockHutFisherman = new BlockHutFisherman();
        ModBlocks.blockSubstitution = new BlockSubstitution();
        ModBlocks.blockSolidSubstitution = new BlockSolidSubstitution();
        ModBlocks.blockHutField = new BlockHutField();
        ModBlocks.blockHutGuardTower = new BlockHutGuardTower();
        ModBlocks.blockHutWareHouse = new BlockHutWareHouse();
        ModBlocks.blockConstructionTape = new BlockConstructionTape();
        ModBlocks.blockConstructionTapeCorner = new BlockConstructionTapeCorner();
    }

    @Override
    public void registerItems()
    {
        ModItems.supplyChest = new ItemSupplyChestDeployer();
        ModItems.buildTool = new ItemBuildTool();
        ModItems.scanTool = new ItemScanTool();
        ModItems.permTool = new ItemScepterPermission();
        ModItems.caliper = new ItemCaliper();
        ModItems.scepterGuard = new ItemScepterGuard();
        ModItems.supplyCamp = new ItemSupplyCampDeployer();

        ModItems.itemAchievementProxySettlement = new ItemAchievementProxy("sizeSettlement");
        ModItems.itemAchievementProxyTown = new ItemAchievementProxy("sizeTown");
        ModItems.itemAchievementProxyCity = new ItemAchievementProxy("sizeCity");
        ModItems.itemAchievementProxyMetropolis = new ItemAchievementProxy("sizeMetropolis");
    }

    @Override
    public void registerTileEntities()
    {
        GameRegistry.registerTileEntity(TileEntityColonyBuilding.class, Constants.MOD_ID + ".ColonyBuilding");
        GameRegistry.registerTileEntity(ScarecrowTileEntity.class, Constants.MOD_ID + ".Scarecrow");
        GameRegistry.registerTileEntity(TileEntityWareHouse.class, Constants.MOD_ID + ".WareHouse");

        NetworkRegistry.INSTANCE.registerGuiHandler(MineColonies.instance, new GuiHandler());
    }

    @Override
    public void registerEvents()
    {
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        MinecraftForge.EVENT_BUS.register(new FMLEventHandler());
        MinecraftForge.EVENT_BUS.register(new ConfigurationHandler());
    }

    /*
    * @param entityName A unique name for the entity
    * @param id A mod specific ID for the entity
    * @param mod The mod
    * @param trackingRange The range at which MC will send tracking updates
    * @param updateFrequency The frequency of tracking updates
    * @param sendsVelocityUpdates Whether to send velocity information packets as well
    * */
    @Override
    public void registerEntities()
    {
        ResourceLocation locationCitizen = new ResourceLocation(Constants.MOD_ID, "Citizen");
        ResourceLocation locationFishHook = new ResourceLocation(Constants.MOD_ID, "Fishhook");

        // Half as much tracking range and same update frequency as a player
        // See EntityTracker.addEntityToTracker for more default values
        EntityRegistry.registerModEntity(locationCitizen, EntityCitizen.class, "Citizen", getNextEntityId(), MineColonies.instance, 256, 2, true);
        EntityRegistry.registerModEntity(locationFishHook, EntityFishHook.class, "Fishhook", getNextEntityId(), MineColonies.instance, 250, 5, true);
    }

    @Override
    public void registerEntityRendering()
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void registerSounds()
    {
        ModSoundEvents.registerSounds();
    }

    @Override
    public void registerTileEntityRendering()
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void showCitizenWindow(final CitizenDataView citizen)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void openBuildToolWindow(final BlockPos pos)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void registerRenderer()
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public File getSchematicsFolder()
    {
        return null;
    }

    /**
     * Used for entity IDs, starts at 0 & increments for each call.
     */
    private int getNextEntityId()
    {
        return nextEntityId++;
    }
}
