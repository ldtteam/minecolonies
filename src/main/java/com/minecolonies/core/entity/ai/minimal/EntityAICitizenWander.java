package com.minecolonies.core.entity.ai.minimal;

import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.minecolonies.api.entity.ai.IStateAI;
import com.minecolonies.api.entity.ai.statemachine.states.CitizenAIState;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.util.MathUtils;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingLibrary;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingUniversity;
import com.minecolonies.core.colony.jobs.AbstractJobGuard;
import com.minecolonies.core.entity.ai.workers.education.EntityAIStudy;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.entity.other.SittingEntity;
import com.minecolonies.core.entity.pathfinding.navigation.EntityNavigationUtils;
import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.minecolonies.core.tileentities.TileEntityDecorationController;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

import static com.minecolonies.api.util.constant.Constants.DEFAULT_SPEED;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.SchematicTagConstants.*;
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
     * The different types of AIStates related to wandering.
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
            if (!EntityNavigationUtils.walkToPos(citizen, walkTo, 3, true))
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
        if (blockEntity instanceof TileEntityColonyBuilding tileEntityColonyBuilding)
        {
            if (tileEntityColonyBuilding.getBuilding() instanceof BuildingLibrary buildingLibrary)
            {
                walkTo = buildingLibrary.getRandomBookShelf();
            }
            else if(tileEntityColonyBuilding.getBuilding() instanceof BuildingUniversity buildingUniversity)
            {
                walkTo = buildingUniversity.getRandomBookShelf();
            }
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

        if (!EntityNavigationUtils.walkToPos(citizen, leisureSite, 3, true))
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

        if (walkTo != null && !EntityNavigationUtils.walkToPos(citizen, walkTo, 3, true))
        {
            return WANDER_AT_LEISURE_SITE;
        }

        final BlockEntity blockEntity = citizen.level.getBlockEntity(leisureSite);
        if (blockEntity instanceof IBlueprintDataProviderBE)
        {
            if (walkTo == null && citizen.getRandom().nextInt(10) <= 0)
            {
                EntityNavigationUtils.walkToRandomPosWithin(citizen, 10, DEFAULT_SPEED, ((IBlueprintDataProviderBE) blockEntity).getInWorldCorners(), citizen.level.isRaining());
                citizen.getCitizenAI().setCurrentDelay(30);
            }
            else if (walkTo == null && blockEntity instanceof TileEntityColonyBuilding
                && (((TileEntityColonyBuilding) blockEntity).getBuilding() instanceof BuildingLibrary
                || ((TileEntityColonyBuilding) blockEntity).getBuilding() instanceof BuildingUniversity)
                  && citizen.getRandom().nextInt(100) < 5)
            {
                return READ_A_BOOK;
            }
            else
            {
                final Map<String, Set<BlockPos>> map = ((IBlueprintDataProviderBE) blockEntity).getWorldTagNamePosMap();
                final List<BlockPos> sittingPos = new ArrayList<>(map.getOrDefault(TAG_SITTING, Collections.emptySet()));
                final List<BlockPos> insideSittingPos = new ArrayList<>(map.getOrDefault(TAG_SIT_IN, Collections.emptySet()));
                final List<BlockPos> outsideSittingPos = new ArrayList<>(map.getOrDefault(TAG_SIT_OUT, Collections.emptySet()));

                final List<BlockPos> insideStandingPos = new ArrayList<>(map.getOrDefault(TAG_STAND_IN, Collections.emptySet()));
                final List<BlockPos> outsideStandingPos = new ArrayList<>(map.getOrDefault(TAG_STAND_OUT, Collections.emptySet()));

                if (walkTo == null)
                {
                    if (citizen.level.isRaining() || MathUtils.RANDOM.nextBoolean())
                    {
                        if (!insideSittingPos.isEmpty() && MathUtils.RANDOM.nextBoolean())
                        {
                            walkTo = insideSittingPos.get(citizen.getRandom().nextInt(insideSittingPos.size()));
                            if (SittingEntity.isSittingPosOccupied(walkTo, citizen.level))
                            {
                                walkTo = null;
                            }
                            else
                            {
                                return WANDER_AT_LEISURE_SITE;
                            }
                        }
                        else if (!insideStandingPos.isEmpty())
                        {
                            walkTo = insideStandingPos.get(citizen.getRandom().nextInt(insideStandingPos.size()));
                            return WANDER_AT_LEISURE_SITE;
                        }
                    }

                    if (!outsideSittingPos.isEmpty() && MathUtils.RANDOM.nextBoolean())
                    {
                        walkTo = outsideSittingPos.get(citizen.getRandom().nextInt(outsideSittingPos.size()));
                        if (SittingEntity.isSittingPosOccupied(walkTo, citizen.level))
                        {
                            walkTo = null;
                        }
                        else
                        {
                            return WANDER_AT_LEISURE_SITE;
                        }
                    }
                    else if (!outsideStandingPos.isEmpty())
                    {
                        walkTo = outsideStandingPos.get(citizen.getRandom().nextInt(outsideStandingPos.size()));
                        return WANDER_AT_LEISURE_SITE;
                    }

                    if (!sittingPos.isEmpty())
                    {
                        walkTo = sittingPos.get(citizen.getRandom().nextInt(sittingPos.size()));
                        if (SittingEntity.isSittingPosOccupied(walkTo, citizen.level))
                        {
                            walkTo = null;
                        }
                        else
                        {
                            return WANDER_AT_LEISURE_SITE;
                        }
                    }
                }
                else
                {
                    if (sittingPos.contains(walkTo) || insideSittingPos.contains(walkTo) || outsideSittingPos.contains(walkTo))
                    {
                        SittingEntity.sitDown(walkTo, citizen, TICKS_SECOND * 30);
                    }
                    citizen.getCitizenAI().setCurrentDelay(TICKS_SECOND * 30);
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

        EntityNavigationUtils.walkToRandomPos(citizen, 10, this.speed);
        return CitizenAIState.IDLE;
    }

    public boolean canUse()
    {
        return citizen.getNavigation().isDone() && !citizen.isBaby()
                 && !(citizen.getCitizenData().getJob() instanceof AbstractJobGuard);
    }
}
