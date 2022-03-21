package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingLibrary;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

import static com.minecolonies.coremod.entity.ai.minimal.EntityAICitizenWander.WanderState.*;

/**
 * Entity action to wander randomly around.
 */
public class EntityAICitizenWander extends Goal
{
    /**
     * The different types of AIStates related to eating.
     */
    public enum WanderState implements IState
    {
        IDLE,
        GO_TO_LEISURE_SITE,
        WANDER_AT_LEISURE_SITE,
        SIT_AT_LEISURE_SITE,
        GO_TO_LIBRARY,
        READ_A_BOOK
    }

    /**
     * The citizen that is wandering.
     */
    protected final AbstractEntityCitizen citizen;

    /**
     * Wandering speed.
     */
    protected final double  speed;

    /**
     * AI statemachine
     */
    private final TickRateStateMachine<WanderState> stateMachine;

    /**
     * Position to path to.
     */
    private BlockPos walkTo;

    /**
     * Instantiates this task.
     *
     * @param citizen        the citizen.
     * @param speed          the speed.
     */
    public EntityAICitizenWander(final AbstractEntityCitizen citizen, final double speed)
    {
        super();
        this.citizen = citizen;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE));

        stateMachine = new TickRateStateMachine<>(IDLE, e -> Log.getLogger().warn("Wandering AI threw exception:", e));
        stateMachine.addTransition(new TickingTransition<>(IDLE, () -> true, this::decide, 20));
        stateMachine.addTransition(new TickingTransition<>(GO_TO_LIBRARY, () -> true, this::goToLibrary, 20));
        stateMachine.addTransition(new TickingTransition<>(GO_TO_LEISURE_SITE, () -> true, this::goToLeisureSite, 20));

        // don't forget tavern.
    }

    private WanderState goToLibrary()
    {
        if (!citizen.isWorkerAtSiteWithMove(walkTo, 3))
        {
            return GO_TO_LIBRARY;
        }


        return IDLE;
    }

    private WanderState goToLeisureSite()
    {
        if (!citizen.isWorkerAtSiteWithMove(walkTo, 3))
        {
            return GO_TO_LEISURE_SITE;
        }


        return IDLE;
    }

    private WanderState decide()
    {
        final int randomBit = citizen.getRandom().nextInt(100);
        if (randomBit < 3)
        {
            walkTo = citizen.getCitizenColonyHandler().getColony().getBuildingManager().getBestBuilding(citizen.blockPosition(), BuildingLibrary.class);
            if (walkTo != null)
            {
                return GO_TO_LIBRARY;
            }
        }
        else if (randomBit < 9)
        {
            walkTo = citizen.getCitizenColonyHandler().getColony().getBuildingManager().getRandomLeisureSite(citizen.getRandom());
            return GO_TO_LEISURE_SITE;
        }

        citizen.getNavigation().moveToRandomPos(10, this.speed);
        return IDLE;
    }

    @Override
    public boolean canUse()
    {
        if (citizen.getDesiredActivity() == DesiredActivity.SLEEP)
        {
            return false;
        }
        stateMachine.tick();
        return citizen.getRandom().nextInt(Constants.TICKS_SECOND * 3) == 0 && citizen.getDesiredActivity() != DesiredActivity.SLEEP && citizen.getNavigation().isDone();
    }

    @Override
    public void tick()
    {
        stateMachine.tick();
    }

    @Override
    public boolean canContinueToUse()
    {
        return !citizen.getNavigation().isDone();
    }

    /**
     * Resets the state of the AI.
     */
    private void reset()
    {

    }

    @Override
    public void stop()
    {
        reset();
        stateMachine.reset();
        citizen.getCitizenData().setVisibleStatus(null);
    }
}
