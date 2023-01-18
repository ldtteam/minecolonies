package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.PlantationModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic.UpwardsGrowingPlantModule;
import com.minecolonies.coremod.entity.ai.citizen.planter.EntityAIWorkPlanter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class KelpPlantModule extends UpwardsGrowingPlantModule
{
    /**
     * The minimum height kelp can grow to.
     */
    private static final int MIN_HEIGHT = 2;

    /**
     * The maximum height kelp can grow to.
     */
    private static final int MAX_HEIGHT = 25;

    /**
     * Default constructor.
     *
     * @param fieldTag               the tag of the field anchor block.
     * @param workTag                the tag of the working positions.
     * @param block                  the block which is harvested.
     * @param maxPlants              the maximum allowed plants.
     * @param plantsToRequest        the amount of plants to request when the planter has none left.
     * @param requiredResearchEffect the research effect required before this field type can be used.
     * @param minimumPlantLength     the minimum length for this plant to grow to before it can be harvested.
     * @param maximumPlantLength     the maximum length this plant can grow.
     */
    private KelpPlantModule(
      final String fieldTag,
      final String workTag,
      final Block block,
      final int maxPlants,
      final int plantsToRequest,
      final ResourceLocation requiredResearchEffect,
      final int minimumPlantLength,
      final Integer maximumPlantLength)
    {
        super(fieldTag, workTag, block, maxPlants, plantsToRequest, requiredResearchEffect, minimumPlantLength, maximumPlantLength);
    }

    @Override
    protected boolean walkToWorkPosition(final EntityAIWorkPlanter planterAI, final Level level, final BlockPos workPosition)
    {
        // Attempt to initially find an air block somewhere above the kelp planting position, so that we have a valid position
        // that the AI can actually walk to.
        for (int i = 0; i < MAX_HEIGHT + 1; i++)
        {
            if (level.getBlockState(workPosition.above(i)).isAir())
            {
                return super.walkToWorkPosition(planterAI, level, workPosition.above(i));
            }
        }

        // This position is not reachable, return false, so we don't end up in a walking loop.
        return false;
    }

    public static class Builder extends PlantationModule.Builder<UpwardsGrowingPlantModule.Builder>
    {
        /**
         * Default constructor.
         *
         * @param fieldTag the tag of the field anchor block.
         * @param workTag  the tag of the working positions.
         * @param block    the block which is harvested.
         */
        public Builder(final String fieldTag, final String workTag, final Block block)
        {
            super(fieldTag, workTag, block);
        }

        @Override
        public PlantationModule build()
        {
            return new KelpPlantModule(fieldTag, workTag, block, maxPlants, plantsToRequest, requiredResearchEffect, MIN_HEIGHT, MAX_HEIGHT);
        }
    }
}
