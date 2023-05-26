package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic.DownwardsGrowingPlantModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import static com.minecolonies.api.research.util.ResearchConstants.PLANTATION_EXOTIC;

/**
 * Planter module for growing {@link Items#GLOW_BERRIES}.
 * <br/>
 * Requirements:
 * <ol>
 *     <li>All requirements from {@link DownwardsGrowingPlantModule}</li>
 * </ol>
 */
public class GlowBerriesPlantModule extends DownwardsGrowingPlantModule
{
    /**
     * Default constructor.
     *
     * @param fieldTag the tag of the field anchor block.
     * @param workTag  the tag of the working positions.
     * @param item     the item which is harvested.
     */
    public GlowBerriesPlantModule(final String fieldTag, final String workTag, final Item item)
    {
        super(fieldTag, workTag, item);
    }

    @Override
    protected boolean isValidHarvestBlock(final BlockState blockState)
    {
        return blockState.getBlock() == Blocks.CAVE_VINES || blockState.getBlock() == Blocks.CAVE_VINES_PLANT;
    }

    @Override
    public ResourceLocation getRequiredResearchEffect()
    {
        return PLANTATION_EXOTIC;
    }

    @Override
    public ToolType getRequiredTool()
    {
        return ToolType.NONE;
    }
}