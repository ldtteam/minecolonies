package com.minecolonies.coremod.entity.ai.citizen.healer;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Disease;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingHospital;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteractionResponseHandler;
import com.minecolonies.coremod.colony.jobs.JobHealer;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.network.messages.CircleParticleEffectMessage;
import com.minecolonies.coremod.network.messages.StreamParticleEffectMessage;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.TranslationConstants.PATIENT_FULL_INVENTORY;

/**
 * Healer AI class.
 */
public class EntityAIWorkHealer extends AbstractEntityAIInteract<JobHealer>
{
    /**
     * Base xp gain for the smelter.
     */
    private static final double BASE_XP_GAIN = 5;

    /**
     * Area the worker targets.
     */
    private AxisAlignedBB targetArea = null;

    /**
     * The current patient.
     */
    private Patient currentPatient = null;

    /**
     * Variable to check if the draining is in progress. And at which tick it is.
     */
    private int progressTicks = 0;

    /**
     * Max progress ticks until drainage is complete (per Level).
     */
    private static final int MAX_PROGRESS_TICKS = 30;

    /**
     * Remote patient to treat.
     */
    private ICitizenData remotePatient;

    /**
     * Player to heal.
     */
    private PlayerEntity playerToHeal;

    /**
     * Constructor for the Cook. Defines the tasks the cook executes.
     *
     * @param job a cook job to use.
     */
    public EntityAIWorkHealer(@NotNull final JobHealer job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, 1),
          new AITarget(START_WORKING, DECIDE, 1),
          new AITarget(DECIDE, this::decide, 20),
          new AITarget(CURE, this::cure, 20),
          new AITarget(FREE_CURE, this::freeCure, 20),
          new AITarget(CURE_PLAYER, this::curePlayer, 20),
          new AITarget(REQUEST_CURE, this::requestCure, 20),
          new AITarget(WANDER, this::wander, 20)

        );
        worker.setCanPickUpLoot(true);
    }

    /**
     * Decide what to do next. Check if all patients are up date, else update their states. Then check if there is any patient we can cure or request things for.
     *
     * @return the next state to go to.
     */
    private IAIState decide()
    {
        if (walkToBuilding())
        {
            return DECIDE;
        }

        final BuildingHospital hospital = getOwnBuilding(BuildingHospital.class);
        for (final AbstractEntityCitizen citizen : world.getEntitiesWithinAABB(ModEntities.CITIZEN, getTargetableArea(), cit -> cit.getCitizenDiseaseHandler().isSick()))
        {
            hospital.checkOrCreatePatientFile(citizen.getCitizenId());
        }

        for (final Patient patient : hospital.getPatients())
        {
            final ICitizenData data = hospital.getColony().getCitizenManager().getCitizen(patient.getId());
            if (data == null || (data.getCitizenEntity().isPresent() && !data.getCitizenEntity().get().getCitizenDiseaseHandler().isSick()))
            {
                hospital.removePatientFile(patient);
                continue;
            }
            final EntityCitizen citizen = (EntityCitizen) data.getCitizenEntity().get();
            final String diseaseName = citizen.getCitizenDiseaseHandler().getDisease();
            @Nullable final Disease disease = diseaseName.isEmpty() ? null : IColonyManager.getInstance().getCompatibilityManager().getDisease(diseaseName);

            if (patient.getState() == Patient.PatientState.NEW)
            {
                this.currentPatient = patient;
                return REQUEST_CURE;
            }

            if (patient.getState() == Patient.PatientState.REQUESTED)
            {
                if (disease == null)
                {
                    this.currentPatient = patient;
                    return CURE;
                }

                if (testRandomCureChance())
                {
                    this.currentPatient = patient;
                    return FREE_CURE;
                }

                if (!InventoryUtils.isItemHandlerFull(citizen.getInventoryCitizen()))
                {
                    if (hasCureInInventory(disease, worker.getInventoryCitizen()) ||
                          hasCureInInventory(disease, getOwnBuilding().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseGet(null)))
                    {
                        this.currentPatient = patient;
                        return CURE;
                    }

                    final ImmutableList<IRequest<? extends Stack>> list = getOwnBuilding().getOpenRequestsOfType(worker.getCitizenData(), TypeToken.of(Stack.class));
                    for (final ItemStack cure : IColonyManager.getInstance().getCompatibilityManager().getDisease(diseaseName).getCure())
                    {
                        boolean hasCureRequested = false;
                        for (final IRequest<? extends Stack> request : list)
                        {
                            if (request.getRequest().getStack().isItemEqual(cure))
                            {
                                hasCureRequested = true;
                            }
                        }
                        if (!hasCureRequested)
                        {
                            patient.setState(Patient.PatientState.NEW);
                            break;
                        }
                    }
                }
                else
                {
                    data.triggerInteraction(new StandardInteractionResponseHandler(new TranslationTextComponent(PATIENT_FULL_INVENTORY), ChatPriority.BLOCKING));
                }
            }

            if (patient.getState() == Patient.PatientState.TREATED)
            {
                if (disease == null)
                {
                    this.currentPatient = patient;
                    return CURE;
                }

                if (!hasCureInInventory(disease, citizen.getInventoryCitizen()))
                {
                    patient.setState(Patient.PatientState.NEW);
                    return DECIDE;
                }
            }
        }

        for (final PlayerEntity player : world.getEntitiesWithinAABB(EntityType.PLAYER,
          getTargetableArea(),
          player -> player.getHealth() < player.getMaxHealth() - 10 - (2 * getOwnBuilding().getBuildingLevel())))
        {
            playerToHeal = player;
            return CURE_PLAYER;
        }

        final ICitizenData data = getOwnBuilding().getColony().getCitizenManager().getRandomCitizen();
        if (data.getCitizenEntity().isPresent() && data.getCitizenEntity().get().getHealth() < 10.0
              && BlockPosUtil.getDistance2D(data.getCitizenEntity().get().getPosition(), getOwnBuilding().getPosition()) < getOwnBuilding().getBuildingLevel() * 40)
        {
            remotePatient = data;
            return WANDER;
        }
        return DECIDE;
    }

    /**
     * Request the cure for a given patient.
     *
     * @return the next state to go to.
     */
    private IAIState requestCure()
    {
        if (currentPatient == null)
        {
            return DECIDE;
        }

        final ICitizenData data = getOwnBuilding().getColony().getCitizenManager().getCitizen(currentPatient.getId());
        if (data == null || !data.getCitizenEntity().isPresent() || !data.getCitizenEntity().get().getCitizenDiseaseHandler().isSick())
        {
            currentPatient = null;
            return DECIDE;
        }

        final EntityCitizen citizen = (EntityCitizen) data.getCitizenEntity().get();
        if (walkToBlock(citizen.getPosition()))
        {
            return REQUEST_CURE;
        }

        final String diseaseName = citizen.getCitizenDiseaseHandler().getDisease();
        if (diseaseName.isEmpty())
        {
            currentPatient.setState(Patient.PatientState.REQUESTED);
            currentPatient = null;
            return DECIDE;
        }

        final ImmutableList<IRequest<? extends Stack>> list = getOwnBuilding().getOpenRequestsOfType(worker.getCitizenData(), TypeToken.of(Stack.class));
        for (final ItemStack cure : IColonyManager.getInstance().getCompatibilityManager().getDisease(diseaseName).getCure())
        {
            boolean hasRequest = false;
            for (final IRequest<? extends Stack> request : list)
            {
                if (request.getRequest().getStack().isItemEqual(cure))
                {
                    hasRequest = true;
                }
            }
            if (!hasRequest)
            {
                worker.getCitizenData().createRequestAsync(new Stack(cure));
            }
        }

        currentPatient.setState(Patient.PatientState.REQUESTED);
        currentPatient = null;
        return DECIDE;
    }

    /**
     * Give a citizen the cure.
     *
     * @return the next state to go to.
     */
    private IAIState cure()
    {
        if (currentPatient == null)
        {
            return DECIDE;
        }

        final ICitizenData data = getOwnBuilding().getColony().getCitizenManager().getCitizen(currentPatient.getId());
        if (data == null || !data.getCitizenEntity().isPresent() || !data.getCitizenEntity().get().getCitizenDiseaseHandler().isSick())
        {
            currentPatient = null;
            return DECIDE;
        }

        final EntityCitizen citizen = (EntityCitizen) data.getCitizenEntity().get();
        if (walkToBlock(data.getCitizenEntity().get().getPosition()))
        {
            return CURE;
        }

        final String diseaseName = citizen.getCitizenDiseaseHandler().getDisease();
        final Disease disease = IColonyManager.getInstance().getCompatibilityManager().getDisease(diseaseName);
        if (diseaseName.isEmpty())
        {
            currentPatient = null;
            citizen.heal(10);
            worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
            citizen.markDirty();
            return DECIDE;
        }

        if (!hasCureInInventory(disease, worker.getInventoryCitizen()))
        {
            if (hasCureInInventory(disease, getOwnBuilding().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseGet(null)))
            {
                for (final ItemStack cure : disease.getCure())
                {
                    if (InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), stack -> stack.isItemEqual(cure)) < cure.getCount())
                    {
                        needsCurrently = new Tuple<>(stack -> stack.isItemEqual(cure), 1);
                        return GATHERING_REQUIRED_MATERIALS;
                    }
                }
            }
            currentPatient = null;
            return DECIDE;
        }

        if (!hasCureInInventory(disease, citizen.getInventoryCitizen()))
        {
            for (final ItemStack cure : disease.getCure())
            {
                if (InventoryUtils.getItemCountInItemHandler(citizen.getInventoryCitizen(), stack -> stack.isItemEqual(cure)) < cure.getCount())
                {
                    if (InventoryUtils.isItemHandlerFull(citizen.getInventoryCitizen()))
                    {
                        data.triggerInteraction(new StandardInteractionResponseHandler(new TranslationTextComponent(PATIENT_FULL_INVENTORY), ChatPriority.BLOCKING));
                        currentPatient = null;
                        return DECIDE;
                    }
                    InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoNextFreeSlotInItemHandler(
                      worker.getInventoryCitizen(),
                      cure::isItemEqual,
                      cure.getCount(), citizen.getInventoryCitizen()
                    );
                }
            }
        }

        worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
        currentPatient.setState(Patient.PatientState.TREATED);
        currentPatient = null;
        return DECIDE;
    }

    /**
     * Do free cure magic.
     *
     * @return the next state to go to.
     */
    private IAIState freeCure()
    {
        if (currentPatient == null)
        {
            return DECIDE;
        }

        final ICitizenData data = getOwnBuilding().getColony().getCitizenManager().getCitizen(currentPatient.getId());
        if (data == null || !data.getCitizenEntity().isPresent() || !data.getCitizenEntity().get().getCitizenDiseaseHandler().isSick())
        {
            currentPatient = null;
            return DECIDE;
        }

        final EntityCitizen citizen = (EntityCitizen) data.getCitizenEntity().get();
        if (walkToBlock(citizen.getPosition()))
        {
            progressTicks = 0;
            return FREE_CURE;
        }

        progressTicks++;
        if (progressTicks < MAX_PROGRESS_TICKS)
        {
            Network.getNetwork().sendToTrackingEntity(
              new StreamParticleEffectMessage(
                worker.getPositionVec().add(0, 2, 0),
                citizen.getPositionVec(),
                ParticleTypes.HEART,
                progressTicks % MAX_PROGRESS_TICKS,
                MAX_PROGRESS_TICKS), worker);

            Network.getNetwork().sendToTrackingEntity(
              new CircleParticleEffectMessage(
                worker.getPositionVec().add(0, 2, 0),
                ParticleTypes.HEART,
                progressTicks), worker);

            return getState();
        }

        progressTicks = 0;
        worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
        citizen.getCitizenDiseaseHandler().cure();
        currentPatient.setState(Patient.PatientState.TREATED);
        currentPatient = null;
        return DECIDE;
    }

    /**
     * Cure the player.
     *
     * @return the next sate to go to.
     */
    private IAIState curePlayer()
    {
        if (playerToHeal == null)
        {
            return DECIDE;
        }

        if (walkToBlock(playerToHeal.getPosition()))
        {
            return getState();
        }

        playerToHeal.heal(playerToHeal.getMaxHealth() - playerToHeal.getHealth() - 5 - getOwnBuilding().getBuildingLevel());
        worker.getCitizenExperienceHandler().addExperience(1);

        return DECIDE;
    }

    @Override
    public IAIState getStateAfterPickUp()
    {
        return CURE;
    }

    /**
     * Wander around in the colony from citizen to citizen.
     *
     * @return the next state to go to.
     */
    private IAIState wander()
    {
        if (remotePatient == null || !remotePatient.getCitizenEntity().isPresent())
        {
            return DECIDE;
        }

        final EntityCitizen citizen = (EntityCitizen) remotePatient.getCitizenEntity().get();
        if (walkToBlock(remotePatient.getCitizenEntity().get().getPosition()))
        {
            return getState();
        }

        Network.getNetwork().sendToTrackingEntity(
          new CircleParticleEffectMessage(
            remotePatient.getCitizenEntity().get().getPositionVec(),
            ParticleTypes.HEART,
            1), worker);

        citizen.heal(citizen.getMaxHealth() - citizen.getHealth() - 5 - getOwnBuilding().getBuildingLevel());
        citizen.markDirty();
        worker.getCitizenExperienceHandler().addExperience(1);

        remotePatient = null;

        return START_WORKING;
    }

    /**
     * Check if we can cure a citizen randomly. Currently it is done workerLevel/10 times every hour (at least 1).
     *
     * @return true if so.
     */
    private boolean testRandomCureChance()
    {
        return worker.getRandom().nextInt(60 * 60) <= Math.max(1, worker.getCitizenData().getJobModifier() / 10);
    }

    /**
     * Check if the cure for a certain illness is in the inv.
     *
     * @param disease the disease to check.
     * @param handler the inventory to check.
     * @return true if so.
     */
    private boolean hasCureInInventory(final Disease disease, final IItemHandler handler)
    {
        for (final ItemStack cure : disease.getCure())
        {
            if (InventoryUtils.getItemCountInItemHandler(handler, stack -> stack.isItemEqual(cure)) < cure.getCount())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a simple area around the Hospitals's Hut used for AABB calculations for finding sick citizens.
     *
     * @return The {@link AxisAlignedBB} of the Hut Area
     */
    private AxisAlignedBB getTargetableArea()
    {
        if (targetArea == null)
        {
            targetArea = getOwnBuilding().getTargetableArea(world);
        }
        return targetArea;
    }

    @Override
    public Class getExpectedBuildingClass()
    {
        return BuildingHospital.class;
    }
}
