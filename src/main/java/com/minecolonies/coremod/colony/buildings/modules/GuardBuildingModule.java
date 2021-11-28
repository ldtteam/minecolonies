package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorkerModule;
import com.minecolonies.api.colony.buildings.modules.*;
import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.jobs.JobArcherTraining;
import com.minecolonies.coremod.colony.jobs.JobCombatTraining;
import com.minecolonies.coremod.util.AttributeModifierUtils;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

import static com.minecolonies.api.util.constant.CitizenConstants.GUARD_HEALTH_MOD_BUILDING_NAME;
import static com.minecolonies.api.util.constant.CitizenConstants.GUARD_HEALTH_MOD_CONFIG_NAME;
import static com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards.HIRE_TRAINEE;

/**
 * Assignment module for jobs that have to live at the work place mandatorily.
 */
public class GuardBuildingModule extends WorkAtHomeBuildingModule implements IAssignsCitizen, IBuildingEventsModule, ITickingModule, IPersistentModule, IBuildingWorkerModule, ICreatesResolversModule
{
    /**
     * Random obj.
     */
    private static final Random random = new Random();

    public GuardBuildingModule(
      final GuardType type,
      final boolean canWorkingDuringRain,
      final Function<IBuilding, Integer> sizeLimit)
    {
        super(type.getJobEntry().get(), type.getPrimarySkill(), type.getSecondarySkill(), canWorkingDuringRain, sizeLimit);
    }

    @Override
    void onRemoval(final ICitizenData citizen)
    {
        super.onRemoval(citizen);
        final Optional<AbstractEntityCitizen> optCitizen = citizen.getEntity();
        if (optCitizen.isPresent())
        {
            AttributeModifierUtils.removeAllHealthModifiers(optCitizen.get());
            optCitizen.get().setItemSlot(EquipmentSlotType.CHEST, ItemStackUtils.EMPTY);
            optCitizen.get().setItemSlot(EquipmentSlotType.FEET, ItemStackUtils.EMPTY);
            optCitizen.get().setItemSlot(EquipmentSlotType.HEAD, ItemStackUtils.EMPTY);
            optCitizen.get().setItemSlot(EquipmentSlotType.LEGS, ItemStackUtils.EMPTY);
            optCitizen.get().setItemSlot(EquipmentSlotType.MAINHAND, ItemStackUtils.EMPTY);
            optCitizen.get().setItemSlot(EquipmentSlotType.OFFHAND, ItemStackUtils.EMPTY);
        }
    }

    @Override
    public boolean isFull()
    {
        return building.getAllAssignedCitizen().size() >= getModuleMax();
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        // Give the other assignment module also a chance.
        if (random.nextBoolean())
        {
            return;
        }

        boolean hiredFromTraining = false;

        // If we have no active worker, attempt to grab one from the appropriate trainer
        if (building.getSetting(HIRE_TRAINEE).getValue() && !isFull() && ((building.getBuildingLevel() > 0 && building.isBuilt()))
              && (this.getHiringMode() == HiringMode.DEFAULT && !building.getColony().isManualHiring() || this.getHiringMode() == HiringMode.AUTO))
        {
            ICitizenData trainingCitizen = null;
            int maxSkill = 0;

            for (ICitizenData trainee : colony.getCitizenManager().getCitizens())
            {
                if ((getJobEntry() == ModJobs.archer && trainee.getJob() instanceof JobArcherTraining)
                      || (getJobEntry() == ModJobs.knight && trainee.getJob() instanceof JobCombatTraining)
                           && trainee.getCitizenSkillHandler().getLevel(getPrimarySkill()) > maxSkill)
                {
                    maxSkill = trainee.getCitizenSkillHandler().getLevel(getPrimarySkill());
                    trainingCitizen = trainee;
                }
            }

            if (trainingCitizen != null)
            {
                hiredFromTraining = true;
                assignCitizen(trainingCitizen);
            }
        }

        //If we hired, we may have more than one to hire, so let's skip the superclass until next time.
        if (!hiredFromTraining)
        {
            super.onColonyTick(colony);
        }
    }

    @Override
    void onAssignment(final ICitizenData citizen)
    {
        super.onAssignment(citizen);
        final Optional<AbstractEntityCitizen> optCitizen = citizen.getEntity();
        if (optCitizen.isPresent() && building instanceof AbstractBuildingGuards)
        {
            final AbstractEntityCitizen citizenEntity = optCitizen.get();
            AttributeModifierUtils.addHealthModifier(citizenEntity,
              new AttributeModifier(GUARD_HEALTH_MOD_BUILDING_NAME, ((AbstractBuildingGuards) building).getBonusHealth(), AttributeModifier.Operation.ADDITION));
            AttributeModifierUtils.addHealthModifier(citizenEntity,
              new AttributeModifier(GUARD_HEALTH_MOD_CONFIG_NAME,
                MineColonies.getConfig().getServer().guardHealthMult.get() - 1.0,
                AttributeModifier.Operation.MULTIPLY_TOTAL));

            // Start timeout to not be stuck with an old patrol target
            ((AbstractBuildingGuards) building).setPatrolTimer(5);
        }
    }
}
