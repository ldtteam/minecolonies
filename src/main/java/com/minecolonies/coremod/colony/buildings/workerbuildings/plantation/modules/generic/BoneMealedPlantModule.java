package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic;

import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.AbstractPlantationModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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
     * The internal random used to decide whether to work this field or not.
     */
    private final Random random;

    /**
     * Default constructor.
     *
     * @param field    the field instance this module is working on.
     * @param fieldTag the tag of the field anchor block.
     * @param workTag  the tag of the working positions.
     * @param item     the item which is harvested.
     */
    protected BoneMealedPlantModule(
      final IField field,
      final String fieldTag,
      final String workTag,
      final Item item)
    {
        super(field, fieldTag, workTag, item);
        this.random = new Random();
    }

    @Override
    public PlantationModuleResult.Builder decideFieldWork(final Level world, final @NotNull BlockPos workingPosition)
    {
        ActionToPerform action = decideWorkAction(world, workingPosition);
        return switch (action)
        {
            case HARVEST -> new PlantationModuleResult.Builder()
                              .harvest(workingPosition.above())
                              .pickNewPosition();
            case BONEMEAL -> new PlantationModuleResult.Builder()
                               .bonemeal(workingPosition)
                               .pickNewPosition();
            default -> PlantationModuleResult.NONE;
        };
    }

    /**
     * Responsible for deciding what action the AI is going to perform on the field.
     *
     * @param world        the world reference that can be used for block state lookups.
     * @param workPosition the position that has been chosen for work.
     * @return the {@link PlantationModuleResult} that the AI is going to perform.
     */
    private ActionToPerform decideWorkAction(Level world, BlockPos workPosition)
    {
        BlockState blockState = world.getBlockState(workPosition.above());
        if (isValidHarvestBlock(blockState))
        {
            return ActionToPerform.HARVEST;
        }

        if (isValidPlantingBlock(blockState))
        {
            return ActionToPerform.BONEMEAL;
        }

        return ActionToPerform.NONE;
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
    public @Nullable BlockPos getNextWorkingPosition(Level world)
    {
        // If there is anything to harvest, return the first position where a non-air block is present.
        BlockPos positionToHarvest = getPositionToHarvest(world);
        if (positionToHarvest != null)
        {
            return positionToHarvest;
        }

        // A bone-mealed field has no growth stage, since the growth stage is instant.
        // Therefore, we need to prevent the worker from constantly running around on this field only.
        // This is achieved by only allowing work when a percentage chance is met,
        // so that the field is not often considered as "needing work".
        int percentChance = Math.max(Math.min(getPercentageChance(), 100), 1);
        boolean willWork = random.nextFloat() < (percentChance / 100F);
        if (willWork && !getWorkingPositions().isEmpty())
        {
            // Get a random position on the field, which will act as the planting position.
            List<BlockPos> workingPositions = getWorkingPositions();
            int idx = random.nextInt(0, workingPositions.size());
            return workingPositions.get(idx);
        }
        return null;
    }

    /**
     * Get the first position which is harvestable on the ground.
     * Defaults to return any non-air position.
     *
     * @param world the world reference that can be used for block state lookups.
     * @return the position to harvest or null if no position needs harvesting.
     */
    @Nullable
    private BlockPos getPositionToHarvest(Level world)
    {
        return getWorkingPositions().stream()
                 .filter(pos -> isValidHarvestBlock(world.getBlockState(pos.above())))
                 .findFirst()
                 .orElse(null);
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
    public int getActionLimit()
    {
        return 1;
    }

    @Override
    public List<ItemStack> getRequiredItemsForOperation()
    {
        return getValidBonemeal().stream().map(ItemStack::new).toList();
    }

    @Override
    public List<Item> getValidBonemeal()
    {
        return List.of(Items.BONE_MEAL, ModItems.compost);
    }
}