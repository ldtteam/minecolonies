package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic.BoneMealedPlantModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import static com.minecolonies.api.research.util.ResearchConstants.PLANTATION_NETHER;

/**
 * Planter module for growing {@link Items#CRIMSON_FUNGUS} and {@link Items#CRIMSON_ROOTS}.
 * <br/>
 * Requirements:
 * <ol>
 *     <li>All requirements from {@link BoneMealedPlantModule}</li>
 * </ol>
 */
public class CrimsonPlantsPlantModule extends BoneMealedPlantModule
{
    /**
     * The chance a worker has to work on this field.
     */
    private static final int CHANCE = 5;

    /**
     * The maximum amount of plants allowed on this field.
     */
    private static final int MAX_PLANTS = 50;

    /**
     * Default constructor.
     */
    public CrimsonPlantsPlantModule()
    {
        super("crimsonp_field", "crimsonp_ground", Items.CRIMSON_FUNGUS);
    }

    @Override
    protected int getPercentageChance()
    {
        return CHANCE;
    }

    @Override
    public ResourceLocation getRequiredResearchEffect()
    {
        return PLANTATION_NETHER;
    }

    @Override
    public ToolType getRequiredTool()
    {
        return ToolType.NONE;
    }

    @Override
    public int getMaxPlants()
    {
        return MAX_PLANTS;
    }
}