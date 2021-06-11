package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenDiseaseHandler;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCook;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.colony.jobs.JobHealer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.CitizenConstants.TAG_DISEASE;
import static com.minecolonies.api.util.constant.CitizenConstants.TAG_IMMUNITY;
import static com.minecolonies.api.util.constant.Constants.ONE_HUNDRED_PERCENT;

/**
 * Handler taking care of citizens getting stuck.
 */
public class CitizenDiseaseHandler implements ICitizenDiseaseHandler
{
    /**
     * Health at which citizens seek a doctor.
     */
    public static final double SEEK_DOCTOR_HEALTH = 6.0;

    /**
     * Base likelihood of a citizen getting a disease.
     */
    private static final int DISEASE_FACTOR = 10000;

    /**
     * Number of seconds after recovering a citizen is immune against any illness.
     */
    private static final int IMMUNITY_TIME  = 60 * 10 * 7;

    /**
     * The citizen assigned to this manager.
     */
    private final AbstractEntityCitizen citizen;

    /**
     * The disease the citizen has, empty if none.
     */
    private String disease = "";

    /**
     * Special immunity time after being cured.
     */
    private int immunityTicks = 0;

    /**
     * The initial citizen count
     */
    private static final int initialCitizenCount = IMinecoloniesAPI.getInstance()
                                                     .getConfig()
                                                     .getServer().initialCitizenAmount.get();

    /**
     * Constructor for the experience handler.
     *
     * @param citizen the citizen owning the handler.
     */
    public CitizenDiseaseHandler(final AbstractEntityCitizen citizen)
    {
        this.citizen = citizen;
    }

    /**
     * Called in the citizen every few ticks to check for illness.
     */
    @Override
    public void tick()
    {
        if (canBecomeSick())
        {
            final int citizenModifier = citizen.getCitizenJobHandler().getColonyJob() == null ? 1 : citizen.getCitizenJobHandler().getColonyJob().getDiseaseModifier();
            final int configModifier = MineColonies.getConfig().getServer().diseaseModifier.get();
            if (!IColonyManager.getInstance().getCompatibilityManager().getDiseases().isEmpty() &&
                  citizen.getRandom().nextInt(configModifier * DISEASE_FACTOR) < citizenModifier)
            {
                this.disease = IColonyManager.getInstance().getCompatibilityManager().getRandomDisease();
            }
        }

        if (immunityTicks > 0)
        {
            immunityTicks--;
        }
    }

    /**
     * Check if the citizen may become sick.
     * @return true if so.
     */
    private boolean canBecomeSick()
    {
        return !isSick()
             && citizen.getCitizenColonyHandler().getColony().isActive()
             && !(citizen.getCitizenJobHandler().getColonyJob() instanceof JobHealer)
             && immunityTicks <= 0
                 && citizen.getCitizenColonyHandler().getColony().getCitizenManager().getCurrentCitizenCount() > initialCitizenCount;
    }

    @Override
    public void onCollission(@NotNull final AbstractEntityCitizen citizen)
    {
        if (citizen.getCitizenDiseaseHandler().isSick()
              && canBecomeSick()
              && citizen.getRandom().nextInt(ONE_HUNDRED_PERCENT) < 1)
        {
            this.disease = citizen.getCitizenDiseaseHandler().getDisease();
        }
    }

    @Override
    public boolean isSick()
    {
        return !disease.isEmpty() || (!(citizen.getCitizenJobHandler() instanceof AbstractJobGuard) && citizen.getHealth() <= SEEK_DOCTOR_HEALTH);
    }

    @Override
    public void write(final CompoundNBT compound)
    {
        compound.putString(TAG_DISEASE, disease);
        compound.putInt(TAG_IMMUNITY, immunityTicks);
    }

    @Override
    public void read(final CompoundNBT compound)
    {
        // cure diseases that have been removed from the configuration file.
        if(IColonyManager.getInstance().getCompatibilityManager().getDisease(compound.getString(TAG_DISEASE)) != null)
        {
            this.disease = compound.getString(TAG_DISEASE);
        }
        this.immunityTicks = compound.getInt(TAG_IMMUNITY);
    }

    @Override
    public String getDisease()
    {
        return this.disease;
    }

    @Override
    public void cure()
    {
        this.disease = "";
        if (citizen.getCitizenSleepHandler().isAsleep())
        {
            citizen.wakeUp();
            final BlockPos hospitalPos = citizen.getCitizenColonyHandler().getColony().getBuildingManager().getBestBuilding(citizen, BuildingCook.class);
            final IColony colony = citizen.getCitizenColonyHandler().getColony();
            final IBuilding hospital = colony.getBuildingManager().getBuilding(hospitalPos);
            if (hospital != null)
            {
                hospital.onWakeUp();
            }
            immunityTicks = IMMUNITY_TIME;
        }
    }
}
