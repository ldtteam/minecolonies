package com.minecolonies.core.entity.ai.minimal;

import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.minecolonies.api.entity.ai.IStateAI;
import com.minecolonies.api.entity.ai.statemachine.states.CitizenAIState;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingLibrary;
import com.minecolonies.core.colony.jobs.AbstractJobGuard;
import com.minecolonies.core.entity.ai.workers.education.EntityAIStudy;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.entity.other.SittingEntity;
import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

import static com.minecolonies.api.util.constant.Constants.DEFAULT_SPEED;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.SchematicTagConstants.TAG_SITTING;
import static com.minecolonies.core.entity.ai.minimal.EntityAICitizenWander.WanderState.*;

/**
 * Entity action to wander randomly around.
 */
public class EntityAICitizenWander implements IStateAI
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
        GO_TO_LEISURE_SITE,
        WANDER_AT_LEISURE_SITE,
        READ_A_BOOK
    }

    /**
     * The citizen that is wandering.
     */
    protected final EntityCitizen citizen;

    /**
     * Wandering speed.
     */
    protected final double speed;

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
     * @param citizen the citizen.
     * @param speed   the speed.
     */
    public EntityAICitizenWander(final EntityCitizen citizen, final double speed)
    {
        super();
        this.citizen = citizen;
        this.speed = speed;

        citizen.getCitizenAI().addTransition(new TickingTransition<>(CitizenAIState.IDLE, () -> true, this::decide, 100));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(GO_TO_LEISURE_SITE, () -> true, this::goToLeisureSite, 20));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(WANDER_AT_LEISURE_SITE, () -> true, this::wanderAtLeisureSite, 20));
        citizen.getCitizenAI().addTransition(new TickingTransition<>(READ_A_BOOK, () -> true, this::readABook, 20));
    }

    private IState readABook()
    {
        if (leisureSite == null)
        {
            walkTo = null;
            return CitizenAIState.IDLE;
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
                citizen.getCitizenData()
                  .getCitizenSkillHandler()
                  .tryLevelUpIntelligence(citizen.getCitizenData().getRandom(), EntityAIStudy.ONE_IN_X_CHANCE, citizen.getCitizenData());
                return CitizenAIState.IDLE;
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

    private IState goToLeisureSite()
    {
        if (leisureSite == null)
        {
            walkTo = null;
            return CitizenAIState.IDLE;
        }

        if (!citizen.isWorkerAtSiteWithMove(leisureSite, 3))
        {
            return GO_TO_LEISURE_SITE;
        }

        return WANDER_AT_LEISURE_SITE;
    }

    private IState wanderAtLeisureSite()
    {
        if (leisureSite == null || citizen.getRandom().nextInt(60 * 5) < 1)
        {
            leisureSite = null;
            walkTo = null;
            return CitizenAIState.IDLE;
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
                citizen.getNavigation()
                  .moveToRandomPos(10, DEFAULT_SPEED, ((IBlueprintDataProviderBE) blockEntity).getInWorldCorners());
            }
            if (walkTo == null && blockEntity instanceof TileEntityColonyBuilding && ((TileEntityColonyBuilding) blockEntity).getBuilding() instanceof BuildingLibrary
                  && citizen.getRandom().nextInt(100) < 5)
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
        return CitizenAIState.IDLE;
    }

    private IState decide()
    {
        if (!canUse())
        {
            return CitizenAIState.IDLE;
        }

        final int randomBit = citizen.getRandom().nextInt(100);
        if (randomBit < LEISURE_CHANCE)
        {
            leisureSite = citizen.getCitizenColonyHandler().getColonyOrRegister().getBuildingManager().getRandomLeisureSite();
            if (leisureSite == null)
            {
                if (citizen.getCitizenData().getHomeBuilding() != null)
                {
                    leisureSite = citizen.getCitizenData().getHomeBuilding().getPosition();
                }
                else
                {
                    leisureSite = citizen.getCitizenColonyHandler().getColonyOrRegister().getCenter();
                }
            }

            if (leisureSite != null)
            {
                citizen.getCitizenAI().setCurrentDelay(60 * 20);
                return GO_TO_LEISURE_SITE;
            }
        }

        citizen.getNavigation().moveToRandomPos(10, this.speed);
        return CitizenAIState.IDLE;
    }

    public boolean canUse()
    {
        return citizen.getNavigation().isDone() && !citizen.isBaby()
                 && !(citizen.getCitizenData().getJob() instanceof AbstractJobGuard);
    }
}
