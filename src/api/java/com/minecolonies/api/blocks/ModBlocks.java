package com.minecolonies.api.blocks;

import com.minecolonies.coremod.blocks.*;
import com.minecolonies.coremod.blocks.decorative.*;
import com.minecolonies.coremod.blocks.huts.*;
import com.minecolonies.coremod.blocks.schematic.*;
import org.jetbrains.annotations.NotNull;

/**
 * Class to create the modBlocks.
 * References to the blocks can be made here
 * <p>
 * We disabled the following finals since we are neither able to mark the items as final, nor do we want to provide public accessors.
 */
@SuppressWarnings({"squid:ClassVariableVisibilityCheck", "squid:S2444", "squid:S1444", "squid:S1820",})
public final class ModBlocks
{
    /*
     * Creating objects for all blocks in the mod.
     * References can be made to here.
     */
    public static BlockHutTownHall blockHutTownHall;
    public static BlockHutCitizen blockHutHome;
    public static BlockHutMiner blockHutMiner;
    public static BlockHutLumberjack blockHutLumberjack;
    public static BlockHutBaker blockHutBaker;
    public static BlockHutBuilder blockHutBuilder;
    public static BlockHutDeliveryman blockHutDeliveryman;
    public static BlockHutBlacksmith blockHutBlacksmith;
    public static BlockHutStonemason blockHutStonemason;
    public static BlockHutFarmer blockHutFarmer;
    public static BlockHutFisherman blockHutFisherman;
    public static BlockHutGuardTower blockHutGuardTower;
    public static BlockHutWareHouse blockHutWareHouse;
    public static BlockHutShepherd blockHutShepherd;
    public static BlockHutCowboy blockHutCowboy;
    public static BlockHutSwineHerder blockHutSwineHerder;
    public static BlockHutChickenHerder blockHutChickenHerder;
    public static BlockHutBarracks blockHutBarracks;
    public static BlockHutBarracksTower blockHutBarracksTower;
    public static BlockHutCook blockHutCook;
    public static BlockHutSmeltery blockHutSmeltery;
    public static BlockHutComposter blockHutComposter;
    public static BlockHutLibrary blockHutLibrary;
    public static BlockHutArchery blockHutArchery;
    public static BlockHutCombatAcademy blockHutCombatAcademy;
    public static BlockHutSawmill blockHutSawmill;
    public static BlockHutStoneSmeltery blockHutStoneSmeltery;
    public static BlockHutCrusher blockHutCrusher;
    public static BlockHutSifter blockHutSifter;
    public static BlockPostBox blockPostBox;
    public static BlockHutFlorist blockHutFlorist;
    public static BlockHutEnchanter blockHutEnchanter;
    public static BlockHutUniversity blockHutUniversity;
    public static BlockHutHospital blockHutHospital;
    public static BlockStash blockStash;
    public static BlockHutSchool blockHutSchool;
    public static BlockHutGlassblower blockHutGlassblower;
    public static BlockHutDyer blockHutDyer;
    public static BlockHutFletcher blockHutFletcher;
    public static BlockHutMechanic blockHutMechanic;
    public static BlockHutPlantation blockHutPlantation;
    public static BlockHutTavern blockHutTavern;

    /**
     * Utility blocks.
     */
    public static BlockConstructionTape blockConstructionTape;
    public static BlockMinecoloniesRack blockRack;
    public static BlockWaypoint blockWayPoint;
    public static BlockBarrel blockBarrel;
    public static BlockDecorationController blockDecorationPlaceholder;
    public static BlockScarecrow blockScarecrow;
    public static BlockBarracksTowerSubstitution blockBarracksTowerSubstitution;
    public static BlockCompostedDirt blockCompostedDirt;

    /**
     * Private constructor to hide the implicit public one.
     */
    private ModBlocks()
    {
    }

    @NotNull
    public static AbstractBlockHut<?>[] getHuts()
    {
        return new AbstractBlockHut[] {blockHutStoneSmeltery, blockHutStonemason, blockHutGuardTower, blockHutArchery, blockHutBaker,
            blockHutBarracks, blockHutBarracksTower, blockHutBlacksmith, blockHutBuilder, blockHutChickenHerder, blockHutHome,
            blockHutCombatAcademy, blockHutComposter, blockHutCook, blockHutCowboy, blockHutCrusher, blockHutArchery, blockHutDeliveryman,
            blockHutFarmer, blockHutFisherman, blockHutLibrary, blockHutLumberjack, blockHutMiner, blockHutSawmill, blockHutSifter,
            blockHutShepherd, blockHutSmeltery, blockHutSwineHerder, blockHutTownHall, blockHutUniversity, blockHutHospital, blockHutSchool,
            blockHutEnchanter, blockHutGlassblower, blockHutDyer, blockHutFletcher, blockHutMechanic, blockHutPlantation, blockHutTavern};
    }
}
