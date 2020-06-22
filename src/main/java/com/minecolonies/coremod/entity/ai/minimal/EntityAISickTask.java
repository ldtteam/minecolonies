package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Disease;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingHospital;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteractionResponseHandler;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.network.messages.client.CircleParticleEffectMessage;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.properties.BedPart;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.List;

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
    public enum DiseaseState
    {
        IDLE,
        CHECK_FOR_CURE,
        GO_TO_HUT,
        SEARCH_HOSPITAL,
        GO_TO_HOSPITAL,
        WAIT_FOR_CURE,
        FIND_EMPTY_BED,
        APPLY_CURE
    }

    /**
     * The citizen assigned to this task.
     */
    private final EntityCitizen citizen;

    /**
     * The state the task is in currently.
     */
    private DiseaseState currentState = IDLE;

    /**
     * Restaurant to which the citizen should path.
     */
    private BlockPos placeToPath;

    /**
     * Delay ticks.
     */
    private int delayTicks = 0;

    /**
     * Instantiates this task.
     *
     * @param citizen the citizen.
     */
    public EntityAISickTask(final EntityCitizen citizen)
    {
        super();
        this.citizen = citizen;
        this.setMutexFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean shouldExecute()
    {
        if (citizen.getDesiredActivity() == DesiredActivity.SLEEP && !citizen.isSleeping())
        {
            return false;
        }

        if (currentState != IDLE)
        {
            return true;
        }

        return citizen.getCitizenDiseaseHandler().isSick();
    }

    @Override
    public void tick()
    {
        if (++delayTicks != TICKS_SECOND)
        {
            return;
        }
        delayTicks = 0;

        final ICitizenData citizenData = citizen.getCitizenData();
        if (citizenData == null)
        {
            return;
        }

        final IJob<?> job = citizen.getCitizenJobHandler().getColonyJob();
        if (job != null)
        {
            job.setActive(false);
        }

        citizen.addPotionEffect(new EffectInstance(Effects.SLOWNESS, TICKS_SECOND * 30));
        switch (currentState)
        {
            case CHECK_FOR_CURE:
                currentState = checkForCure(citizenData);
                return;
            case GO_TO_HUT:
                currentState = goToHut(citizenData);
                return;
            case SEARCH_HOSPITAL:
                currentState = searchHospital(citizenData);
                return;
            case GO_TO_HOSPITAL:
                currentState = goToHospital();
                return;
            case WAIT_FOR_CURE:
                currentState = waitForCure(citizenData);
                return;
            case APPLY_CURE:
                currentState = applyCure(citizenData);
                return;
            case FIND_EMPTY_BED:
                currentState = findEmptyBed();
                return;
            default:
                reset();
                break;
        }
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

        final BlockPos hospitalPos = citizen.getCitizenColonyHandler().getColony().getBuildingManager().getBestHospital(citizen);
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
                    final World world = citizen.world;
                    BlockState state = world.getBlockState(pos);
                    state = state.getBlock().getExtendedState(state, world, pos);
                    if (state.getBlock().isIn(BlockTags.BEDS)
                          && !state.get(BedBlock.OCCUPIED)
                          && state.get(BedBlock.PART).equals(BedPart.HEAD)
                          && world.isAirBlock(pos.up()))
                    {
                        usedBed = pos;
                        ((BuildingHospital) hospital).registerPatient(usedBed, citizen.getCitizenId());
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
     * @param citizenData the citizen.
     * @return the next state to go to, if successful idle.
     */
    private DiseaseState applyCure(final ICitizenData citizenData)
    {
        if (checkForCure(citizenData) != APPLY_CURE)
        {
            return CHECK_FOR_CURE;
        }

        final List<ItemStack> list = IColonyManager.getInstance().getCompatibilityManager().getDisease(citizen.getCitizenDiseaseHandler().getDisease()).getCure();
        citizen.setHeldItem(Hand.MAIN_HAND, list.get(citizen.getRandom().nextInt(list.size())));


        citizen.swingArm(Hand.MAIN_HAND);
        citizen.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(citizen.getRandom()));
            Network.getNetwork().sendToTrackingEntity(
              new CircleParticleEffectMessage(
                citizen.getPositionVec().add(0, 2, 0),
                ParticleTypes.HAPPY_VILLAGER,
                waitingTicks), citizen);


        waitingTicks++;
        if (waitingTicks < REQUIRED_TIME_TO_CURE)
        {
            return APPLY_CURE;
        }

        cure(citizenData);
        return IDLE;
    }

    /**
     * Cure the citizen.
     * 
     * @param citizenData the data of the citizen to cure.
     */
    private void cure(final ICitizenData citizenData)
    {
        final Disease disease = IColonyManager.getInstance().getCompatibilityManager().getDisease(citizen.getCitizenDiseaseHandler().getDisease());
        if (disease != null)
        {
            for (final ItemStack cure : disease.getCure())
            {
                final int slot = InventoryUtils.findFirstSlotInProviderNotEmptyWith(citizen, stack -> stack.isItemEqual(cure));
                if (slot != -1)
                {
                    citizenData.getInventory().extractItem(slot, 1, false);
                }
            }
        }

        if (usedBed != null)
        {
            final BlockPos hospitalPos = citizen.getCitizenColonyHandler().getColony().getBuildingManager().getBestHospital(citizen);
            final IColony colony = citizen.getCitizenColonyHandler().getColony();
            final IBuilding hospital = colony.getBuildingManager().getBuilding(hospitalPos);
            ((BuildingHospital) hospital).registerPatient(usedBed, 0);
            usedBed = null;
            citizen.getCitizenData().setBedPos(BlockPos.ZERO);
        }
        citizen.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
        citizenData.markDirty();
        citizen.getCitizenDiseaseHandler().cure();
        citizen.setHealth(citizen.getMaxHealth());
        reset();
    }

    /**
     * Stay in bed while waiting to be cured.
     *
     * @param citizenData the citizen to check.
     * @return the next state to go to.
     */
    private DiseaseState waitForCure(final ICitizenData citizenData)
    {
        final IColony colony = citizenData.getColony();
        placeToPath = colony.getBuildingManager().getBestHospital(citizen);

        if (placeToPath == null)
        {
            return SEARCH_HOSPITAL;
        }

        final DiseaseState state = checkForCure(citizenData);
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
            cure(citizenData);
            return IDLE;
        }

        if (!citizen.getCitizenSleepHandler().isAsleep() && BlockPosUtil.getDistance2D(placeToPath, citizen.getPosition()) > MINIMUM_DISTANCE_TO_HOSPITAL)
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
     * @param data the citizens data.
     * @return the next state to go to.
     */
    private DiseaseState goToHut(final ICitizenData data)
    {
        final IBuildingWorker buildingWorker = data.getWorkBuilding();
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

        if (citizen.getCitizenSleepHandler().isAsleep() || (citizen.getNavigator().noPath() && citizen.isWorkerAtSiteWithMove(placeToPath, MIN_DIST_TO_HOSPITAL)))
        {
            return WAIT_FOR_CURE;
        }
        return SEARCH_HOSPITAL;
    }

    /**
     * Search for a placeToPath within the colony of the citizen.
     *
     * @param citizenData the citizen.
     * @return the next state to go to.
     */
    private DiseaseState searchHospital(final ICitizenData citizenData)
    {
        final IColony colony = citizenData.getColony();
        placeToPath = colony.getBuildingManager().getBestHospital(citizen);

        if (placeToPath == null)
        {
            final String id = citizen.getCitizenDiseaseHandler().getDisease();
            if (id.isEmpty())
            {
                return IDLE;
            }
            final Disease disease = IColonyManager.getInstance().getCompatibilityManager().getDisease(id);
            citizenData.triggerInteraction(new StandardInteractionResponseHandler(new TranslationTextComponent(NO_HOSPITAL, disease.getName(), disease.getCureString()), new TranslationTextComponent(NO_HOSPITAL),
              ChatPriority.BLOCKING));
            return IDLE;
        }
        else if (!citizen.getCitizenDiseaseHandler().getDisease().isEmpty())
        {
            final Disease disease = IColonyManager.getInstance().getCompatibilityManager().getDisease(citizen.getCitizenDiseaseHandler().getDisease());
            citizenData.triggerInteraction(new StandardInteractionResponseHandler(new TranslationTextComponent(WAITING_FOR_CURE, disease.getName(), disease.getCureString()), new TranslationTextComponent(WAITING_FOR_CURE),
              ChatPriority.BLOCKING));
        }

        // Reset AI when starting to go to the hospital.
        if (citizen.getCitizenJobHandler().getColonyJob() != null)
        {
            citizen.getCitizenJobHandler().getColonyJob().resetAI();
        }
        return GO_TO_HOSPITAL;
    }

    /**
     * Checks if the citizen has the cure in the inventory and makes a decision based on that.
     *
     * @param citizenData the citizen to check.
     * @return the next state to go to.
     */
    private DiseaseState checkForCure(final ICitizenData citizenData)
    {
        final String id = citizen.getCitizenDiseaseHandler().getDisease();
        if (id.isEmpty())
        {
            if (citizen.getHealth() > SEEK_DOCTOR_HEALTH)
            {
                reset();
                currentState = IDLE;
                return IDLE;
            }
            return GO_TO_HUT;
        }
        final Disease disease = IColonyManager.getInstance().getCompatibilityManager().getDisease(id);
        for (final ItemStack cure : disease.getCure())
        {
            final int slot = InventoryUtils.findFirstSlotInProviderNotEmptyWith(citizen, stack -> stack.isItemEqual(cure));
            if (slot == -1)
            {
                if (citizen.getCitizenDiseaseHandler().isSick())
                {
                    return GO_TO_HUT;
                }

                reset();
                currentState = IDLE;
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
        citizen.stopActiveHand();
        citizen.resetActiveHand();
        citizen.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
        placeToPath = null;
        currentState = CHECK_FOR_CURE;
    }
}
