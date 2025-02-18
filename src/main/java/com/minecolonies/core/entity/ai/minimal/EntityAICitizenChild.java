package com.minecolonies.core.entity.ai.minimal;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.ai.IStateAI;
import com.minecolonies.api.entity.ai.statemachine.AIEventTarget;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIBlockingEventType;
import com.minecolonies.api.entity.ai.statemachine.states.CitizenAIState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.citizen.AbstractCivilianEntity;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.core.colony.eventhooks.citizenEvents.CitizenGrownUpEvent;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.entity.pathfinding.navigation.EntityNavigationUtils;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.minecolonies.api.research.util.ResearchConstants.GROWTH;
import static com.minecolonies.api.util.constant.TranslationConstants.MESSAGE_INFO_COLONY_CHILD_GREW_UP;

/**
 * AI which controls child behaviour and growing.
 */
public class EntityAICitizenChild implements IStateAI
{

    private static final int GROW_UP_NOTIFY_LIMIT = 10;

    /**
     * States used for this AI
     */
    public enum State implements IAIState
    {
        BORED,
        PLAYING,
        VISITING,
        FOLLOWING;

        @Override
        public boolean isOkayToEat()
        {
            return true;
        }
    }

    protected final EntityCitizen child;
    private final   Random        rand = new Random();

    /**
     * Timer for actions/between actions
     */
    private int actionTimer = 0;

    /**
     * Distance at which the child starts following around.
     */
    private final static int START_FOLLOW_DISTANCE = 10;

    /**
     * Timer for how long the AI is active
     */
    private int aiActiveTime = 0;

    /**
     * Minimum ticks the AI is active before it is allowed to grow
     */
    private static final int MIN_ACTIVE_TIME = 4000;

    /**
     * Bonus ticks a child can get from a colony
     */
    private static final int BONUS_TIME_COLONY = 2000;

    /**
     * The entity we're following around
     */
    private WeakReference<Entity> followTarget = new WeakReference<>(null);

    /**
     * The position we started following on
     */
    private BlockPos followStart;

    /**
     * The blockpos the child is visiting
     */
    private IBuilding visitingHut;

    /**
     * The path for visiting the hut
     */
    private PathResult visitingPath;

    /**
     * Instantiates this task.
     *
     * @param citizen the citizen.
     */
    public EntityAICitizenChild(@NotNull final EntityCitizen citizen)
    {
        super();
        this.child = citizen;

        citizen.getCitizenAI().addTransition(new AITarget(CitizenAIState.IDLE, this::isReadyForActivity, () -> State.VISITING, 200));
        citizen.getCitizenAI().addTransition(new AIEventTarget(AIBlockingEventType.EVENT, this::tryGrowUp, () -> citizen.getCitizenAI().getState(), 500));
        citizen.getCitizenAI().addTransition(new AITarget(CitizenAIState.IDLE, this::searchEntityToFollow, () -> State.FOLLOWING, 200));

        citizen.getCitizenAI().addTransition(new AITarget(State.FOLLOWING, this::followingEntity, 20));
        citizen.getCitizenAI().addTransition(new AITarget(State.VISITING, this::visitHuts, 120));
    }

    /**
     * Whether the child moves to a new activity
     *
     * @return whether the child moves to a new activity
     */
    private boolean isReadyForActivity()
    {
        if (actionTimer > 0)
        {
            actionTimer -= 100;
        }

        if (canUse() && actionTimer <= 0 && rand.nextInt(10) == 0)
        {
            setDelayForNextAction();
            return true;
        }
        return false;
    }

    /**
     * Sets the delay till the next activity can start
     */
    private void setDelayForNextAction()
    {
        // Delay next activity by 3-5min
        actionTimer = rand.nextInt(2 * 60 * 20) + 3 * 60 * 20;
    }

    /**
     * Follow activity preconditions Search for someone to follow around
     *
     * @return whether a entity to follow was found
     */
    private boolean searchEntityToFollow()
    {
        if (!isReadyForActivity())
        {
            return false;
        }

        CompatibilityUtils.getWorldFromCitizen(child)
          // Search entities in radius
          .getEntities(
            child,
            child.getBoundingBox().expandTowards(
              (double) START_FOLLOW_DISTANCE,
              1.0D,
              (double) START_FOLLOW_DISTANCE),
            // Limit entity classes
            target -> target.isAlive() && (target instanceof AbstractCivilianEntity || target instanceof Player))
          // Take the first entity
          .stream()
          .findFirst()
          .ifPresent(entity -> followTarget = new WeakReference<>(entity));

        if (followTarget.get() != null)
        {
            // Follow time 30-60seconds, in ticks
            actionTimer = rand.nextInt(30 * 20) + 30 * 20;
            followStart = child.blockPosition();

            return true;
        }
        return false;
    }

    /**
     * Follow an entity around
     *
     * @return the next ai state to go into
     */
    private IState followingEntity()
    {
        actionTimer -= 20;
        if (actionTimer <= 0 || followTarget.get() == null)
        {
            // run back to start position
            EntityNavigationUtils.walkToPos(child, followStart, 2, true);

            setDelayForNextAction();
            return CitizenAIState.IDLE;
        }

        EntityNavigationUtils.walkToPos(child, followTarget.get().blockPosition(), 2, false);
        return State.FOLLOWING;
    }

    /**
     * Child visits random buildings
     *
     * @return the next ai state to go into
     */
    private IState visitHuts()
    {
        // Find a hut to visit
        if (visitingPath == null && child.getCitizenColonyHandler().getColonyOrRegister() != null)
        {
            // Visiting huts for 3min.
            if (actionTimer <= 0 && visitingHut == null)
            {
                actionTimer = 3 * 60 * 20;
            }

            int index = child.getCitizenColonyHandler().getColonyOrRegister().getBuildingManager().getBuildings().size();

            index = rand.nextInt(index);

            final List<IBuilding> buildings = new ArrayList<>(child.getCitizenColonyHandler().getColonyOrRegister().getBuildingManager().getBuildings().values());
            visitingHut = buildings.get(index);

            EntityNavigationUtils.walkToBuilding(child, visitingHut);
            visitingPath = child.getNavigation().getPathResult();
        }

        actionTimer -= 120;
        // Visiting
        if (actionTimer > 0)
        {
            // Path got interrupted by sth
            if (visitingPath != null && !visitingPath.isInProgress())
            {
                EntityNavigationUtils.walkToBuilding(child, visitingHut);
                visitingPath = child.getNavigation().getPathResult();
            }

            return State.VISITING;
        }

        child.getNavigation().stop();
        visitingPath = null;
        visitingHut = null;
        setDelayForNextAction();

        return CitizenAIState.IDLE;
    }

    /**
     * Tries to grow up the child.
     *
     * @return true if it grew
     */
    private boolean tryGrowUp()
    {
        if (!child.isBaby())
        {
            return false;
        }

        if (child.getCitizenColonyHandler().getColonyOrRegister() != null)
        {
            if (child.getCitizenColonyHandler().getColonyOrRegister().useAdditionalChildTime(BONUS_TIME_COLONY))
            {
                aiActiveTime += BONUS_TIME_COLONY;
            }
        }

        if (aiActiveTime >= MIN_ACTIVE_TIME)
        {
            final double growthModifier = (1 + child.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(GROWTH));

            // 1/144 Chance to grow up, every 25 seconds = avg 1h. Set to half since this AI isnt always active, e.g. sleeping.  At 2h they directly grow
            if (rand.nextInt((int) (70 / growthModifier) + 1) == 0 || aiActiveTime > 70000 / growthModifier)
            {
                child.getCitizenColonyHandler()
                  .getColonyOrRegister()
                  .getEventDescriptionManager()
                  .addEventDescription(new CitizenGrownUpEvent(child.blockPosition(), child.getCitizenData().getName()));
                if (child.getCitizenColonyHandler().getColonyOrRegister().getCitizenManager().getCitizens().size() <= GROW_UP_NOTIFY_LIMIT)
                {
                    MessageUtils.format(MESSAGE_INFO_COLONY_CHILD_GREW_UP, child.getName().getString()).sendTo(child.getCitizenColonyHandler().getColonyOrRegister()).forAllPlayers();
                }
                // Grow up
                child.setIsChild(false);
                child.setTextureDirty();
                child.getCitizenData().setIsChild(false);
                return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc} Returns whether the Goal should begin execution. True when age less than 100, when a random (120) is chosen correctly, and when a citizen is nearby.
     */
    public boolean canUse()
    {
        return child.isBaby() && child.getCitizenData() != null;
    }
}
