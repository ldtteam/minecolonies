package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic.UpwardsGrowingPlantModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

import static com.minecolonies.api.research.util.ResearchConstants.PLANTATION_JUNGLE;

/**
 * Planter module for growing {@link Blocks#BAMBOO}.
 */
public class BambooPlantModule extends UpwardsGrowingPlantModule
{
    /**
     * The minimum height bamboo can grow to.
     */
    private static final int MIN_HEIGHT = 6;

    /**
     * Default constructor.
     */
    public BambooPlantModule()
    {
        super("bamboo_field", "bamboo", Blocks.BAMBOO);
    }

    @Override
    protected int getMinimumPlantLength()
    {
        return MIN_HEIGHT;
    }

    @Override
    public ResourceLocation getRequiredResearchEffect()
    {
        return PLANTATION_JUNGLE;
    }
}
