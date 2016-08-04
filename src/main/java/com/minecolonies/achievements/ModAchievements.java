package com.minecolonies.achievements;

import com.minecolonies.blocks.ModBlocks;
import com.minecolonies.items.ModItems;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;

/**
 * Achievement collection
 *
 * @author Isfirs
 * @since 0.1
 */
public class ModAchievements
{

    // Achievements
    public static Achievement achGetSupply      = new AchGetSupply("get.supply", "get.supply", -2, -2);
    public static Achievement achWandOfbuilding = new AchWandOfBuilding("wandofbuilding", "wandofbuilding", 0, -2);
    public static Achievement achTownhall       = new AchTownhall("townhall", "townhall", -1, 0);
    public static Achievement achBuilder        = new AchBuilder("builder", "builder", 1, 0);
    public static Achievement achPioneers = new AchPioneers("size.pioneer", "", 0, 0);
    public static Achievement achHdideout;
    public static Achievement achTown;
    public static Achievement achCity;

    // Achievement pages
    public static AchievementPage pageMineColonies = new AchievementPageMineColonies(
            achGetSupply,
            achWandOfbuilding,
            achTownhall,
            achBuilder);

    /**
     * private constructor to hide the implicit public one.
     */
    private ModAchievements()
    {
    }

}
