package com.minecolonies.coremod.entity.ai.citizen;

import com.minecolonies.api.colony.interactionhandling.ChatPriority;
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
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.CitizenConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteraction;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.colony.jobs.JobPupil;
import com.minecolonies.coremod.entity.ai.minimal.*;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.Monster;

import static com.minecolonies.api.entity.citizen.VisibleCitizenStatus.*;
import static com.minecolonies.api.research.util.ResearchConstants.WORKING_IN_RAIN;
import static com.minecolonies.api.util.constant.CitizenConstants.*;
import static com.minecolonies.api.util.constant.Constants.DEFAULT_SPEED;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.coremod.entity.ai.minimal.EntityAIEatTask.RESTAURANT_LIMIT;
import static com.minecolonies.coremod.entity.citizen.citizenhandlers.CitizenDiseaseHandler.SEEK_DOCTOR_HEALTH;

public class CitizenAI implements IStateAI
{
    private final EntityCitizen citizen;
    private       IState        lastState = CitizenAIState.IDLE;

    public CitizenAI(final EntityCitizen citizen)
    {
        this.citizen = citizen;

        citizen.getCitizenAI().addTransition(new AIEventTarget<IState>(AIBlockingEventType.EVENT, () -> true, this::decideAiTask, 10));
        registerWorkAI();

        new EntityAICitizenAvoidEntity(citizen, Monster.class, (float) DISTANCE_OF_ENTITY_AVOID, LATER_RUN_SPEED_AVOID, INITIAL_RUN_SPEED_AVOID);
        new EntityAIEatTask(citizen);
        new EntityAICitizenWander(citizen, DEFAULT_SPEED);
        new EntityAISickTask(citizen);
        new EntityAISleep(citizen);
        new EntityAIMournCitizen(citizen, DEFAULT_SPEED);
    }

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
        }, 1));
    }

    private IState decideAiTask()
    {
        IState next = calculateNextState();
        if (next == null || next == lastState && citizen.getCitizenAI().getState() != CitizenAIState.IDLE)
        {
            return null;
        }

        citizen.getCitizenData().setVisibleStatus(null);
        Log.getLogger().warn(lastState+" -> "+next);
        lastState = next;
        return lastState;
    }

    private IState calculateNextState()
    {
        if (citizen.getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard)
        {
            if (shouldEat())
            {
                return CitizenAIState.EATING;
            }
            return CitizenAIState.WORK;
        }

        // Sick at hospital
        if (citizen.getCitizenDiseaseHandler().isSick() && citizen.getCitizenDiseaseHandler().sleepsAtHospital())
        {
            citizen.getCitizenData().setVisibleStatus(VisibleCitizenStatus.SICK);
            return CitizenAIState.SICK;
        }

        // Raiding
        if (citizen.getCitizenColonyHandler().getColony().getRaiderManager().isRaided() && !(citizen.getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard))
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
            if (citizen.getCitizenSleepHandler().isAsleep() && !citizen.getCitizenDiseaseHandler().isSick())
            {
                citizen.getCitizenSleepHandler().onWakeUp();
            }
        }

        // Sick
        if (citizen.getCitizenDiseaseHandler().isSick() || citizen.getCitizenDiseaseHandler().isHurt())
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
        if (citizen.getCitizenData().getCitizenMournHandler().isMourning() && citizen.getCitizenData().getCitizenMournHandler().shouldMourn())
        {
            if (!citizen.getCitizenColonyHandler().getColony().getRaiderManager().isRaided())
            {
                citizen.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(COM_MINECOLONIES_COREMOD_ENTITY_CITIZEN_MOURNING,
                  citizen.getCitizenData().getCitizenMournHandler().getDeceasedCitizens().iterator().next()),
                  Component.translatable(COM_MINECOLONIES_COREMOD_ENTITY_CITIZEN_MOURNING),
                  ChatPriority.IMPORTANT));
            }
            citizen.setVisibleStatusIfNone(MOURNING);
            return CitizenAIState.MOURN;
        }
        else
        {
            this.citizen.getCitizenData().getCitizenMournHandler().clearDeceasedCitizen();
            this.citizen.getCitizenData().getCitizenMournHandler().setMourning(false);
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
            return CitizenAIState.SLEEP;
        }

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

    public boolean shouldEat()
    {
        if (citizen.getCitizenData().justAte())
        {
            return false;
        }

        if (citizen.getCitizenDiseaseHandler().isSick() && citizen.getCitizenSleepHandler().isAsleep())
        {
            return false;
        }

        if (citizen.getCitizenJobHandler().getColonyJob() != null && !citizen.getCitizenJobHandler().getColonyJob().canAIBeInterrupted())
        {
            return false;
        }

        if (citizen.getCitizenData().getSaturation() <= CitizenConstants.AVERAGE_SATURATION)
        {
            if (citizen.getCitizenData().getSaturation() <= RESTAURANT_LIMIT ||
                  (citizen.getCitizenData().getSaturation() < LOW_SATURATION && citizen.getHealth() < SEEK_DOCTOR_HEALTH))
            {
                return true;
            }
        }
        return false;
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
        if (colonyHandler.getColony().getResearchManager().getResearchEffects().getEffectStrength(WORKING_IN_RAIN) > 0)
        {
            return true;
        }

        if (colonyHandler.getWorkBuilding() != null)
        {
            if (colonyHandler.getWorkBuilding().hasModule(WorkerBuildingModule.class))
            {
                if (colonyHandler.getWorkBuilding().getFirstModuleOccurance(WorkerBuildingModule.class).canWorkDuringTheRain())
                {
                    return true;
                }
            }
        }
        return false;
    }
}
