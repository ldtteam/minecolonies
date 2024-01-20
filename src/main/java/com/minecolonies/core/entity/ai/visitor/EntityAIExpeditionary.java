package com.minecolonies.core.entity.ai.visitor;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.ai.statemachine.states.EntityState;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.visitor.AbstractEntityVisitor;
import com.minecolonies.api.util.WorldUtil;
import org.jetbrains.annotations.NotNull;

/**
 * AI for expeditionaries, they hang around in the town hall and not much else.
 */
public class EntityAIExpeditionary implements IState
{
    /**
     * The visitor entity we are attached to.
     */
    private final AbstractEntityVisitor visitor;

    /**
     * The townhall building reference.
     */
    private IBuilding townhall;

    /**
     * Constructor.
     *
     * @param entity current entity.
     */
    public EntityAIExpeditionary(@NotNull final AbstractEntityVisitor entity)
    {
        super();
        this.visitor = entity;

        ITickRateStateMachine<IState> stateMachine = entity.getEntityStateController();
        stateMachine.addTransition(new TickingTransition<>(EntityState.INIT, this::isEntityLoaded, () -> VisitorState.IDLE, 50));
        stateMachine.addTransition(new TickingTransition<>(VisitorState.IDLE, () -> true, this::decide, 50));
    }

    /**
     * Whether the entity is in a ticked chunk
     *
     * @return true if loaded
     */
    private boolean isEntityLoaded()
    {
        if (visitor.getCitizenColonyHandler().getColony() == null || visitor.getCitizenData() == null || visitor.getCitizenData().getHomeBuilding() == null)
        {
            return false;
        }

        townhall = visitor.getCitizenColonyHandler().getColony().getBuildingManager().getTownHall();

        return WorldUtil.isEntityBlockLoaded(visitor.level, visitor.blockPosition());
    }

    /**
     * Decides on the next activity
     *
     * @return next state
     */
    private VisitorState decide()
    {
        visitor.isWorkerAtSiteWithMove(townhall.getPosition(), 20);
        return VisitorState.IDLE;
    }

    /**
     * States of the expeditionary AI.
     */
    public enum VisitorState implements IState
    {
        IDLE,
        SLEEPING,
        SITTING,
        WANDERING
    }
}