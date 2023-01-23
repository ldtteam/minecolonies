package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic.UpwardsGrowingPlantModule;
import com.minecolonies.coremod.entity.ai.citizen.planter.EntityAIWorkPlanter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.research.util.ResearchConstants.PLANTATION_SEA;

/**
 * Planter module for growing {@link Blocks#KELP}.
 */
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
     */
    public KelpPlantModule()
    {
        super("kelp_field", "kelp", Blocks.KELP);
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

    @Override
    protected int getMinimumPlantLength()
    {
        return MIN_HEIGHT;
    }

    @Override
    protected @Nullable Integer getMaximumPlantLength()
    {
        return MAX_HEIGHT;
    }

    @Override
    public ResourceLocation getRequiredResearchEffect()
    {
        return PLANTATION_SEA;
    }
}
