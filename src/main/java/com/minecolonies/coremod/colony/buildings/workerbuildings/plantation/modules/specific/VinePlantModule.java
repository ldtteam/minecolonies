package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic.TreeSidePlantModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import static com.minecolonies.api.research.util.ResearchConstants.PLANTATION_JUNGLE;

/**
 * Planter module for growing {@link Items#VINE}.
 * Planting of vines is not possible, it is only harvested on tree sides when upper blocks let it grow down.
 * This is because vines have natural generation (in any direction), their growth cycle revolves around spreading to other blocks.
 * <br/>
 * Requirements:
 * <ol>
 *     <li>All requirements from {@link TreeSidePlantModule}</li>
 *     <li>There must be some vines nearby the marked tagged working positions, this is needed because the worker cannot plant these vines himself.</li>
 * </ol>
 */
public class VinePlantModule extends TreeSidePlantModule
{
    /**
     * Default constructor.
     */
    public VinePlantModule()
    {
        super("vine_field", "vine", Items.VINE);
    }

    @Override
    protected boolean isValidPlantingBlock(final BlockState blockState)
    {
        return false;
    }

    @Override
    protected boolean isValidClearingBlock(final BlockState blockState)
    {
        return !blockState.isAir() && blockState.getBlock() != Blocks.VINE;
    }

    @Override
    protected boolean isValidHarvestBlock(final BlockState blockState)
    {
        return blockState.getBlock() == Blocks.VINE;
    }

    @Override
    public ResourceLocation getRequiredResearchEffect()
    {
        return PLANTATION_JUNGLE;
    }

    @Override
    public ToolType getRequiredTool()
    {
        return ToolType.NONE;
    }
}