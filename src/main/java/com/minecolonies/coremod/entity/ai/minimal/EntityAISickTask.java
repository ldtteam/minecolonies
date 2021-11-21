package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.entity.ai.statemachine.AIEventTarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIBlockingEventType;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.util.*;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingHospital;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteraction;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.network.messages.client.CircleParticleEffectMessage;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

import java.util.EnumSet;
import java.util.List;

import static com.minecolonies.api.util.constant.CitizenConstants.LOW_SATURATION;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.GuardConstants.BASIC_VOLUME;
import static com.minecolonies.api.util.constant.TranslationConstants.NO_HOSPITAL;
import static com.minecolonies.api.util.constant.TranslationConstants.WAITING_FOR_CURE;
import static com.minecolonies.coremod.entity.ai.minimal.EntityAISickTask.DiseaseState.*;
import static com.minecolonies.coremod.entity.citizen.citizenhandlers.CitizenDiseaseHandler.SEEK_DOCTOR_HEALTH;

/**
 * The AI task for citizens to execute when they are supposed to eat.
 */
public class EntityAISickTask extends Goal
{
    /**
     * Min distance to hut before pathing to hospital.
     */
    private static final int MIN_DIST_TO_HUT = 5;

    /**
     * Min distance to hospital before trying to find a bed.
     */
    private static final int MIN_DIST_TO_HOSPITAL = 3;

    /**
     * Min distance to the hospital in general.
     */
    private static final long MINIMUM_DISTANCE_TO_HOSPITAL = 10;

    /**
     * Required time to cure.
     */
    private static final int REQUIRED_TIME_TO_CURE = 60;

    /**
     * Chance for a random cure to happen.
     */
    private static final int CHANCE_FOR_RANDOM_CURE = 10;

    /**
     * Attempts to position right in the bed.
     */
    private static final int GOING_TO_BED_ATTEMPTS = 20;

    /**
     * Citizen data.
     */
    private final ICitizenData citizenData;

    /**
     * The waiting ticks.
     */
    private int waitingTicks = 0;

    /**
     * The bed the citizen is sleeping in.
     */
    private BlockPos usedBed;

    /**
     * The different types of AIStates related to eating.
     */
    public enum DiseaseState implements IState
    {
        IDLE,
        CHECK_FOR_CURE,
        GO_TO_HUT,
        SEARCH_HOSPITAL,
        GO_TO_HOSPITAL,
        WAIT_FOR_CURE,
        FIND_EMPTY_BED,
        APPLY_CURE,
        WANDER
    }

    /**
     * The citizen assigned to this task.
     */
    private final EntityCitizen citizen;

    /**
     * Restaurant to which the citizen should path.
     */
    private BlockPos placeToPath;

    /**
     * AI statemachine
     */
    private final ITickRateStateMachine<DiseaseState> stateMachine;

    /**
     * Instantiates this task.
     *
     * @param citizen the citizen.
     */
    public EntityAISickTask(final EntityCitizen citizen)
    {
        super();
        this.citizen = citizen;
        this.citizenData = citizen.getCitizenData();
        this.setFlags(EnumSet.of(Flag.MOVE));

        stateMachine = new TickRateStateMachine<>(IDLE, e -> Log.getLogger().warn("Disease AI threw exception:", e));
        stateMachine.addTransition(new TickingTransition<>(IDLE, this::isSick, () -> CHECK_FOR_CURE, 20));
        stateMachine.addTransition(new AIEventTarget<DiseaseState>(AIBlockingEventType.AI_BLOCKING, () -> true, this::setNotWorking, 20));
        stateMachine.addTransition(new TickingTransition<>(CHECK_FOR_CURE, () -> true, this::checkForCure, 20));
        stateMachine.addTransition(new TickingTransition<>(WANDER, () -> true, this::wander, 200));

        stateMachine.addTransition(new TickingTransition<>(CHECK_FOR_CURE, () -> true, this::checkForCure, 20));
        stateMachine.addTransition(new TickingTransition<>(GO_TO_HUT, () -> true, this::goToHut, 20));
        stateMachine.addTransition(new TickingTransition<>(SEARCH_HOSPITAL, () -> true, this::searchHospital, 20));
        stateMachine.addTransition(new TickingTransition<>(GO_TO_HOSPITAL, () -> true, this::goToHospital, 20));
        stateMachine.addTransition(new TickingTransition<>(WAIT_FOR_CURE, () -> true, this::waitForCure, 20));
        stateMachine.addTransition(new TickingTransition<>(APPLY_CURE, () -> true, this::applyCure, 20));
        stateMachine.addTransition(new TickingTransition<>(FIND_EMPTY_BED, () -> true, this::findEmptyBed, 20));
    }

    private DiseaseState setNotWorking()
    {
        final IJob<?> job = citizen.getCitizenJobHandler().getColonyJob();
        if (job != null && stateMachine.getState() != IDLE)
        {
            citizenData.setWorking(false);
            citizen.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, TICKS_SECOND * 30));
        }

        return null;
    }

    private boolean isSick()
    {
        if (citizen.getDesiredActivity() == DesiredActivity.SLEEP && !citizen.isSleeping())
        {
            return false;
        }

        if (citizen.getCitizenJobHandler().getColonyJob() != null && !citizen.getCitizenJobHandler().getColonyJob().canAIBeInterrupted())
        {
            return false;
        }

        return citizen.getCitizenDiseaseHandler().isSick()
                 || (!(citizen.getCitizenJobHandler() instanceof AbstractJobGuard) && citizen.getHealth() < SEEK_DOCTOR_HEALTH && citizenData.getSaturation() > LOW_SATURATION);
    }

    /**
     * Do a bit of wandering.
     * @return start over.
     */
    public DiseaseState wander()
    {
        citizen.getNavigation().moveToRandomPos(10, 0.6D);
        return CHECK_FOR_CURE;
    }


    @Override
    public boolean canUse()
    {
        if (citizen.getDesiredActivity() == DesiredActivity.SLEEP)
        {
            return false;
        }
        stateMachine.tick();
        return stateMachine.getState() != IDLE;
    }

    /**
     * Find an empty bed to ly in.
     *
     * @return the next state to go to.
     */
    private DiseaseState findEmptyBed()
    {
        // Finding bed
        if (usedBed == null && citizen.getCitizenData() != null)
        {
            this.usedBed = citizen.getCitizenData().getBedPos();
            if (citizen.getCitizenData().getBedPos().equals(BlockPos.ZERO))
            {
                this.usedBed = null;
            }
        }

        final BlockPos hospitalPos = citizen.getCitizenColonyHandler().getColony().getBuildingManager().getBestBuilding(citizen, BuildingHospital.class);
        final IColony colony = citizen.getCitizenColonyHandler().getColony();
        final IBuilding hospital = colony.getBuildingManager().getBuilding(hospitalPos);

        if (hospital instanceof BuildingHospital)
        {
            if (usedBed != null && !((BuildingHospital) hospital).getBedList().contains(usedBed))
            {
                usedBed = null;
            }

            if (usedBed == null)
            {
                for (final BlockPos pos : ((BuildingHospital) hospital).getBedList())
                {
                    final Level world = citizen.level;
                    BlockState state = world.getBlockState(pos);
                    if (state.is(BlockTags.BEDS)
                          && !state.getValue(BedBlock.OCCUPIED)
                          && state.getValue(BedBlock.PART).equals(BedPart.HEAD)
                          && world.isEmptyBlock(pos.above()))
                    {
                        usedBed = pos;
                        ((BuildingHospital) hospital).registerPatient(usedBed, citizen.getCivilianID());
                        return FIND_EMPTY_BED;
                    }
                }

                if (usedBed == null)
                {
                    return WAIT_FOR_CURE;
                }
            }

            if (citizen.isWorkerAtSiteWithMove(usedBed, 3))
            {
                waitingTicks++;
                if (!citizen.getCitizenSleepHandler().trySleep(usedBed))
                {
                    ((BuildingHospital) hospital).registerPatient(usedBed, 0);
                    citizen.getCitizenData().setBedPos(BlockPos.ZERO);
                    usedBed = null;
                }
            }
        }

        if (waitingTicks > GOING_TO_BED_ATTEMPTS)
        {
            waitingTicks = 0;
            return WAIT_FOR_CURE;
        }
        return FIND_EMPTY_BED;
    }

    /**
     * Actual action of eating.
     *
     * @return the next state to go to, if successful idle.
     */
    private DiseaseState applyCure()
    {
        if (checkForCure() != APPLY_CURE)
        {
            return CHECK_FOR_CURE;
        }

        final List<ItemStack> list = IColonyManager.getInstance().getCompatibilityManager().getDisease(citizen.getCitizenDiseaseHandler().getDisease()).getCure();
        citizen.setItemInHand(InteractionHand.MAIN_HAND, list.get(citizen.getRandom().nextInt(list.size())));


        citizen.swing(InteractionHand.MAIN_HAND);
        citizen.playSound(SoundEvents.NOTE_BLOCK_HARP, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPentatonic(citizen.getRandom()));
        Network.getNetwork().sendToTrackingEntity(
          new CircleParticleEffectMessage(
            citizen.position().add(0, 2, 0),
            ParticleTypes.HAPPY_VILLAGER,
            waitingTicks), citizen);


        waitingTicks++;
        if (waitingTicks < REQUIRED_TIME_TO_CURE)
        {
            return APPLY_CURE;
        }

        cure();
        return IDLE;
    }

    /**
     * Cure the citizen.
     *
     */
    private void cure()
    {
        final Disease disease = IColonyManager.getInstance().getCompatibilityManager().getDisease(citizen.getCitizenDiseaseHandler().getDisease());
        if (disease != null)
        {
            for (final ItemStack cure : disease.getCure())
            {
                final int slot = InventoryUtils.findFirstSlotInProviderNotEmptyWith(citizen, stack -> stack.sameItem(cure));
                if (slot != -1)
                {
                    citizenData.getInventory().extractItem(slot, 1, false);
                }
            }
        }

        if (usedBed != null)
        {
            final BlockPos hospitalPos = citizen.getCitizenColonyHandler().getColony().getBuildingManager().getBestBuilding(citizen, BuildingHospital.class);
            final IColony colony = citizen.getCitizenColonyHandler().getColony();
            final IBuilding hospital = colony.getBuildingManager().getBuilding(hospitalPos);
            ((BuildingHospital) hospital).registerPatient(usedBed, 0);
            usedBed = null;
            citizen.getCitizenData().setBedPos(BlockPos.ZERO);
        }
        citizen.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        citizenData.markDirty();
        citizen.getCitizenDiseaseHandler().cure();
        citizen.setHealth(citizen.getMaxHealth());
        reset();
    }

    /**
     * Stay in bed while waiting to be cured.
     *
     * @return the next state to go to.
     */
    private DiseaseState waitForCure()
    {
        final IColony colony = citizenData.getColony();
        placeToPath = colony.getBuildingManager().getBestBuilding(citizen, BuildingHospital.class);

        if (placeToPath == null)
        {
            return SEARCH_HOSPITAL;
        }

        final DiseaseState state = checkForCure();
        if (state == APPLY_CURE)
        {
            return APPLY_CURE;
        }
        else if (state == IDLE)
        {
            reset();
            return IDLE;
        }

        if (citizen.getRandom().nextInt(10000) < CHANCE_FOR_RANDOM_CURE)
        {
            cure();
            return IDLE;
        }

        if (citizen.getCitizenSleepHandler().isAsleep())
        {
            final BlockPos hospital = colony.getBuildingManager().getBestBuilding(citizen, BuildingHospital.class);
            if (hospital != null)
            {
                final IBuilding building = colony.getBuildingManager().getBuilding(hospital);
                if (building instanceof BuildingHospital && !((BuildingHospital) building).getBedList().contains(citizen.getCitizenSleepHandler().getBedLocation()))
                {
                    citizen.getCitizenSleepHandler().onWakeUp();
                }
            }
        }

        if (!citizen.getCitizenSleepHandler().isAsleep() && BlockPosUtil.getDistance2D(placeToPath, citizen.blockPosition()) > MINIMUM_DISTANCE_TO_HOSPITAL)
        {
            return GO_TO_HOSPITAL;
        }

        if (!citizen.getCitizenSleepHandler().isAsleep())
        {
            return FIND_EMPTY_BED;
        }

        return WAIT_FOR_CURE;
    }

    /**
     * Go to the hut to try to get food there first.
     *
     * @return the next state to go to.
     */
    private DiseaseState goToHut()
    {
        final IBuilding buildingWorker = citizenData.getWorkBuilding();
        if (buildingWorker == null)
        {
            return SEARCH_HOSPITAL;
        }

        if (citizen.getCitizenSleepHandler().isAsleep() || citizen.isWorkerAtSiteWithMove(buildingWorker.getPosition(), MIN_DIST_TO_HUT))
        {
            return SEARCH_HOSPITAL;
        }
        return GO_TO_HUT;
    }

    /**
     * Go to the previously found placeToPath to get cure.
     *
     * @return the next state to go to.
     */
    private DiseaseState goToHospital()
    {
        if (placeToPath == null)
        {
            return SEARCH_HOSPITAL;
        }

        if (citizen.getCitizenSleepHandler().isAsleep() || (citizen.getNavigation().isDone() && citizen.isWorkerAtSiteWithMove(placeToPath, MIN_DIST_TO_HOSPITAL)))
        {
            return WAIT_FOR_CURE;
        }
        return SEARCH_HOSPITAL;
    }

    /**
     * Search for a placeToPath within the colony of the citizen.
     *
     * @return the next state to go to.
     */
    private DiseaseState searchHospital()
    {
        final IColony colony = citizenData.getColony();
        placeToPath = colony.getBuildingManager().getBestBuilding(citizen, BuildingHospital.class);

        if (placeToPath == null)
        {
            final String id = citizen.getCitizenDiseaseHandler().getDisease();
            if (id.isEmpty())
            {
                return IDLE;
            }
            final Disease disease = IColonyManager.getInstance().getCompatibilityManager().getDisease(id);
            citizenData.triggerInteraction(new StandardInteraction(new TranslatableComponent(NO_HOSPITAL, disease.getName(), disease.getCureString()),
              new TranslatableComponent(NO_HOSPITAL),
              ChatPriority.BLOCKING));
            return WANDER;
        }
        else if (!citizen.getCitizenDiseaseHandler().getDisease().isEmpty())
        {
            final Disease disease = IColonyManager.getInstance().getCompatibilityManager().getDisease(citizen.getCitizenDiseaseHandler().getDisease());
            citizenData.triggerInteraction(new StandardInteraction(new TranslatableComponent(WAITING_FOR_CURE, disease.getName(), disease.getCureString()),
              new TranslatableComponent(WAITING_FOR_CURE),
              ChatPriority.BLOCKING));
        }

        return GO_TO_HOSPITAL;
    }

    /**
     * Checks if the citizen has the cure in the inventory and makes a decision based on that.
     *
     * @return the next state to go to.
     */
    private DiseaseState checkForCure()
    {
        final String id = citizen.getCitizenDiseaseHandler().getDisease();
        if (id.isEmpty())
        {
            if (citizen.getHealth() > SEEK_DOCTOR_HEALTH)
            {
                reset();
                return IDLE;
            }
            return GO_TO_HUT;
        }
        final Disease disease = IColonyManager.getInstance().getCompatibilityManager().getDisease(id);
        for (final ItemStack cure : disease.getCure())
        {
            final int slot = InventoryUtils.findFirstSlotInProviderNotEmptyWith(citizen, stack -> stack.sameItem(cure));
            if (slot == -1)
            {
                if (citizen.getCitizenDiseaseHandler().isSick())
                {
                    return GO_TO_HUT;
                }

                reset();
                return IDLE;
            }
        }
        return APPLY_CURE;
    }

    /**
     * Resets the state of the AI.
     */
    private void reset()
    {
        waitingTicks = 0;
        citizen.releaseUsingItem();
        citizen.stopUsingItem();
        citizen.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        placeToPath = null;
    }

    @Override
    public void stop()
    {
        reset();
        stateMachine.reset();
        citizen.getCitizenData().setVisibleStatus(null);
    }

    @Override
    public void start()
    {
        citizen.getCitizenData().setVisibleStatus(VisibleCitizenStatus.SICK);
    }
}
