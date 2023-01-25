package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic.BoneMealedFieldPlantModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import static com.minecolonies.api.research.util.ResearchConstants.PLANTATION_NETHER;

/**
 * Planter module for growing {@link Items#CRIMSON_FUNGUS} and {@link Items#CRIMSON_ROOTS}.
 */
public class CrimsonPlantsPlantModule extends BoneMealedFieldPlantModule
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
        super("crimsonp_field", "plant", Items.CRIMSON_FUNGUS);
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
