package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorkerModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingEventsModule;
import com.minecolonies.api.colony.buildings.modules.ICreatesResolversModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.colony.buildings.modules.ITickingModule;
import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.jobs.JobArcherTraining;
import com.minecolonies.coremod.colony.jobs.JobCombatTraining;
import com.minecolonies.coremod.util.AttributeModifierUtils;
import com.minecolonies.coremod.util.BuildingUtils;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

import static com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards.HIRE_TRAINEE;

/**
 * Assignment module for guards.
 */
public class GuardBuildingModule extends WorkAtHomeBuildingModule implements IBuildingEventsModule, ITickingModule, IPersistentModule, IBuildingWorkerModule, ICreatesResolversModule
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
            optCitizen.get().setItemSlot(EquipmentSlot.CHEST, ItemStackUtils.EMPTY);
            optCitizen.get().setItemSlot(EquipmentSlot.FEET, ItemStackUtils.EMPTY);
            optCitizen.get().setItemSlot(EquipmentSlot.HEAD, ItemStackUtils.EMPTY);
            optCitizen.get().setItemSlot(EquipmentSlot.LEGS, ItemStackUtils.EMPTY);
            optCitizen.get().setItemSlot(EquipmentSlot.MAINHAND, ItemStackUtils.EMPTY);
            optCitizen.get().setItemSlot(EquipmentSlot.OFFHAND, ItemStackUtils.EMPTY);
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
        if (random.nextInt(building.getModules(GuardBuildingModule.class).size()) > 0)
        {
            return;
        }

        boolean hiredFromTraining = false;

        // If we have no active worker, attempt to grab one from the appropriate trainer
        if (building.getSetting(HIRE_TRAINEE).getValue() && !isFull() &&
                BuildingUtils.canAutoHire(building, getHiringMode(), getJobEntry()))
        {
            ICitizenData trainingCitizen = null;
            int maxSkill = 0;

            for (ICitizenData trainee : colony.getCitizenManager().getCitizens())
            {
                if ((getJobEntry() == ModJobs.archer.get() && trainee.getJob() instanceof JobArcherTraining
                      || getJobEntry() == ModJobs.knight.get() && trainee.getJob() instanceof JobCombatTraining)
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
        if (building instanceof AbstractBuildingGuards)
        {
            // Start timeout to not be stuck with an old patrol target
            ((AbstractBuildingGuards) building).setPatrolTimer(5);
        }
    }
}
