package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic.BoneMealedFieldPlantModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import static com.minecolonies.api.research.util.ResearchConstants.PLANTATION_NETHER;

/**
 * Planter module for growing {@link Items#WARPED_FUNGUS} and {@link Items#WARPED_ROOTS}.
 */
public class WarpedPlantsPlantModule extends BoneMealedFieldPlantModule
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
    public WarpedPlantsPlantModule()
    {
        super("warpedp_field", "plant", Items.WARPED_FUNGUS);
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
    public int getMaxPlants()
    {
        return MAX_PLANTS;
    }
}