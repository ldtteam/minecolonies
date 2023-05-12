package com.minecolonies.coremod.entity.ai.minimal;

import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.pathfinding.AbstractAdvancedPathNavigate;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingLibrary;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.SittingEntity;
import com.minecolonies.coremod.entity.ai.citizen.student.EntityAIStudy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

import static com.minecolonies.api.util.constant.Constants.DEFAULT_SPEED;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.SchematicTagConstants.TAG_SITTING;
import static com.minecolonies.coremod.entity.ai.minimal.EntityAICitizenWander.WanderState.*;

/**
 * Entity action to wander randomly around.
 */
public class EntityAICitizenWander extends Goal
{
    /**
     * Chance to enter the leisure state.
     */
    private static final int LEISURE_CHANCE = 5;

    /**
     * The different types of AIStates related to eating.
     */
    public enum WanderState implements IState
    {
        IDLE,
        GO_TO_LEISURE_SITE,
        WANDER_AT_LEISURE_SITE,
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
     * Leisure site to path to.
     */
    private BlockPos leisureSite;

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
        stateMachine.addTransition(new TickingTransition<>(IDLE, () -> true, this::decide, 100));
        stateMachine.addTransition(new TickingTransition<>(GO_TO_LEISURE_SITE, () -> true, this::goToLeisureSite, 20));
        stateMachine.addTransition(new TickingTransition<>(WANDER_AT_LEISURE_SITE, () -> true, this::wanderAtLeisureSite, 20));
        stateMachine.addTransition(new TickingTransition<>(READ_A_BOOK, () -> true, this::readABook, 20));
    }

    private WanderState readABook()
    {
        if (leisureSite == null)
        {
            walkTo = null;
            return IDLE;
        }

        if (walkTo != null)
        {
            if (!citizen.isWorkerAtSiteWithMove(walkTo, 3))
            {
                return READ_A_BOOK;
            }

            if (citizen.getRandom().nextInt(100) < 5)
            {
                citizen.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                walkTo = null;
                leisureSite = null;
                citizen.getCitizenData().getCitizenSkillHandler().tryLevelUpIntelligence(citizen.getCitizenData().getRandom(), EntityAIStudy.ONE_IN_X_CHANCE, citizen.getCitizenData());
                return IDLE;
            }

            citizen.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOOK));
            return READ_A_BOOK;
        }

        final BlockEntity blockEntity = citizen.level.getBlockEntity(leisureSite);
        if (blockEntity instanceof TileEntityColonyBuilding && ((TileEntityColonyBuilding) blockEntity).getBuilding() instanceof BuildingLibrary)
        {
            walkTo = ((BuildingLibrary) ((TileEntityColonyBuilding) blockEntity).getBuilding()).getRandomBookShelf();
        }

        return READ_A_BOOK;
    }

    private WanderState goToLeisureSite()
    {
        if (leisureSite == null)
        {
            walkTo = null;
            return IDLE;
        }

        if (!citizen.isWorkerAtSiteWithMove(leisureSite, 3))
        {
            return GO_TO_LEISURE_SITE;
        }

        return WANDER_AT_LEISURE_SITE;
    }

    private WanderState wanderAtLeisureSite()
    {
        if (leisureSite == null || citizen.getRandom().nextInt(60 * 5) < 1)
        {
            leisureSite = null;
            walkTo = null;
            return IDLE;
        }

        if (walkTo != null && !citizen.isWorkerAtSiteWithMove(walkTo, 3))
        {
            return WANDER_AT_LEISURE_SITE;
        }

        if (citizen.isPassenger())
        {
            return WANDER_AT_LEISURE_SITE;
        }

        final BlockEntity blockEntity = citizen.level.getBlockEntity(leisureSite);
        if (blockEntity instanceof IBlueprintDataProviderBE)
        {
            if (walkTo == null && citizen.getRandom().nextBoolean())
            {
                citizen.getNavigation().moveToRandomPos(10, DEFAULT_SPEED, ((IBlueprintDataProviderBE) blockEntity).getInWorldCorners(), AbstractAdvancedPathNavigate.RestrictionType.XYZ);
            }
            if (walkTo == null && blockEntity instanceof TileEntityColonyBuilding && ((TileEntityColonyBuilding) blockEntity).getBuilding() instanceof BuildingLibrary && citizen.getRandom().nextInt(100) < 5)
            {
                return READ_A_BOOK;
            }
            else
            {
                if (walkTo == null)
                {
                    final Map<String, Set<BlockPos>> map = ((IBlueprintDataProviderBE) blockEntity).getWorldTagNamePosMap();
                    final List<BlockPos> sittingPos = new ArrayList<>(map.getOrDefault(TAG_SITTING, Collections.emptySet()));
                    if (!sittingPos.isEmpty())
                    {
                        walkTo = sittingPos.get(citizen.getRandom().nextInt(sittingPos.size()));
                        return WANDER_AT_LEISURE_SITE;
                    }
                }
                else
                {
                    SittingEntity.sitDown(walkTo, citizen, TICKS_SECOND * 60);
                    walkTo = null;
                }
            }

            return WANDER_AT_LEISURE_SITE;
        }
        return IDLE;
    }

    private WanderState decide()
    {
        final int randomBit = citizen.getRandom().nextInt(100);
        if (randomBit < LEISURE_CHANCE)
        {
            leisureSite = citizen.getCitizenColonyHandler().getColony().getBuildingManager().getRandomLeisureSite();
            if (leisureSite != null)
            {
                return GO_TO_LEISURE_SITE;
            }
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
        return citizen.getDesiredActivity() != DesiredActivity.SLEEP && citizen.getNavigation().isDone() && !citizen.isBaby()
                 && !(citizen.getCitizenData().getJob() instanceof AbstractJobGuard);
    }

    @Override
    public void tick()
    {
        stateMachine.tick();
    }

    @Override
    public boolean canContinueToUse()
    {
        return !citizen.getNavigation().isDone() || stateMachine.getState() != IDLE;
    }

    @Override
    public void stop()
    {
        stateMachine.reset();
        citizen.getCitizenData().setVisibleStatus(null);
    }
}
