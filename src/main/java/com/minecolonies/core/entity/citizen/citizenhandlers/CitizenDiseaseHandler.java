package com.minecolonies.core.entity.citizen.citizenhandlers;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenDiseaseHandler;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingCook;
import com.minecolonies.core.colony.jobs.AbstractJobGuard;
import com.minecolonies.core.colony.jobs.JobHealer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.research.util.ResearchConstants.MASKS;
import static com.minecolonies.api.research.util.ResearchConstants.VACCINES;
import static com.minecolonies.api.util.constant.CitizenConstants.*;
import static com.minecolonies.api.util.constant.Constants.ONE_HUNDRED_PERCENT;
import static com.minecolonies.api.util.constant.StatisticsConstants.CITIZENS_HEALED;

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
    private static final int DISEASE_FACTOR = 100000;

    /**
     * Number of seconds after recovering a citizen is immune against any illness.
     */
    private static final int IMMUNITY_TIME = 60 * 10 * 7;

    /**
     * Additional immunity time through vaccines.
     */
    private static final int VACCINE_MODIFIER = 10;

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
     * Whether the citizen sleeps at the hostpital
     */
    private boolean sleepsAtHospital = false;

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
            final double citizenModifier = citizen.getCitizenData().getDiseaseModifier();
            final int configModifier = MineColonies.getConfig().getServer().diseaseModifier.get();

            // normally it's one in 5 x 10.000

            if (!IColonyManager.getInstance().getCompatibilityManager().getDiseases().isEmpty() &&
                  citizen.getRandom().nextInt(configModifier * DISEASE_FACTOR) < citizenModifier * 10)
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
     *
     * @return true if so.
     */
    private boolean canBecomeSick()
    {
        return !isSick()
                 && citizen.getCitizenColonyHandler().getColonyOrRegister() != null
                 && citizen.getCitizenColonyHandler().getColonyOrRegister().isActive()
                 && !(citizen.getCitizenJobHandler().getColonyJob() instanceof JobHealer)
                 && immunityTicks <= 0
                 && citizen.getCitizenColonyHandler().getColonyOrRegister().getCitizenManager().getCurrentCitizenCount() > initialCitizenCount;
    }

    @Override
    public void onCollission(@NotNull final AbstractEntityCitizen citizen)
    {
        if (citizen.getCitizenDiseaseHandler().isSick()
              && canBecomeSick()
              && citizen.getRandom().nextInt(ONE_HUNDRED_PERCENT) < 1)
        {
            if (citizen.getCitizenColonyHandler().getColonyOrRegister() != null
                  && (citizen.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(MASKS) <= 0 || citizen.getRandom().nextBoolean()))
            {
                this.disease = citizen.getCitizenDiseaseHandler().getDisease();
            }
        }
    }

    @Override
    public boolean isHurt()
    {
        return !(citizen.getCitizenJobHandler() instanceof AbstractJobGuard) && citizen.getHealth() < SEEK_DOCTOR_HEALTH && citizen.getCitizenData().getSaturation() > LOW_SATURATION;
    }

    @Override
    public boolean isSick()
    {
        return !disease.isEmpty();
    }

    @Override
    public void write(final CompoundTag compound)
    {
        compound.putString(TAG_DISEASE, disease);
        compound.putInt(TAG_IMMUNITY, immunityTicks);
    }

    @Override
    public void read(final CompoundTag compound)
    {
        // cure diseases that have been removed from the configuration file.
        if (IColonyManager.getInstance().getCompatibilityManager().getDisease(compound.getString(TAG_DISEASE)) != null)
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
        sleepsAtHospital = false;
        if (citizen.getCitizenSleepHandler().isAsleep())
        {
            citizen.stopSleeping();
            final BlockPos hospitalPos = citizen.getCitizenColonyHandler().getColonyOrRegister().getBuildingManager().getBestBuilding(citizen, BuildingCook.class);
            final IColony colony = citizen.getCitizenColonyHandler().getColonyOrRegister();
            final IBuilding hospital = colony.getBuildingManager().getBuilding(hospitalPos);
            if (hospital != null)
            {
                hospital.onWakeUp();
            }

            if (citizen.getCitizenColonyHandler().getColonyOrRegister() != null && citizen.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(VACCINES) > 0)
            {
                immunityTicks = IMMUNITY_TIME * VACCINE_MODIFIER;
            }
            else
            {
                immunityTicks = IMMUNITY_TIME;
            }

            citizen.getCitizenColonyHandler().getColonyOrRegister().getStatisticsManager().increment(CITIZENS_HEALED, citizen.getCitizenColonyHandler().getColonyOrRegister().getDay());
        }

        citizen.markDirty(0);
    }

    @Override
    public boolean sleepsAtHospital()
    {
        return sleepsAtHospital;
    }

    @Override
    public void setSleepsAtHospital(final boolean isAtHospital)
    {
        sleepsAtHospital = isAtHospital;
    }
}
