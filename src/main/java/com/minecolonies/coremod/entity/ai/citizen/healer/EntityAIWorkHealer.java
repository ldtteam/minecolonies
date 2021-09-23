package com.minecolonies.coremod.entity.ai.citizen.healer;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.*;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingHospital;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteraction;
import com.minecolonies.coremod.colony.jobs.JobHealer;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.network.messages.client.CircleParticleEffectMessage;
import com.minecolonies.coremod.network.messages.client.StreamParticleEffectMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
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
public class EntityAIWorkHealer extends AbstractEntityAIInteract<JobHealer, BuildingHospital>
{
    /**
     * Base xp gain for the smelter.
     */
    private static final double BASE_XP_GAIN = 2;

    /**
     * How many of each cure item it should try to request at a time.
     */
    private static final int REQUEST_COUNT = 16;

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

        final BuildingHospital hospital = getOwnBuilding();
        for (final AbstractEntityCitizen citizen : WorldUtil.getEntitiesWithinBuilding(world, AbstractEntityCitizen.class, getOwnBuilding(), cit -> cit.getCitizenDiseaseHandler().isSick()))
        {
            hospital.checkOrCreatePatientFile(citizen.getCivilianID());
        }

        for (final Patient patient : hospital.getPatients())
        {
            final ICitizenData data = hospital.getColony().getCitizenManager().getCivilian(patient.getId());
            if (data == null || !data.getEntity().isPresent() || (data.getEntity().isPresent() && !data.getEntity().get().getCitizenDiseaseHandler().isSick()))
            {
                hospital.removePatientFile(patient);
                continue;
            }
            final EntityCitizen citizen = (EntityCitizen) data.getEntity().get();
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

                    final ImmutableList<IRequest<? extends Stack>> list = getOwnBuilding().getOpenRequestsOfType(worker.getCitizenData().getId(), TypeToken.of(Stack.class));
                    final ImmutableList<IRequest<? extends Stack>> completed = getOwnBuilding().getCompletedRequestsOfType(worker.getCitizenData(), TypeToken.of(Stack.class));
                    for (final ItemStack cure : IColonyManager.getInstance().getCompatibilityManager().getDisease(diseaseName).getCure())
                    {
                        if (!InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), cure::sameItem))
                        {
                            if (InventoryUtils.getItemCountInItemHandler(getOwnBuilding().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseGet(null),
                              stack -> stack.sameItem(cure)) >= cure.getCount())
                            {
                                needsCurrently = new Tuple<>(stack -> stack.sameItem(cure), cure.getCount());
                                return GATHERING_REQUIRED_MATERIALS;
                            }
                            boolean hasCureRequested = false;
                            for (final IRequest<? extends Stack> request : list)
                            {
                                if (request.getRequest().getStack().sameItem(cure))
                                {
                                    hasCureRequested = true;
                                    break;
                                }
                            }
                            for (final IRequest<? extends Stack> request : completed)
                            {
                                if (request.getRequest().getStack().sameItem(cure))
                                {
                                    hasCureRequested = true;
                                    break;
                                }
                            }
                            if (!hasCureRequested)
                            {
                                patient.setState(Patient.PatientState.NEW);
                                break;
                            }
                        }
                    }
                }
                else
                {
                    data.triggerInteraction(new StandardInteraction(new TranslationTextComponent(PATIENT_FULL_INVENTORY), ChatPriority.BLOCKING));
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

        for (final PlayerEntity player : WorldUtil.getEntitiesWithinBuilding(world, PlayerEntity.class, getOwnBuilding(), player -> player.getHealth() < player.getMaxHealth() - 10 - (2 * getOwnBuilding().getBuildingLevel())))
        {
            playerToHeal = player;
            return CURE_PLAYER;
        }

        final ICitizenData data = getOwnBuilding().getColony().getCitizenManager().getRandomCitizen();
        if (data.getEntity().isPresent() && data.getEntity().get().getHealth() < 10.0
              && BlockPosUtil.getDistance2D(data.getEntity().get().blockPosition(), getOwnBuilding().getPosition()) < getOwnBuilding().getBuildingLevel() * 40)
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

        final ICitizenData data = getOwnBuilding().getColony().getCitizenManager().getCivilian(currentPatient.getId());
        if (data == null || !data.getEntity().isPresent() || !data.getEntity().get().getCitizenDiseaseHandler().isSick())
        {
            currentPatient = null;
            return DECIDE;
        }

        final EntityCitizen citizen = (EntityCitizen) data.getEntity().get();
        if (walkToBlock(citizen.blockPosition()))
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

        final ImmutableList<IRequest<? extends Stack>> list = getOwnBuilding().getOpenRequestsOfType(worker.getCitizenData().getId(), TypeToken.of(Stack.class));
        final ImmutableList<IRequest<? extends Stack>> completed = getOwnBuilding().getCompletedRequestsOfType(worker.getCitizenData(), TypeToken.of(Stack.class));

        for (final ItemStack cure : IColonyManager.getInstance().getCompatibilityManager().getDisease(diseaseName).getCure())
        {
            if (!InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), cure::sameItem)
                  && !InventoryUtils.hasItemInItemHandler(getOwnBuilding().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseGet(null), cure::sameItem))
            {
                boolean hasRequest = false;
                for (final IRequest<? extends Stack> request : list)
                {
                    if (request.getRequest().getStack().sameItem(cure))
                    {
                        hasRequest = true;
                        break;
                    }
                }
                for (final IRequest<? extends Stack> request : completed)
                {
                    if (request.getRequest().getStack().sameItem(cure))
                    {
                        hasRequest = true;
                        break;
                    }
                }
                if (!hasRequest)
                {
                    worker.getCitizenData().createRequestAsync(new Stack(cure, REQUEST_COUNT, 1));
                }
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

        final ICitizenData data = getOwnBuilding().getColony().getCitizenManager().getCivilian(currentPatient.getId());
        if (data == null || !data.getEntity().isPresent() || !data.getEntity().get().getCitizenDiseaseHandler().isSick())
        {
            currentPatient = null;
            return DECIDE;
        }

        final EntityCitizen citizen = (EntityCitizen) data.getEntity().get();
        if (walkToBlock(data.getEntity().get().blockPosition()))
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
                    if (InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), stack -> stack.sameItem(cure)) < cure.getCount())
                    {
                        needsCurrently = new Tuple<>(stack -> stack.sameItem(cure), 1);
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
                if (InventoryUtils.getItemCountInItemHandler(citizen.getInventoryCitizen(), stack -> stack.sameItem(cure)) < cure.getCount())
                {
                    if (InventoryUtils.isItemHandlerFull(citizen.getInventoryCitizen()))
                    {
                        data.triggerInteraction(new StandardInteraction(new TranslationTextComponent(PATIENT_FULL_INVENTORY), ChatPriority.BLOCKING));
                        currentPatient = null;
                        return DECIDE;
                    }
                    InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoNextFreeSlotInItemHandler(
                      worker.getInventoryCitizen(),
                      cure::sameItem,
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

        final ICitizenData data = getOwnBuilding().getColony().getCitizenManager().getCivilian(currentPatient.getId());
        if (data == null || !data.getEntity().isPresent() || !data.getEntity().get().getCitizenDiseaseHandler().isSick())
        {
            currentPatient = null;
            return DECIDE;
        }

        final EntityCitizen citizen = (EntityCitizen) data.getEntity().get();
        if (walkToBlock(citizen.blockPosition()))
        {
            progressTicks = 0;
            return FREE_CURE;
        }

        progressTicks++;
        if (progressTicks < MAX_PROGRESS_TICKS)
        {
            Network.getNetwork().sendToTrackingEntity(
              new StreamParticleEffectMessage(
                worker.position().add(0, 2, 0),
                citizen.position(),
                ParticleTypes.HEART,
                progressTicks % MAX_PROGRESS_TICKS,
                MAX_PROGRESS_TICKS), worker);

            Network.getNetwork().sendToTrackingEntity(
              new CircleParticleEffectMessage(
                worker.position().add(0, 2, 0),
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

        if (walkToBlock(new BlockPos(playerToHeal.position())))
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
        if (remotePatient == null || !remotePatient.getEntity().isPresent())
        {
            return DECIDE;
        }

        final EntityCitizen citizen = (EntityCitizen) remotePatient.getEntity().get();
        if (walkToBlock(remotePatient.getEntity().get().blockPosition()))
        {
            return getState();
        }

        Network.getNetwork().sendToTrackingEntity(
          new CircleParticleEffectMessage(
            remotePatient.getEntity().get().position(),
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
        return worker.getRandom().nextInt(60 * 60) <= Math.max(1, getSecondarySkillLevel() / 20);
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
            if (InventoryUtils.getItemCountInItemHandler(handler, stack -> stack.sameItem(cure)) < cure.getCount())
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public Class<BuildingHospital> getExpectedBuildingClass()
    {
        return BuildingHospital.class;
    }
}
