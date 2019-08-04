package com.minecolonies.api.blocks;

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

    public static AbstractBlockMinecolonies blockHutTownHall;
    public static AbstractBlockMinecolonies blockHutCitizen;
    public static AbstractBlockMinecolonies blockHutMiner;
    public static AbstractBlockMinecolonies blockHutLumberjack;
    public static AbstractBlockMinecolonies blockHutBaker;
    public static AbstractBlockMinecolonies blockHutBuilder;
    public static AbstractBlockMinecolonies blockHutDeliveryman;
    public static AbstractBlockMinecolonies blockHutBlacksmith;
    public static AbstractBlockMinecolonies blockHutStonemason;
    public static AbstractBlockMinecolonies blockHutFarmer;
    public static AbstractBlockMinecolonies blockHutFisherman;
    public static AbstractBlockMinecolonies blockBarracksTowerSubstitution;
    public static AbstractBlockMinecolonies blockHutField;
    public static AbstractBlockMinecolonies blockHutGuardTower;
    public static AbstractBlockMinecolonies blockHutWareHouse;
    public static AbstractBlockMinecolonies blockHutShepherd;
    public static AbstractBlockMinecolonies blockHutCowboy;
    public static AbstractBlockMinecolonies blockHutSwineHerder;
    public static AbstractBlockMinecolonies blockHutChickenHerder;
    public static AbstractBlockMinecolonies blockHutBarracks;
    public static AbstractBlockMinecolonies blockHutBarracksTower;
    public static AbstractBlockMinecolonies blockHutCook;
    public static AbstractBlockMinecolonies blockHutSmeltery;
    public static AbstractBlockMinecolonies blockHutComposter;
    public static AbstractBlockMinecolonies blockHutLibrary;
    public static AbstractBlockMinecolonies blockHutArchery;
    public static AbstractBlockMinecolonies blockHutCombatAcademy;
    public static AbstractBlockMinecolonies blockHutSawmill;
    public static AbstractBlockMinecolonies blockHutStoneSmeltery;
    public static AbstractBlockMinecolonies blockHutCrusher;
    public static AbstractBlockMinecolonies blockHutSifter;

    /**
     * Utility blocks.
     */
    public static AbstractBlockMinecoloniesFalling    blockConstructionTape;
    public static AbstractBlockMinecolonies           blockRack;
    public static AbstractBlockMinecolonies           blockWayPoint;
    public static AbstractBlockMinecolonies           blockInfoPoster;
    public static AbstractBlockBarrel                 blockBarrel;
    public static AbstractBlockMinecolonies           blockPostBox;
    public static AbstractBlockMinecoloniesHorizontal blockDecorationPlaceholder;

    /**
     * Private constructor to hide the implicit public one.
     */
    private ModBlocks()
    {
    }

    @NotNull
    public static AbstractBlockHut[] getHuts()
    {
        return new AbstractBlockHut[] {
          (AbstractBlockHut) blockHutStoneSmeltery,
          (AbstractBlockHut) blockHutStonemason,
          (AbstractBlockHut) blockHutGuardTower,
          (AbstractBlockHut) blockHutArchery,
          (AbstractBlockHut) blockHutBaker,
          (AbstractBlockHut) blockHutBarracks,
          (AbstractBlockHut) blockHutBarracksTower,
          (AbstractBlockHut) blockHutBlacksmith,
          (AbstractBlockHut) blockHutBuilder,
          (AbstractBlockHut) blockHutChickenHerder,
          (AbstractBlockHut) blockHutCitizen,
          (AbstractBlockHut) blockHutCombatAcademy,
          (AbstractBlockHut) blockHutComposter,
          (AbstractBlockHut) blockHutCook,
          (AbstractBlockHut) blockHutCowboy,
          (AbstractBlockHut) blockHutCrusher,
          (AbstractBlockHut) blockHutArchery,
          (AbstractBlockHut) blockHutDeliveryman,
          (AbstractBlockHut) blockHutFarmer,
          (AbstractBlockHut) blockHutFisherman,
          (AbstractBlockHut) blockHutLibrary,
          (AbstractBlockHut) blockHutLumberjack,
          (AbstractBlockHut) blockHutMiner,
          (AbstractBlockHut) blockHutSawmill,
          (AbstractBlockHut) blockHutSifter,
          (AbstractBlockHut) blockHutShepherd,
          (AbstractBlockHut) blockHutSmeltery,
          (AbstractBlockHut) blockHutSwineHerder,
          (AbstractBlockHut) blockHutTownHall};
    }
}
