package com.minecolonies.core.entity.ai.workers.crafting;

import com.minecolonies.api.colony.requestsystem.request.RequestState;
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
import net.neoforged.neoforge.common.capabilities.Capabilities;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Concrete mason AI class.
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
    }

    @Override
    public Class<BuildingConcreteMixer> getExpectedBuildingClass()
    {
        return BuildingConcreteMixer.class;
    }

    @Override
    protected IAIState decide()
    {
        // This needs to only run on concrete powder that isn't earmarked for delivery. 
        // We need an 'output' inventory to protect those from processing here. 
        /*
        if (job.getTaskQueue().isEmpty())
        {
            final IAIState state = mixConcrete();
            if (state != CRAFT)
            {
                return state;
            }
            return START_WORKING;
        }
        */

        if (job.getCurrentTask() == null)
        {
            return START_WORKING;
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

    /**
     * Mix the concrete and mine it.
     *
     * @return next state.
     */
    private IAIState mixConcrete()
    {
        int slot = -1;

        if (currentRequest != null && currentRecipeStorage != null)
        {
            ItemStack inputStack = currentRecipeStorage.getCleanedInput().get(0).getItemStack();
            if (CONCRETE.test(inputStack))
            {
                slot = InventoryUtils.findFirstSlotInItemHandlerWith(worker.getInventoryCitizen(), s -> ItemStackUtils.compareItemStacksIgnoreStackSize(s, inputStack));
            }
            else
            {
                return START_WORKING;
            }
        }
        else
        {
            slot = InventoryUtils.findFirstSlotInItemHandlerWith(worker.getInventoryCitizen(), CONCRETE);
        }

        if (slot != -1)
        {
            final ItemStack stack = worker.getInventoryCitizen().getStackInSlot(slot);
            final Block block = ((BlockItem) stack.getItem()).getBlock();
            final BlockPos posToPlace = building.getBlockToPlace();
            if (posToPlace != null)
            {
                if (walkToBlock(posToPlace))
                {
                    walkTo = posToPlace;
                    return START_WORKING;
                }
                walkTo = null;
                if (InventoryUtils.attemptReduceStackInItemHandler(worker.getInventoryCitizen(), stack, 1))
                {
                    world.setBlock(posToPlace, block.defaultBlockState().updateShape(Direction.DOWN, block.defaultBlockState(), world, posToPlace, posToPlace), 0x03);
                }
                return START_WORKING;
            }
        }

        final BlockPos pos = building.getBlockToMine();
        if (pos != null)
        {
            if (walkToBlock(pos))
            {
                walkTo = pos;
                return START_WORKING;
            }
            walkTo = null;
            if (mineBlock(pos))
            {
                this.resetActionsDone();
                return CRAFT;
            }
            return START_WORKING;
        }

        if (InventoryUtils.hasItemInItemHandler(building.getCapability(Capabilities.ITEM_HANDLER).orElseGet(null), CONCRETE))
        {
            needsCurrently = new Tuple<>(CONCRETE, STACKSIZE);
            return GATHERING_REQUIRED_MATERIALS;
        }
        else
        {
            incrementActionsDone();
        }

        return START_WORKING;
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
            currentRequest.addDelivery(new ItemStack(concrete.getItem(), 1));
            job.setCraftCounter(job.getCraftCounter() + 1);
            if (job.getCraftCounter() >= job.getMaxCraftingCount())
            {
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
        }

        return mixState;
    }
}
