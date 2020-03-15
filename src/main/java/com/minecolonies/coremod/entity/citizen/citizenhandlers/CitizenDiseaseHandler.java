package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenDiseaseHandler;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.colony.jobs.JobHealer;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

import static com.minecolonies.api.util.constant.CitizenConstants.*;

/**
 * Handler taking care of citizens getting stuck.
 */
public class CitizenDiseaseHandler implements ICitizenDiseaseHandler
{
    /**
     * Health at which citizens seek a doctor.
     */
    public static final double SEEK_DOCTOR_HEALTH = 4.0;

    /**
     * Base likelihood of a citizen getting a disease.
     */
    private static final int DISEASE_FACTOR = 100000;

    /**
     * The citizen assigned to this manager.
     */
    private final AbstractEntityCitizen citizen;

    /**
     * The disease the citizen has, empty if none.
     */
    private String disease = "";

    /**
     * Constructor for the experience handler.
     * @param citizen the citizen owning the handler.
     */
    public CitizenDiseaseHandler(final EntityCitizen citizen)
    {
        this.citizen = citizen;
    }

    /**
     * Called in the citizen every few ticks to check if stuck.
     */
    @Override
    public void tick()
    {
        if (citizen.getTicksExisted() % TICKS_20 == 0 && !(citizen.getCitizenJobHandler().getColonyJob() instanceof JobHealer)
              && citizen.getCitizenColonyHandler().getColony().getCitizenManager().getCurrentCitizenCount() > IMinecoloniesAPI.getInstance().getConfig().getCommon().initialCitizenAmount.get())
        {
            final int citizenModifier = citizen.getCitizenJobHandler().getColonyJob() == null ? 1 : citizen.getCitizenJobHandler().getColonyJob().getDiseaseModifier();
            final int configModifier = MineColonies.getConfig().getCommon().diseaseModifier.get();
            if (citizen.getRandom().nextInt(configModifier * DISEASE_FACTOR / citizen.getCitizenColonyHandler().getColony().getCitizenManager().getCurrentCitizenCount()) < citizenModifier)
            {
                this.disease = IColonyManager.getInstance().getCompatibilityManager().getRandomDisease();
            }
        }
    }

    @Override
    public boolean isSick()
    {
        return !disease.isEmpty() || ( !(citizen.getCitizenJobHandler() instanceof AbstractJobGuard) && citizen.getHealth() <= SEEK_DOCTOR_HEALTH);
    }

    @Override
    public void write(final CompoundNBT compound)
    {
        compound.putString(TAG_DISEASE, disease);
    }

    @Override
    public void read(final CompoundNBT compound)
    {
        this.disease = compound.getString(TAG_DISEASE);
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
            final BlockPos hospitalPos = citizen.getCitizenColonyHandler().getColony().getBuildingManager().getBestHospital(citizen);
            final IColony colony = citizen.getCitizenColonyHandler().getColony();
            final IBuilding hospital = colony.getBuildingManager().getBuilding(hospitalPos);
            if (hospital != null)
            {
                hospital.onWakeUp();
            }
        }
    }
}
