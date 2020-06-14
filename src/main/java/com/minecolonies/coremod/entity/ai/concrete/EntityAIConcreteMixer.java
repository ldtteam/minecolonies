package com.minecolonies.coremod.entity.ai.concrete;

import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingConcreteMixer;
import com.minecolonies.coremod.colony.jobs.JobConcreteMixer;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.START_WORKING;

/**
 * Concrete mason AI class.
 */
public class EntityAIConcreteMixer extends AbstractEntityAICrafting<JobConcreteMixer, BuildingConcreteMixer>
{
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

        //todo walk to all positions we can place the concrete at.
        // Then place concrete each of them
        // then mine concrete at each of them, until we got all. The recipes is concrete powder ingredients -> concrete poweder -> concrete

        if (walkToBuilding())
        {
            return getState();
        }


        worker.setHeldItem(Hand.MAIN_HAND,
          currentRecipeStorage.getCleanedInput().get(worker.getRandom().nextInt(currentRecipeStorage.getCleanedInput().size())).getItemStack().copy());
        worker.setHeldItem(Hand.OFF_HAND, currentRecipeStorage.getPrimaryOutput().copy());
        worker.getCitizenItemHandler().hitBlockWithToolInHand(getOwnBuilding().getPosition());

        currentRequest = job.getCurrentTask();

        if (currentRequest != null && (currentRequest.getState() == RequestState.CANCELLED || currentRequest.getState() == RequestState.FAILED))
        {
            currentRequest = null;
            incrementActionsDone(getActionRewardForCraftingSuccess());
            currentRecipeStorage = null;
            return START_WORKING;
        }

        if (mineBlock(new BlockPos(0, 0, 0)))
        {
            final IAIState check = checkForItems(currentRecipeStorage);
            if (check == CRAFT)
            {
                if (!currentRecipeStorage.fullFillRecipe(worker.getItemHandlerCitizen()))
                {
                    currentRequest = null;
                    incrementActionsDone(getActionRewardForCraftingSuccess());
                    job.finishRequest(false);
                    resetValues();
                    return START_WORKING;
                }

                currentRequest.addDelivery(currentRecipeStorage.getPrimaryOutput());
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
                else
                {
                    return GET_RECIPE;
                }
            }
            else
            {
                currentRequest = null;
                job.finishRequest(false);
                incrementActionsDoneAndDecSaturation();
                resetValues();
            }
            return START_WORKING;
        }

        return getState();
    }
}
