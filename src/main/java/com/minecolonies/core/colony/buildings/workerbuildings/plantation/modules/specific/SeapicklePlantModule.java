package com.minecolonies.core.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.items.ModToolTypes;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.core.colony.buildings.workerbuildings.plantation.modules.generic.BoneMealedPlantModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
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
     * The maximum amount of sea pickles that can be planted.
     */
    private static final int MAX_PLANTS = 10;

    /**
     * Default constructor.
     *
     * @param field    the field instance this module is working on.
     * @param fieldTag the tag of the field anchor block.
     * @param workTag  the tag of the working positions.
     * @param item     the item which is harvested.
     */
    public SeapicklePlantModule(final IField field, final String fieldTag, final String workTag, final Item item)
    {
        super(field, fieldTag, workTag, item);
    }

    @Override
    public IToolType getRequiredTool()
    {
        return ModToolTypes.none.get();
    }

    @Override
    public PlantationModuleResult.Builder decideFieldWork(final Level world, final @NotNull BlockPos workPosition)
    {
        // Sea pickles do not work entirely the same as regular bonemeal fields,
        // because they actually require having manually planted the pickles and then continue to grow them
        // by applying additional bonemeal.
        BlockState state = world.getBlockState(workPosition.above());
        if (state.getBlock().equals(Blocks.WATER))
        {
            return new PlantationModuleResult.Builder().plant(workPosition.above()).pickNewPosition();
        }
        else
        {
            return super.decideFieldWork(world, workPosition);
        }
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
    protected boolean isValidBonemealLocation(final BlockState blockState)
    {
        if (blockState.getBlock() instanceof SeaPickleBlock)
        {
            final Integer value = blockState.getValue(SeaPickleBlock.PICKLES);
            return value < SeaPickleBlock.MAX_PICKLES;
        }
        return super.isValidBonemealLocation(blockState);
    }

    @Override
    protected int getMaxWorkingPositions()
    {
        return MAX_PLANTS;
    }

    @Override
    public @NonNull List<Item> getValidBonemeal()
    {
        // Only base minecraft bonemeal has water growing capabilities.
        // Compost (by design) should not inherit this functionality.
        return List.of(Items.BONE_MEAL);
    }

    @Override
    public void applyBonemeal(final AbstractEntityCitizen worker, final BlockPos workPosition, final ItemStack stackInSlot, final Player fakePlayer)
    {
        BoneMealItem.applyBonemeal(stackInSlot, worker.level(), workPosition.above(), fakePlayer);
        BoneMealItem.addGrowthParticles(worker.level(), workPosition.above(), 1);
    }
}