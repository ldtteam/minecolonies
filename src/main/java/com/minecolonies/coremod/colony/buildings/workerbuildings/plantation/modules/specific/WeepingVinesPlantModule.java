package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic.DownwardsGrowingPlantModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.research.util.ResearchConstants.PLANTATION_NETHER;

/**
 * Planter module for growing {@link Items#WEEPING_VINES}.
 */
public class WeepingVinesPlantModule extends DownwardsGrowingPlantModule
{
    /**
     * The minimum height twisting vines can grow to.
     */
    private static final int MIN_HEIGHT = 2;

    /**
     * The maximum height twisting vines can grow to.
     */
    private static final int MAX_HEIGHT = 25;

    /**
     * Default constructor.
     */
    public WeepingVinesPlantModule()
    {
        super("weepv_field", "vine", Items.WEEPING_VINES);
    }

    @Override
    protected @NotNull Block getExpectedBlock()
    {
        return Blocks.WEEPING_VINES;
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
        return PLANTATION_NETHER;
    }
}
