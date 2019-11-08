package com.minecolonies.api.blocks;

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
    public static AbstractBlockHut blockHutTownHall;
    public static AbstractBlockHut blockHutHome;
    public static AbstractBlockHut blockHutMiner;
    public static AbstractBlockHut blockHutLumberjack;
    public static AbstractBlockHut blockHutBaker;
    public static AbstractBlockHut blockHutBuilder;
    public static AbstractBlockHut blockHutDeliveryman;
    public static AbstractBlockHut blockHutBlacksmith;
    public static AbstractBlockHut blockHutStonemason;
    public static AbstractBlockHut blockHutFarmer;
    public static AbstractBlockHut blockHutFisherman;
    public static AbstractBlockHut blockHutGuardTower;
    public static AbstractBlockHut blockHutWareHouse;
    public static AbstractBlockHut blockHutShepherd;
    public static AbstractBlockHut blockHutCowboy;
    public static AbstractBlockHut blockHutSwineHerder;
    public static AbstractBlockHut blockHutChickenHerder;
    public static AbstractBlockHut blockHutBarracks;
    public static AbstractBlockHut blockHutBarracksTower;
    public static AbstractBlockHut blockHutCook;
    public static AbstractBlockHut blockHutSmeltery;
    public static AbstractBlockHut blockHutComposter;
    public static AbstractBlockHut blockHutLibrary;
    public static AbstractBlockHut blockHutArchery;
    public static AbstractBlockHut blockHutCombatAcademy;
    public static AbstractBlockHut blockHutSawmill;
    public static AbstractBlockHut blockHutStoneSmeltery;
    public static AbstractBlockHut blockHutCrusher;
    public static AbstractBlockHut blockHutSifter;
    public static AbstractBlockHut blockPostBox;
    public static AbstractBlockHut blockHutFlorist;
    public static AbstractBlockHut blockHutEnchanter;

    /**
     * Utility blocks.
     */
    public static AbstractBlockMinecoloniesFalling    blockConstructionTape;
    public static AbstractBlockMinecolonies           blockRack;
    public static AbstractBlockMinecolonies           blockWayPoint;
    public static AbstractBlockMinecolonies           blockInfoPoster;
    public static AbstractBlockBarrel                 blockBarrel;
    public static AbstractBlockMinecoloniesHorizontal blockDecorationPlaceholder;
    public static AbstractBlockMinecolonies           blockScarecrow;
    public static AbstractBlockMinecolonies           blockBarracksTowerSubstitution;
    public static AbstractBlockMinecolonies           blockCompostedDirt;

    /**
     * Private constructor to hide the implicit public one.
     */
    private ModBlocks()
    {
    }
}
