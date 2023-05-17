package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic.UpwardsGrowingPlantModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.research.util.ResearchConstants.PLANTATION_NETHER;

/**
 * Planter module for growing {@link Items#TWISTING_VINES}.
 * <br/>
 * Requirements:
 * <ol>
 *     <li>All requirements from {@link UpwardsGrowingPlantModule}</li>
 * </ol>
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
        super("twistv_field", "twistv_vine", Items.TWISTING_VINES);
    }

    @Override
    protected boolean isValidHarvestBlock(final BlockState blockState)
    {
        return blockState.getBlock() == Blocks.TWISTING_VINES || blockState.getBlock() == Blocks.TWISTING_VINES_PLANT;
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

    @Override
    public ToolType getRequiredTool()
    {
        return ToolType.NONE;
    }
}