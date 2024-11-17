package com.minecolonies.core.entity.ai.workers.crafting;

import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingConcreteMixer;
import com.minecolonies.core.colony.jobs.JobConcreteMixer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.*;

/**
 * Concrete mixer AI class.
 */
public class EntityAIConcreteMixer extends AbstractEntityAICrafting<JobConcreteMixer, BuildingConcreteMixer>
{
    /**
     * Predicate to check if concrete powder is in inv.
     */
    private static final Predicate<ItemStack> CONCRETE =
      stack -> !stack.isEmpty()
                 && stack.getItem() instanceof BlockItem
                 && ((BlockItem) stack.getItem()).getBlock() instanceof ConcretePowderBlock;

    /**
     * Constructor for the Concrete mason. Defines the tasks the Concrete mason executes.
     *
     * @param job a Concrete mason job to use.
     */
    public EntityAIConcreteMixer(@NotNull final JobConcreteMixer job)
    {
        super(job);
        super.registerTargets(
          new AITarget(CONCRETE_MIXER_PLACING, this::placePowder, TICKS_SECOND),
          new AITarget(CONCRETE_MIXER_HARVESTING, this::harvestConcrete, TICKS_SECOND)
        );
    }

    @Override
    public Class<BuildingConcreteMixer> getExpectedBuildingClass()
    {
        return BuildingConcreteMixer.class;
    }

    /**
     * Place concrete powder down into the water stream.
     *
     * @return the next AI state.
     */
    private IAIState placePowder()
    {
        final BlockPos posToPlace = building.getBlockToPlace();
        if (posToPlace == null)
        {
            return START_WORKING;
        }

        final int slot = getSlotWithPowder();
        if (slot == -1)
        {
            if (InventoryUtils.hasItemInItemHandler(building.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseGet(null), CONCRETE))
            {
                needsCurrently = new Tuple<>(CONCRETE, STACKSIZE);
                return GATHERING_REQUIRED_MATERIALS;
            }

            return START_WORKING;
        }

        if (walkToBlock(posToPlace))
        {
            return getState();
        }

        final ItemStack stack = worker.getInventoryCitizen().getStackInSlot(slot);
        final Block block = ((BlockItem) stack.getItem()).getBlock();
        if (InventoryUtils.attemptReduceStackInItemHandler(worker.getInventoryCitizen(), stack, 1))
        {
            world.setBlock(posToPlace, block.defaultBlockState().updateShape(Direction.DOWN, block.defaultBlockState(), world, posToPlace, posToPlace), UPDATE_FLAG);
        }

        return getState();
    }

    /**
     * Harvest concrete from the water stream.
     *
     * @return the next AI state.
     */
    private IAIState harvestConcrete()
    {
        final BlockPos posToMine = building.getBlockToMine();
        if (posToMine == null)
        {
            this.resetActionsDone();
            return START_WORKING;
        }

        if (walkToBlock(posToMine))
        {
            return getState();
        }

        final BlockState blockToMine = world.getBlockState(posToMine);
        if (mineBlock(posToMine))
        {
            incrementActionsDoneAndDecSaturation();

            if (currentRequest != null && currentRecipeStorage != null && blockToMine.getBlock().asItem().equals(currentRecipeStorage.getPrimaryOutput().getItem()))
            {
                currentRequest.addDelivery(new ItemStack(blockToMine.getBlock(), 1));
                job.setCraftCounter(job.getCraftCounter() + 1);
                if (job.getCraftCounter() >= job.getMaxCraftingCount())
                {
                    job.finishRequest(true);
                    worker.getCitizenExperienceHandler().addExperience(currentRequest.getRequest().getCount() / 2.0);
                    currentRequest = null;
                    currentRecipeStorage = null;
                    resetValues();

                    return START_WORKING;
                }
            }
        }

        return getState();
    }

    /**
     * Get the first slow in the inventory that contains concrete powder.
     * We attempt to find powder tied to the current request first, if we can't find any, we look for any possible powder.
     *
     * @return the slot number containing powder, or -1 if no slot contains any.
     */
    private int getSlotWithPowder()
    {
        if (currentRequest != null && currentRecipeStorage != null)
        {
            final ItemStack inputStack = currentRecipeStorage.getCleanedInput().get(0).getItemStack();
            if (CONCRETE.test(inputStack))
            {
                return InventoryUtils.findFirstSlotInItemHandlerWith(worker.getInventoryCitizen(), s -> ItemStackUtils.compareItemStacksIgnoreStackSize(s, inputStack));
            }
            return -1;
        }
        else
        {
            return InventoryUtils.findFirstSlotInItemHandlerWith(worker.getInventoryCitizen(), CONCRETE);
        }
    }

    @Override
    protected IAIState decide()
    {
        if (job.getCurrentTask() == null)
        {
            return performMixingWork();
        }

        if (walkTo == null && walkToBuilding())
        {
            return START_WORKING;
        }

        if (job.getActionsDone() > 0)
        {
            // Wait to dump before continuing.
            return getState();
        }

        if (currentRequest != null && currentRecipeStorage != null)
        {
            return QUERY_ITEMS;
        }

        return GET_RECIPE;
    }

    @Override
    protected int getExtendedCount(final ItemStack primaryOutput)
    {
        return building.outputBlockCountInWorld(primaryOutput);
    }

    @Override
    protected IAIState craft()
    {
        if (currentRecipeStorage == null)
        {
            return START_WORKING;
        }

        if (currentRequest == null && job.getCurrentTask() != null)
        {
            return GET_RECIPE;
        }

        if (walkTo == null && walkToBuilding())
        {
            return getState();
        }

        currentRequest = job.getCurrentTask();

        if (currentRequest != null && (currentRequest.getState() == RequestState.CANCELLED || currentRequest.getState() == RequestState.FAILED))
        {
            incrementActionsDone(getActionRewardForCraftingSuccess());
            currentRequest = null;
            currentRecipeStorage = null;
            return START_WORKING;
        }

        final ItemStack concrete = currentRecipeStorage.getPrimaryOutput();
        if (concrete.getItem() instanceof BlockItem && ((BlockItem) concrete.getItem()).getBlock() instanceof ConcretePowderBlock)
        {
            return super.craft();
        }

        return performMixingWork();
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return getState().equals(CONCRETE_MIXER_HARVESTING) ? building.getMaxConcretePlaced() : super.getActionsDoneUntilDumping();
    }

    /**
     * Harvest and placement logic for concrete.
     *
     * @return the next AI state.
     */
    private IAIState performMixingWork()
    {
        final BlockPos blockToMine = building.getBlockToMine();
        if (blockToMine != null)
        {
            return CONCRETE_MIXER_HARVESTING;
        }

        return CONCRETE_MIXER_PLACING;
    }
}
