package com.minecolonies.api.blocks;

import com.minecolonies.api.blocks.decorative.AbstractBlockMinecoloniesConstructionTape;
import com.minecolonies.api.blocks.huts.AbstractBlockMinecoloniesDefault;
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
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutTownHall;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutHome;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutMiner;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutLumberjack;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutBaker;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutBuilder;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutDeliveryman;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutBlacksmith;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutStonemason;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutFarmer;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutFisherman;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutGuardTower;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutWareHouse;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutShepherd;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutCowboy;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutSwineHerder;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutChickenHerder;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutBarracks;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutBarracksTower;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutCook;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutSmeltery;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutComposter;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutLibrary;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutArchery;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutCombatAcademy;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutSawmill;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutStoneSmeltery;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutCrusher;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutSifter;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockPostBox;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutFlorist;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutEnchanter;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutUniversity;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutHospital;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockStash;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutSchool;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutGlassblower;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutDyer;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutFletcher;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutMechanic;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutPlantation;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutTavern;
    public static AbstractBlockHut<? extends AbstractBlockHut<?>> blockHutBeekeeper;

    /**
     * Utility blocks.
     */
    public static AbstractBlockMinecoloniesConstructionTape<? extends AbstractBlockMinecoloniesConstructionTape<?>> blockConstructionTape;
    public static AbstractBlockMinecoloniesRack<? extends AbstractBlockMinecoloniesRack<?>> blockRack;
    public static AbstractBlockMinecolonies<? extends AbstractBlockMinecolonies<?>> blockWayPoint;
    public static AbstractBlockBarrel<? extends AbstractBlockBarrel<?>> blockBarrel;
    public static AbstractBlockMinecoloniesHorizontal<? extends AbstractBlockMinecoloniesHorizontal<?>> blockDecorationPlaceholder;
    public static AbstractBlockMinecoloniesDefault<? extends AbstractBlockMinecoloniesDefault<?>> blockScarecrow;
    public static AbstractBlockMinecolonies<? extends AbstractBlockMinecolonies<?>> blockBarracksTowerSubstitution;
    public static AbstractBlockMinecolonies<? extends AbstractBlockMinecolonies<?>> blockCompostedDirt;

    /**
     * Private constructor to hide the implicit public one.
     */
    private ModBlocks()
    {

    }

    @NotNull
    public static AbstractBlockHut<?>[] getHuts()
    {
        return new AbstractBlockHut[] {
          blockHutStoneSmeltery,
          blockHutStonemason,
          blockHutGuardTower,
          blockHutArchery,
          blockHutBaker,
          blockHutBarracks,
          blockHutBarracksTower,
          blockHutBlacksmith,
          blockHutBuilder,
          blockHutChickenHerder,
          blockHutHome,
          blockHutCombatAcademy,
          blockHutComposter,
          blockHutCook,
          blockHutCowboy,
          blockHutCrusher,
          blockHutDeliveryman,
          blockHutFarmer,
          blockHutFisherman,
          blockHutLibrary,
          blockHutLumberjack,
          blockHutMiner,
          blockHutSawmill,
          blockHutSifter,
          blockHutShepherd,
          blockHutSmeltery,
          blockHutSwineHerder,
          blockHutTownHall,
          blockHutUniversity,
          blockHutHospital,
          blockHutSchool,
          blockHutEnchanter,
          blockHutGlassblower,
          blockHutDyer,
          blockHutFletcher,
          blockHutMechanic,
          blockHutPlantation,
          blockHutTavern,
          blockHutBeekeeper};
    }
}
