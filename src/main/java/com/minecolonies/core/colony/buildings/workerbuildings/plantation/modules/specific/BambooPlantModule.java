package com.minecolonies.core.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.tools.ModToolTypes;
import com.minecolonies.api.tools.registry.ToolTypeEntry;
import com.minecolonies.core.colony.buildings.workerbuildings.plantation.modules.generic.UpwardsGrowingPlantModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.research.util.ResearchConstants.PLANTATION_JUNGLE;

/**
 * Planter module for growing {@link Items#BAMBOO}.
 * <br/>
 * Requirements:
 * <ol>
 *     <li>All requirements from {@link UpwardsGrowingPlantModule}</li>
 * </ol>
 */
public class BambooPlantModule extends UpwardsGrowingPlantModule
{
    /**
     * The minimum height bamboo can grow to.
     */
    private static final int MIN_HEIGHT = 6;

    /**
     * The maximum height twisting vines can grow to.
     */
    private static final int MAX_HEIGHT = BambooStalkBlock.MAX_HEIGHT;

    /**
     * Default constructor.
     *
     * @param field    the field instance this module is working on.
     * @param fieldTag the tag of the field anchor block.
     * @param workTag  the tag of the working positions.
     * @param item     the item which is harvested.
     */
    public BambooPlantModule(final IField field, final String fieldTag, final String workTag, final Item item)
    {
        super(field, fieldTag, workTag, item);
    }

    @Override
    protected boolean isValidHarvestBlock(final BlockState blockState)
    {
        return blockState.getBlock() == Blocks.BAMBOO || blockState.getBlock() == Blocks.BAMBOO_SAPLING;
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
        return PLANTATION_JUNGLE;
    }

    @Override
    public ToolTypeEntry getRequiredTool()
    {
        return ModToolTypes.axe.get();
    }
}