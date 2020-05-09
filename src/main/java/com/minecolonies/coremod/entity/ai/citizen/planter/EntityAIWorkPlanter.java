package com.minecolonies.coremod.entity.ai.citizen.planter;

import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCrusher;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingPlantation;
import com.minecolonies.coremod.colony.jobs.JobCrusher;
import com.minecolonies.coremod.colony.jobs.JobPlanter;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import com.minecolonies.coremod.network.messages.client.LocalizedParticleEffectMessage;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.CitizenConstants.TICKS_20;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Planter AI class.
 */
public class EntityAIWorkPlanter<J extends JobPlanter> extends AbstractEntityAICrafting<J>
{
    /**
     * Delay for each of the craftings.
     */
    private static final int TICK_DELAY = 20;

    /**
     * Constructor for the planter.
     *
     * @param job a planter job to use.
     */
    public EntityAIWorkPlanter(@NotNull final J job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, 1),
          new AITarget(CRUSH, this::crush, TICK_DELAY)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class getExpectedBuildingClass()
    {
        return BuildingPlantation.class;
    }

    @Override
    protected IAIState decide()
    {
        final IAIState nextState = super.decide();
        if (nextState != START_WORKING)
        {
            return nextState;
        }
        return CRUSH;
    }

    /**
     * The crushing process.
     *
     * @return the next AiState to go to.
     */
    protected IAIState crush()
    {
        if (walkToBuilding())
        {
            return getState();
        }
        
        return getState();
    }

    /**
     * The actual crafting logic.
     *
     * @return the next state to go to.
     */
    @Override
    protected IAIState craft()
    {
        if (currentRecipeStorage == null)
        {
            setDelay(TICKS_20);
            return START_WORKING;
        }

        if (walkToBuilding())
        {
            setDelay(STANDARD_DELAY);
            return getState();
        }

        if (job.getMaxCraftingCount() == 0)
        {
            final PublicCrafting crafting = (PublicCrafting) job.getCurrentTask().getRequest();
            job.setMaxCraftingCount(CraftingUtils.calculateMaxCraftingCount(crafting.getCount(), currentRecipeStorage));
        }

        if (job.getMaxCraftingCount() == 0)
        {
            getOwnBuilding().getColony().getRequestManager().updateRequestState(job.getCurrentTask().getId(), RequestState.FAILED);
            job.setMaxCraftingCount(0);
            job.setCraftCounter(0);
            setDelay(TICKS_20);
            return START_WORKING;
        }

        currentRequest = job.getCurrentTask();
        final IAIState nextState = crush();
        if (nextState == getState())
        {
            if (job.getCraftCounter() >= job.getMaxCraftingCount())
            {
                final ItemStack primaryOutput = currentRecipeStorage.getPrimaryOutput();
                primaryOutput.setCount(currentRequest.getRequest().getCount());
                currentRequest.addDelivery(primaryOutput);
                incrementActionsDoneAndDecSaturation();
                job.setMaxCraftingCount(0);
                job.setCraftCounter(0);
                currentRecipeStorage = null;
                return START_WORKING;
            }
        }
        else
        {
            job.setMaxCraftingCount(0);
            job.setCraftCounter(0);
            return START_WORKING;
        }
        return getState();
    }
}
