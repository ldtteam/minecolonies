package com.minecolonies.coremod.entity.ai.citizen.concrete;

import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingConcreteMixer;
import com.minecolonies.coremod.colony.jobs.JobConcreteMixer;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.START_WORKING;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Concrete mason AI class.
 */
public class EntityAIConcreteMixer extends AbstractEntityAICrafting<JobConcreteMixer, BuildingConcreteMixer>
{
    /**
     * Predicate to check if concrete powder is in inv.
     */
    private static final Predicate<ItemStack> CONCRETE =  stack -> !stack.isEmpty() && stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof ConcretePowderBlock;

    /**
     * Constructor for the Concrete mason.
     * Defines the tasks the Concrete mason executes.
     *
     * @param job a Concrete mason job to use.
     */
    public EntityAIConcreteMixer(@NotNull final JobConcreteMixer job)
    {
        super(job);
    }

    @Override
    public Class<BuildingConcreteMixer> getExpectedBuildingClass()
    {
        return BuildingConcreteMixer.class;
    }

    @Override
    protected IAIState decide()
    {
        if (job.getTaskQueue().isEmpty())
        {
            final IAIState state = mixConcrete();
            if (state != CRAFT)
            {
                return state;
            }
            return START_WORKING;
        }

        if (job.getCurrentTask() == null)
        {
            return START_WORKING;
        }

        if (walkToBuilding())
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

    /**
     * Mix the concrete and mine it.
     *
     * @return next state.
     */
    private IAIState mixConcrete()
    {
        final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(worker.getInventoryCitizen(), CONCRETE);

        if (slot != -1)
        {
            final ItemStack stack = worker.getInventoryCitizen().getStackInSlot(slot);
            final Block block = ((BlockItem) stack.getItem()).getBlock();
            final BlockPos posToPlace = getOwnBuilding().getBlockToPlace();
            if (posToPlace != null)
            {
                if (walkToBlock(posToPlace))
                {
                    return getState();
                }

                if (InventoryUtils.attemptReduceStackInItemHandler(worker.getInventoryCitizen(), stack, 1))
                {
                    world.setBlockState(posToPlace, block.getDefaultState().updatePostPlacement(Direction.DOWN, block.getDefaultState(), world, posToPlace, posToPlace), 0x03);
                }
                return getState();
            }
        }

        final BlockPos pos = getOwnBuilding().getBlockToMine();
        if (pos != null)
        {
            if (walkToBlock(pos))
            {
                return getState();
            }

            if (mineBlock(pos))
            {
                return CRAFT;
            }
            return getState();
        }

        if (InventoryUtils.hasItemInItemHandler(getOwnBuilding().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseGet(null), CONCRETE))
        {
            needsCurrently = new Tuple<>(CONCRETE, STACKSIZE);
            return GATHERING_REQUIRED_MATERIALS;
        }

        return getState();
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

        if (walkToBuilding())
        {
            return getState();
        }

        currentRequest = job.getCurrentTask();

        if (currentRequest != null && (currentRequest.getState() == RequestState.CANCELLED || currentRequest.getState() == RequestState.FAILED))
        {
            currentRequest = null;
            incrementActionsDone(getActionRewardForCraftingSuccess());
            currentRecipeStorage = null;
            return START_WORKING;
        }

        final ItemStack concrete = currentRecipeStorage.getPrimaryOutput();
        if (concrete.getItem() instanceof BlockItem && ((BlockItem) concrete.getItem()).getBlock() instanceof ConcretePowderBlock)
        {
            return super.craft();
        }

        final IAIState mixState = mixConcrete();
        if (mixState == getState())
        {
            currentRequest.addDelivery(concrete);

            if (job.getCraftCounter() >= job.getMaxCraftingCount())
            {
                currentRequest.addDelivery(new ItemStack(concrete.getItem(), 1));

                incrementActionsDone(getActionRewardForCraftingSuccess());
                currentRecipeStorage = null;
                resetValues();

                if (inventoryNeedsDump())
                {
                    if (job.getMaxCraftingCount() == 0 && job.getCraftCounter() == 0 && currentRequest != null)
                    {
                        job.finishRequest(true);
                        worker.getCitizenExperienceHandler().addExperience(currentRequest.getRequest().getCount() / 2.0);
                    }
                }
            }
            else
            {
                final IAIState check = checkForItems(currentRecipeStorage);
                if (check == CRAFT)
                {
                    job.setCraftCounter(job.getCraftCounter() + 1);
                    return GET_RECIPE;
                }
                else
                {
                    currentRequest = null;
                    job.finishRequest(false);
                    incrementActionsDoneAndDecSaturation();
                    resetValues();
                }
            }
        }

        return mixState;
    }
}
