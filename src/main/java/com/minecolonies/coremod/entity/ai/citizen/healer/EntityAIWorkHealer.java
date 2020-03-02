package com.minecolonies.coremod.entity.ai.citizen.healer;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingFurnaceUser;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingHospital;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteractionResponseHandler;
import com.minecolonies.coremod.colony.jobs.JobHealer;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_REQUESTS_SMELTABLE_ORE;
import static com.minecolonies.api.util.constant.TranslationConstants.FURNACE_USER_NO_ORE;

/**
 * Healer AI class.
 */
public class EntityAIWorkHealer extends AbstractEntityAIInteract<JobHealer>
{
    /**
     * How often should charisma factor into the cook's skill modifier.
     */
    private static final int CHARISMA_MULTIPLIER = 1;

    /**
     * How often should intelligence factor into the cook's skill modifier.
     */
    private static final int INTELLIGENCE_MULTIPLIER = 2;

    /**
     * Area the worker targets.
     */
    private AxisAlignedBB targetArea = null;

    /**
     * The current patient.
     */
    private Patient currentPatient = null;

    /**
     * Constructor for the Cook.
     * Defines the tasks the cook executes.
     *
     * @param job a cook job to use.
     */
    public EntityAIWorkHealer(@NotNull final JobHealer job)
    {
        super(job);
        super.registerTargets(
          new AITarget(START_WORKING, DECIDE, 1),
          new AITarget(DECIDE, this::decide, 20),
          new AITarget(CURE, this::cure, 20),
          new AITarget(FREE_CURE, this::freeCure, 20),
          new AITarget(REQUEST_CURE, this::requestCure, 20),
          new AITarget(WANDER, this::wander, 20)

        );
        //super.registerTargets(new AITarget(COOK_SERVE_FOOD_TO_CITIZEN, this::serveFoodToCitizen, SERVE_DELAY));
        worker.getCitizenExperienceHandler().setSkillModifier(CHARISMA_MULTIPLIER * worker.getCitizenData().getCharisma()
                + INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence());
        worker.setCanPickUpLoot(true);
    }

    /**
     * Request the cure for a given patient.
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
            return DECIDE;
        }

        if (walkToBlock(data.getCitizenEntity().get().getPosition()))
        {
            return REQUEST_CURE;
        }

        final String disease = data.getCitizenEntity().get().getCitizenDiseaseHandler().getDisease();
        if (disease.isEmpty())
        {
            currentPatient.setState(Patient.PatientState.REQUESTED);
            return DECIDE;
        }

        final ImmutableList<IRequest<? extends Stack>> list = getOwnBuilding().getOpenRequestsOfType(worker.getCitizenData(), TypeToken.of(Stack.class));
        for (final ItemStack cure : IColonyManager.getInstance().getCompatibilityManager().getDisease(disease).getCure())
        {
            for (final IRequest<? extends Stack> request : list)
            {
                if (!request.getRequest().getStack().isItemEqual(cure))
                {
                    worker.getCitizenData().createRequestAsync(new Stack(cure));
                }
            }
        }

        currentPatient = null;
        return DECIDE;
    }

    /**
     * Decide what to do next.
     * Check if all patients are up date, else update their states.
     * Then check if there is any patient we can cure or request things for.
     *
     * @return the next state to go to.
     */
    private IAIState decide()
    {
        if ( walkToBuilding() )
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

            if (patient.getState() == Patient.PatientState.NEW)
            {
                this.currentPatient = patient;
                return REQUEST_CURE;
            }

            if (patient.getState() == Patient.PatientState.REQUESTED)
            {
                final String disease = data.getCitizenEntity().get().getCitizenDiseaseHandler().getDisease();
                if (disease.isEmpty())
                {
                    this.currentPatient = patient;
                    return CURE;
                }

                //todo, add a random chance of curing the patient depending on the workers level
                for (final ItemStack cure : IColonyManager.getInstance().getCompatibilityManager().getDisease(disease).getCure())
                {
                    if (InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), stack -> stack.isItemEqual(cure)) < cure.getCount())
                    {
                        patient.setState(Patient.PatientState.NEW);
                        return DECIDE;
                    }
                }

                this.currentPatient = patient;
                return CURE;
            }

            if (patient.getState() == Patient.PatientState.TREATED)
            {
                //todo check if the citizen has the cure item in the inventory.
            }
        }
        return WANDER;
    }

    /**
     * Give a citizen the cure.
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
            return DECIDE;
        }

        if (walkToBlock(data.getCitizenEntity().get().getPosition()))
        {
            return CURE;
        }

        final String disease = data.getCitizenEntity().get().getCitizenDiseaseHandler().getDisease();
        if (disease.isEmpty())
        {
            data.getCitizenEntity().get().heal(5);
            //todo give the worker the cure
            return DECIDE;
        }

        for (final ItemStack cure : IColonyManager.getInstance().getCompatibilityManager().getDisease(disease).getCure())
        {
            if (InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), stack -> stack.isItemEqual(cure)) < cure.getCount())
            {
                currentPatient.setState(Patient.PatientState.NEW);
                currentPatient = null;
                return DECIDE;
            }
        }

        //todo give the worker the cure
        return DECIDE;
    }

    private IAIState freeCure()
    {
        if (currentPatient == null)
        {
            return DECIDE;
        }

        final ICitizenData data = getOwnBuilding().getColony().getCitizenManager().getCitizen(currentPatient.getId());
        if (data == null || !data.getCitizenEntity().isPresent() || !data.getCitizenEntity().get().getCitizenDiseaseHandler().isSick())
        {
            return DECIDE;
        }

        if (walkToBlock(data.getCitizenEntity().get().getPosition()))
        {
            return CURE;
        }

        //todo, do some particle effect and magic and then cure the worker.

        return DECIDE;
    }

    private IAIState wander()
    {
        // * If none of the above, then we go into HOUSE_DOCTOR
        //* check list of citizens for citizens with low health, check for a citizen in building level distance and go visit and heal them
        return null;
    }


    /**
     * Creates a simple area around the Hospitals's Hut used for AABB calculations for finding sick citizens.
     *
     * @return The {@link AxisAlignedBB} of the Hut Area
     */
    private AxisAlignedBB getTargetableArea()
    {
        if(targetArea == null)
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
