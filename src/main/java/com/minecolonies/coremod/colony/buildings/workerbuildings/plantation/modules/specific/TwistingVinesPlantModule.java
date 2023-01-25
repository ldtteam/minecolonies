package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic.UpwardsGrowingPlantModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.research.util.ResearchConstants.PLANTATION_NETHER;

/**
 * Planter module for growing {@link Items#TWISTING_VINES}.
 */
public class TwistingVinesPlantModule extends UpwardsGrowingPlantModule
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
    public TwistingVinesPlantModule()
    {
        super("twistv_field", "vine", Items.TWISTING_VINES);
    }

    @Override
    protected boolean isValidBlock(final Block block)
    {
        return block == Blocks.TWISTING_VINES || block == Blocks.TWISTING_VINES_PLANT;
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
