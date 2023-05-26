package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.fields.plantation.BasicPlanterAI;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic.BoneMealedPlantModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Planter module for growing {@link Items#SEAGRASS}.
 * <br/>
 * Requirements:
 * <ol>
 *     <li>All requirements from {@link BoneMealedPlantModule}</li>
 *     <li>All working positions must have water directly overhead of them, else the seagrass won't be able to grow.</li>
 * </ol>
 */
public class SeapicklePlantModule extends BoneMealedPlantModule
{
    /**
     * Default constructor.
     *
     * @param fieldTag the tag of the field anchor block.
     * @param workTag  the tag of the working positions.
     * @param item     the item which is harvested.
     */
    public SeapicklePlantModule(final String fieldTag, final String workTag, final Item item)
    {
        super(fieldTag, workTag, item);
    }

    @Override
    public ToolType getRequiredTool()
    {
        return ToolType.NONE;
    }

    @Override
    public PlanterAIModuleResult workField(
      final @NotNull IField field,
      final @NotNull BasicPlanterAI planterAI,
      final @NotNull AbstractEntityCitizen worker,
      final @NotNull BlockPos workPosition,
      final @NotNull FakePlayer fakePlayer)
    {
        // Sea pickles do not work entirely the same as regular bonemeal fields,
        // because they actually require having manually planted the pickles and then continue to grow them
        // by applying additional bonemeal.

        BlockState state = field.getColony().getWorld().getBlockState(workPosition.above());
        if (state.getBlock().equals(Blocks.WATER))
        {
            return planterAI.planterPlaceBlock(workPosition.above(), getItem(), getPlantsToRequest()) ? PlanterAIModuleResult.PLANTED : PlanterAIModuleResult.PLANTING;
        }
        else
        {
            return super.workField(field, planterAI, worker, workPosition, fakePlayer);
        }
    }

    @Override
    protected @NonNull List<Item> getValidBonemeal()
    {
        // Only base minecraft bonemeal has water growing capabilities.
        // Compost (by design) should not inherit this functionality.
        return List.of(Items.BONE_MEAL);
    }

    @Override
    protected void applyBonemeal(final AbstractEntityCitizen worker, final BlockPos workPosition, final ItemStack stackInSlot, final Player fakePlayer)
    {
        BoneMealItem.applyBonemeal(stackInSlot, worker.getLevel(), workPosition.above(), fakePlayer);
        BoneMealItem.addGrowthParticles(worker.getLevel(), workPosition.above(), 1);
    }

    @Override
    protected boolean isValidHarvestBlock(final BlockState blockState)
    {
        if (blockState.getBlock() instanceof SeaPickleBlock)
        {
            final Integer value = blockState.getValue(SeaPickleBlock.PICKLES);
            return value >= SeaPickleBlock.MAX_PICKLES;
        }
        return super.isValidHarvestBlock(blockState);
    }

    @Override
    protected boolean isValidPlantingBlock(final BlockState blockState)
    {
        if (blockState.getBlock() instanceof SeaPickleBlock)
        {
            final Integer value = blockState.getValue(SeaPickleBlock.PICKLES);
            return value < SeaPickleBlock.MAX_PICKLES;
        }
        return super.isValidPlantingBlock(blockState);
    }
}