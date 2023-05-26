package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic;

import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.fields.plantation.BasicPlanterAI;
import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.translation.RequestSystemTranslationConstants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.AbstractPlantationModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

/**
 * Plantation module for plants that grow on a flat piece of land when bone-mealed.
 * <br/>
 * Requirements:
 * <ol>
 *     <li>Soil blocks must be the same so plants can grow on them.</li>
 *     <li>Anything that grows on the field will be harvested, but only things that are 1 block high (multi block high things must break completely, else they are ignored).</li>
 * </ol>
 */
public abstract class BoneMealedPlantModule extends AbstractPlantationModule
{
    /**
     * The default percentage chance to be able to work on this field.
     */
    protected static final int DEFAULT_PERCENTAGE_CHANCE = 10;

    /**
     * The amount of bonemeal the worker should have at any time.
     */
    protected static final int BONEMEAL_TO_KEEP = 16;

    /**
     * The internal random used to decide whether to work this field or not.
     */
    private final Random random;

    /**
     * Default constructor.
     *
     * @param fieldTag the tag of the field anchor block.
     * @param workTag  the tag of the working positions.
     * @param item     the item which is harvested.
     */
    protected BoneMealedPlantModule(
      final String fieldTag,
      final String workTag,
      final Item item)
    {
        super(fieldTag, workTag, item);
        this.random = new Random();
    }

    @Override
    public PlanterAIModuleResult workField(
      final @NotNull IField field,
      final @NotNull BasicPlanterAI planterAI,
      final @NotNull AbstractEntityCitizen worker,
      final @NotNull BlockPos workPosition,
      final @NotNull FakePlayer fakePlayer)
    {
        if (!checkForBoneMeal(planterAI))
        {
            return PlanterAIModuleResult.REQUIRES_ITEMS;
        }

        PlanterAIModuleState action = decideWorkAction(field, workPosition);
        return switch (action)
        {
            case NONE -> PlanterAIModuleResult.NONE;
            case HARVESTING ->
            {
                // Walk to the position where work has to happen.
                if (planterAI.planterWalkToBlock(workPosition.above()))
                {
                    yield PlanterAIModuleResult.MOVING;
                }

                // Harvest the field block by block.
                final PlanterAIModuleResult harvestResult = getHarvestingResultFromMiningResult(planterAI.planterMineBlock(workPosition.above(), true));
                if (harvestResult != PlanterAIModuleResult.HARVESTED)
                {
                    yield harvestResult;
                }

                if (getPositionToHarvest(field) != null)
                {
                    // Return a custom result here indicates that we harvested something, however we do want to keep working on the field
                    // but on another position.
                    yield new PlanterAIModuleResult(PlanterAIModuleState.HARVESTED, PlanterAIModuleResultResetState.POSITION);
                }
                else
                {
                    // Return a custom result here indicates that we harvested something, however we want to completely abort working on the field right now.
                    // Therefore, we need to release the current field so that next tick a new field will be selected,
                    // running the needsWork check on the field again.
                    yield new PlanterAIModuleResult(PlanterAIModuleState.HARVESTED, PlanterAIModuleResultResetState.FIELD);
                }
            }
            case PLANTING ->
            {
                if (planterAI.planterWalkToBlock(workPosition))
                {
                    yield PlanterAIModuleResult.MOVING;
                }

                final int boneMealSlot =
                  InventoryUtils.findFirstSlotInItemHandlerWith(worker.getInventoryCitizen(), stack -> getValidBonemeal().contains(stack.getItem()));
                final ItemStack stackInSlot = worker.getInventoryCitizen().getStackInSlot(boneMealSlot);
                applyBonemeal(worker, workPosition, stackInSlot, fakePlayer);

                // Return a custom result that indicates we planted something, but we want to remain working on the field, at a different position.
                yield new PlanterAIModuleResult(PlanterAIModuleState.PLANTED, PlanterAIModuleResultResetState.POSITION);
            }
            default -> PlanterAIModuleResult.INVALID;
        };
    }

    /**
     * Check if the planter has bonemeal available.
     *
     * @param planterAI the AI class of the planter so instructions can be ordered to it.
     * @return whether the planter has bonemeal available.
     */
    private boolean checkForBoneMeal(BasicPlanterAI planterAI)
    {
        List<ItemStack> bonemeal = getValidBonemeal().stream().map(ItemStack::new).toList();
        return planterAI.requestItems(new StackList(bonemeal,
          RequestSystemTranslationConstants.REQUEST_TYPE_FERTILIZER,
          BONEMEAL_TO_KEEP,
          BONEMEAL_TO_KEEP));
    }

    /**
     * Responsible for deciding what action the AI is going to perform on the field.
     *
     * @param field        the field to check for.
     * @param workPosition the position that has been chosen for work.
     * @return the {@link PlanterAIModuleResult} that the AI is going to perform.
     */
    private PlanterAIModuleState decideWorkAction(IField field, BlockPos workPosition)
    {
        BlockState blockState = field.getColony().getWorld().getBlockState(workPosition.above());
        if (isValidHarvestBlock(blockState))
        {
            return PlanterAIModuleState.HARVESTING;
        }

        if (isValidPlantingBlock(blockState))
        {
            return PlanterAIModuleState.PLANTING;
        }

        return PlanterAIModuleState.NONE;
    }

    /**
     * Get the first position which is harvestable on the ground.
     * Defaults to return any non-air position.
     *
     * @param field the field to check on.
     * @return the position to harvest or null if no position needs harvesting.
     */
    @Nullable
    private BlockPos getPositionToHarvest(IField field)
    {
        return getWorkingPositions(field).stream()
                 .filter(pos -> isValidHarvestBlock(field.getColony().getWorld().getBlockState(pos.above())))
                 .findFirst()
                 .orElse(null);
    }

    /**
     * Returns which items are considered valid bonemeal items.
     */
    @NonNull
    protected List<Item> getValidBonemeal()
    {
        return List.of(Items.BONE_MEAL, ModItems.compost);
    }

    /**
     * Logic for applying bonemeal to the working position.
     *
     * @param worker       the worker entity working on the plantation.
     * @param workPosition the position that has been chosen for work.
     * @param stackInSlot  the item stack to use for the planting.
     * @param fakePlayer   a fake player class to use.
     */
    protected void applyBonemeal(AbstractEntityCitizen worker, BlockPos workPosition, ItemStack stackInSlot, Player fakePlayer)
    {
        BoneMealItem.applyBonemeal(stackInSlot, worker.getLevel(), workPosition, fakePlayer);
        BoneMealItem.addGrowthParticles(worker.getLevel(), workPosition, 1);
    }

    /**
     * Check if the block is a correct block for harvesting.
     * Defaults to check if the block state is not air.
     *
     * @param blockState the block state.
     * @return whether the block can be harvested.
     */
    protected boolean isValidHarvestBlock(BlockState blockState)
    {
        return !blockState.isAir();
    }

    /**
     * Check if the block is a correct block for planting.
     * Defaults to being any air block.
     *
     * @param blockState the block state.
     * @return whether the block can be planted.
     */
    protected boolean isValidPlantingBlock(BlockState blockState)
    {
        return blockState.isAir();
    }

    @Override
    public boolean needsWork(final IField field)
    {
        // A bone-mealed field has no growth stage, since the growth stage is instant.
        // Therefore, we need to prevent the worker from constantly running around on this field only.
        // This is achieved by only allowing work when a percentage chance is met,
        // so that the field is not often considered as "needing work".
        int percentChance = Math.max(Math.min(getPercentageChance(), 100), 1);
        boolean willWork = random.nextFloat() < (percentChance / 100F);
        return willWork && !getWorkingPositions(field).isEmpty();
    }

    /**
     * Get the chance in percentages of being able to perform work on this field when asked.
     * Defaults to {@link BoneMealedPlantModule#DEFAULT_PERCENTAGE_CHANCE}.
     *
     * @return a number between 1 and 100.
     */
    protected int getPercentageChance()
    {
        return DEFAULT_PERCENTAGE_CHANCE;
    }

    @Override
    public @Nullable BlockPos getNextWorkingPosition(final IField field)
    {
        // If there is anything to harvest, return the first position where a non-air block is present.
        BlockPos positionToHarvest = getPositionToHarvest(field);
        if (positionToHarvest != null)
        {
            return positionToHarvest;
        }

        // Get a random position on the field, which will act as the planting position.
        List<BlockPos> workingPositions = getWorkingPositions(field);
        int idx = random.nextInt(0, workingPositions.size());
        return workingPositions.get(idx);
    }

    @Override
    public List<ItemStack> getRequiredItemsForOperation()
    {
        return getValidBonemeal().stream().map(ItemStack::new).toList();
    }

    @Override
    public int getActionLimit()
    {
        return 1;
    }
}