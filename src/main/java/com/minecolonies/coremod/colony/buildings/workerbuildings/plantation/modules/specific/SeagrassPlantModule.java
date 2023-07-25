package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic.BoneMealedPlantModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

import static com.minecolonies.api.research.util.ResearchConstants.PLANTATION_SEA;

/**
 * Planter module for growing {@link Items#SEAGRASS}.
 * <br/>
 * Requirements:
 * <ol>
 *     <li>All requirements from {@link BoneMealedPlantModule}</li>
 *     <li>All working positions must have water directly overhead of them, else the seagrass won't be able to grow.</li>
 * </ol>
 */
public class SeagrassPlantModule extends BoneMealedPlantModule
{
    /**
     * Default constructor.
     *
     * @param field    the field instance this module is working on.
     * @param fieldTag the tag of the field anchor block.
     * @param workTag  the tag of the working positions.
     * @param item     the item which is harvested.
     */
    public SeagrassPlantModule(final IField field, final String fieldTag, final String workTag, final Item item)
    {
        super(field, fieldTag, workTag, item);
    }

    @Override
    public ResourceLocation getRequiredResearchEffect()
    {
        return PLANTATION_SEA;
    }

    @Override
    public void applyBonemeal(final AbstractEntityCitizen worker, final BlockPos workPosition, final ItemStack stackInSlot, final Player fakePlayer)
    {
        BoneMealItem.growWaterPlant(stackInSlot, worker.getLevel(), workPosition.above(), Direction.UP);
        BoneMealItem.addGrowthParticles(worker.getLevel(), workPosition.above(), 1);
    }

    @Override
    public ToolType getRequiredTool()
    {
        return ToolType.NONE;
    }

    @Override
    protected boolean isValidHarvestBlock(final BlockState blockState)
    {
        return blockState.getFluidState().is(Fluids.WATER) && (blockState.getBlock() == Blocks.SEAGRASS || blockState.getBlock() == Blocks.TALL_SEAGRASS);
    }

    @Override
    protected boolean isValidBonemealLocation(final BlockState blockState)
    {
        return blockState.is(Blocks.WATER);
    }

    @Override
    public @NonNull List<Item> getValidBonemeal()
    {
        // Only base minecraft bonemeal has water growing capabilities.
        // Compost (by design) should not inherit this functionality.
        return List.of(Items.BONE_MEAL);
    }
}