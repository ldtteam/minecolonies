package com.minecolonies.core.entity.ai.workers;

import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.entity.ai.IStateAI;
import com.minecolonies.api.entity.ai.ITickingStateAI;
import com.minecolonies.api.entity.ai.statemachine.AIEventTarget;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIBlockingEventType;
import com.minecolonies.api.entity.ai.statemachine.states.CitizenAIState;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenColonyHandler;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.MathUtils;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.CitizenConstants;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.core.colony.interactionhandling.StandardInteraction;
import com.minecolonies.core.colony.jobs.AbstractJobGuard;
import com.minecolonies.core.colony.jobs.JobPupil;
import com.minecolonies.core.entity.ai.minimal.*;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.Monster;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.entity.citizen.AbstractEntityCitizen.ENTITY_AI_TICKRATE;
import static com.minecolonies.api.entity.citizen.VisibleCitizenStatus.*;
import static com.minecolonies.api.research.util.ResearchConstants.WORKING_IN_RAIN;
import static com.minecolonies.api.util.constant.CitizenConstants.*;
import static com.minecolonies.api.util.constant.Constants.DEFAULT_SPEED;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.core.entity.ai.minimal.EntityAIEatTask.RESTAURANT_LIMIT;
import static com.minecolonies.core.entity.citizen.citizenhandlers.CitizenDiseaseHandler.SEEK_DOCTOR_HEALTH;

/**
 * High level AI for citizens, which switches between all the different AI states like sleeping,working,fleeing etc
 */
public class CitizenAI implements IStateAI
{
    /**
     * Citizen this AI belongs to
     */
    private final EntityCitizen citizen;

    /**
     * The last citizen AI state
     */
    private IState lastState = CitizenAIState.IDLE;

    /**
     * List of small AI's added
     */
    private List<IStateAI> minimalAI = new ArrayList<>();

    public CitizenAI(final EntityCitizen citizen)
    {
        this.citizen = citizen;

        citizen.getCitizenAI().addTransition(new AIEventTarget<IState>(AIBlockingEventType.EVENT, () -> true, this::decideAiTask, 10));
        registerWorkAI();

        minimalAI.add(new EntityAICitizenAvoidEntity(citizen, Monster.class, (float) DISTANCE_OF_ENTITY_AVOID, LATER_RUN_SPEED_AVOID, INITIAL_RUN_SPEED_AVOID));
        minimalAI.add(new EntityAIEatTask(citizen));
        minimalAI.add(new EntityAICitizenWander(citizen, DEFAULT_SPEED));
        minimalAI.add(new EntityAISickTask(citizen));
        minimalAI.add(new EntityAISleep(citizen));
        minimalAI.add(new EntityAIMournCitizen(citizen, DEFAULT_SPEED));
    }

    /**
     * Registers callbacks for the work/job AI
     */
    private void registerWorkAI()
    {
        citizen.getCitizenAI().addTransition(new AITarget<>(CitizenAIState.WORK, () -> true, () ->
        {
            final ITickingStateAI ai = citizen.getCitizenJobHandler().getColonyJob().getWorkerAI();
            if (ai != null)
            {
                ai.resetAI();
            }
            citizen.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);
            return CitizenAIState.WORKING;
        }, 10));

        citizen.getCitizenAI().addTransition(new AITarget<>(CitizenAIState.WORKING, () -> true, () ->
        {
            if (citizen.getCitizenJobHandler().getColonyJob() != null)
            {
                final ITickingStateAI ai = citizen.getCitizenJobHandler().getColonyJob().getWorkerAI();
                if (ai != null)
                {
                    citizen.getCitizenJobHandler().getColonyJob().getWorkerAI().tick();
                }
            }

            return CitizenAIState.WORKING;
        }, ENTITY_AI_TICKRATE));
    }

    /**
     * Checks on the AI state the citizen should be in, and transitions as necessary
     *
     * @return
     */
    private IState decideAiTask()
    {
        IState next = calculateNextState();
        if (next == null || next == lastState && citizen.getCitizenAI().getState() != CitizenAIState.IDLE)
        {
            return null;
        }

        citizen.getCitizenData().setVisibleStatus(null);
        lastState = next;
        return lastState;
    }

    /**
     * Determines the AI state the citizen should be doing, sleeping,raiding etc at different priorities
     *
     * @return
     */
    private IState calculateNextState()
    {
        if (citizen.getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard guardJob)
        {
            if (shouldEat())
            {
                return CitizenAIState.EATING;
            }

            // Sick
            if (citizen.getCitizenData().getCitizenDiseaseHandler().isSick() && guardJob.canAIBeInterrupted())
            {
                citizen.getCitizenData().setVisibleStatus(VisibleCitizenStatus.SICK);
                return CitizenAIState.SICK;
            }

            return CitizenAIState.WORK;
        }

        // Sick at hospital
        if (citizen.getCitizenData().getCitizenDiseaseHandler().isSick() && citizen.getCitizenData().getCitizenDiseaseHandler().sleepsAtHospital())
        {
            citizen.getCitizenData().setVisibleStatus(VisibleCitizenStatus.SICK);
            return CitizenAIState.SICK;
        }

        // Raiding
        if (citizen.getCitizenColonyHandler().getColonyOrRegister().getRaiderManager().isRaided())
        {
            citizen.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(COM_MINECOLONIES_COREMOD_ENTITY_CITIZEN_RAID), ChatPriority.IMPORTANT));
            citizen.setVisibleStatusIfNone(RAIDED);
            return CitizenAIState.SLEEP;
        }

        // Sleeping
        if (!WorldUtil.isPastTime(CompatibilityUtils.getWorldFromCitizen(citizen), NIGHT - 2000))
        {
            if (lastState == CitizenAIState.SLEEP)
            {
                citizen.setVisibleStatusIfNone(SLEEP);
                citizen.getCitizenAI().setCurrentDelay(20 * 15);
                return CitizenAIState.SLEEP;
            }

            if (citizen.getCitizenSleepHandler().shouldGoSleep())
            {
                citizen.getCitizenData().onGoSleep();
                return CitizenAIState.SLEEP;
            }
        }
        else
        {
            if (citizen.getCitizenSleepHandler().isAsleep())
            {
                if (citizen.getCitizenData().getCitizenDiseaseHandler().isSick())
                {
                    final BlockPos bedPos = citizen.getCitizenSleepHandler().getBedLocation();
                    if (bedPos == null || bedPos.distSqr(citizen.blockPosition()) > 5)
                    {
                        citizen.getCitizenSleepHandler().onWakeUp();
                    }
                }
                else
                {
                    citizen.getCitizenSleepHandler().onWakeUp();
                }
            }
        }

        // Sick
        if (citizen.getCitizenData().getCitizenDiseaseHandler().isSick() || citizen.getCitizenData().getCitizenDiseaseHandler().isHurt())
        {
            citizen.getCitizenData().setVisibleStatus(VisibleCitizenStatus.SICK);
            return CitizenAIState.SICK;
        }

        // Eating
        if (shouldEat())
        {
            return CitizenAIState.EATING;
        }

        // Mourning
        if (citizen.getCitizenData().getCitizenMournHandler().isMourning())
        {
            if (lastState != CitizenAIState.MOURN)
            {
                citizen.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(COM_MINECOLONIES_COREMOD_ENTITY_CITIZEN_MOURNING,
                  citizen.getCitizenData().getCitizenMournHandler().getDeceasedCitizens().iterator().next()),
                  Component.translatable(COM_MINECOLONIES_COREMOD_ENTITY_CITIZEN_MOURNING),
                  ChatPriority.IMPORTANT));

                citizen.setVisibleStatusIfNone(MOURNING);
            }
            return CitizenAIState.MOURN;
        }

        // Raining
        if (CompatibilityUtils.getWorldFromCitizen(citizen).isRaining() && !shouldWorkWhileRaining() && !WorldUtil.isNetherType(citizen.level))
        {
            citizen.setVisibleStatusIfNone(BAD_WEATHER);
            if (!citizen.getCitizenData().getColony().getRaiderManager().isRaided()
                  && !citizen.getCitizenData().getCitizenMournHandler().isMourning())
            {
                citizen.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(COM_MINECOLONIES_COREMOD_ENTITY_CITIZEN_RAINING), ChatPriority.HIDDEN));
            }
            return CitizenAIState.IDLE;
        }

        // Work
        if (citizen.isBaby() && citizen.getCitizenJobHandler().getColonyJob() instanceof JobPupil && citizen.level.getDayTime() % 24000 > NOON)
        {
            citizen.setVisibleStatusIfNone(HOUSE);
            return CitizenAIState.IDLE;
        }

        if (citizen.getCitizenJobHandler().getColonyJob() != null)
        {
            return CitizenAIState.WORK;
        }

        citizen.setVisibleStatusIfNone(HOUSE);
        return CitizenAIState.IDLE;
    }

    /**
     * Checks if the citizen should be eating
     *
     * @return
     */
    public boolean shouldEat()
    {
        if (citizen.getCitizenData().justAte())
        {
            return false;
        }

        if (citizen.getCitizenData().getJob() != null && (!citizen.getCitizenData().getJob().canAIBeInterrupted()))
        {
            return false;
        }

        if (lastState == CitizenAIState.EATING)
        {
            return true;
        }

        if (citizen.getCitizenData().getJob() != null && (citizen.getCitizenData().getJob().getJobRegistryEntry() == ModJobs.cook.get()) && MathUtils.RANDOM.nextInt(20) > 0)
        {
            return false;
        }

        if (citizen.getCitizenData().getCitizenDiseaseHandler().isSick() && citizen.getCitizenSleepHandler().isAsleep())
        {
            return false;
        }

        return citizen.getCitizenData().getSaturation() <= CitizenConstants.AVERAGE_SATURATION &&
                 (citizen.getCitizenData().getSaturation() <= RESTAURANT_LIMIT ||
                    (citizen.getCitizenData().getSaturation() < LOW_SATURATION && citizen.getHealth() < SEEK_DOCTOR_HEALTH));
    }

    /**
     * Checks if the citizen should work even when it rains.
     *
     * @return true if his building level is bigger than 5.
     */
    private boolean shouldWorkWhileRaining()
    {
        if (MineColonies.getConfig().getServer().workersAlwaysWorkInRain.get())
        {
            return true;
        }

        final ICitizenColonyHandler colonyHandler = citizen.getCitizenColonyHandler();
        if (colonyHandler.getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(WORKING_IN_RAIN) > 0)
        {
            return true;
        }

        if (colonyHandler.getWorkBuilding() != null)
        {
            if (colonyHandler.getWorkBuilding().hasModule(WorkerBuildingModule.class))
            {
                return colonyHandler.getWorkBuilding().getFirstModuleOccurance(WorkerBuildingModule.class).canWorkDuringTheRain();
            }
        }
        return false;
    }
}
