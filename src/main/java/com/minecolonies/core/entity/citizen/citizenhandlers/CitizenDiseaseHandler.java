package com.minecolonies.core.entity.citizen.citizenhandlers;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenDiseaseHandler;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingCook;
import com.minecolonies.core.colony.jobs.AbstractJobGuard;
import com.minecolonies.core.colony.jobs.JobHealer;
import com.minecolonies.core.datalistener.model.Disease;
import com.minecolonies.core.datalistener.DiseasesListener;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

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
    private static final int DISEASE_FACTOR = 100000 / 3;

    /**
     * Number of ticks after recovering a citizen is immune against any illness. 90 Minutes currently.
     */
    private static final int IMMUNITY_TIME = 20 * 60 * 90;

    /**
     * Additional immunity time through vaccines.
     */
    private static final int VACCINE_MODIFIER = 10;

    /**
     * The citizen assigned to this manager.
     */
    private final ICitizenData citizenData;

    /**
     * The disease the citizen has, empty if none.
     */
    @Nullable
    private Disease disease;

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
    public CitizenDiseaseHandler(final ICitizenData citizen)
    {
        this.citizenData = citizen;
    }

    /**
     * Called in the citizen every few ticks to check for illness. Called every 60 ticks
     */
    @Override
    public void update(final int tickRate)
    {
        if (canBecomeSick())
        {
            final double citizenModifier = citizenData.getDiseaseModifier();
            final int configModifier = MineColonies.getConfig().getServer().diseaseModifier.get();

            // normally it's one in 5 x 10.000

            if (citizenData.getRandom().nextInt(configModifier * DISEASE_FACTOR) < citizenModifier * 10)
            {
                this.disease = DiseasesListener.getRandomDisease(citizenData.getEntity().map(AbstractEntityCitizen::getRandom).orElse(RandomSource.create()));
            }
        }

        if (immunityTicks > 0)
        {
            immunityTicks -= tickRate;
        }
    }

    @Override
    public boolean setDisease(final @Nullable Disease disease)
    {
        if (canBecomeSick())
        {
            this.disease = disease;
            return true;
        }
        return false;
    }

    /**
     * Check if the citizen may become sick.
     *
     * @return true if so.
     */
    private boolean canBecomeSick()
    {
        return !isSick()
            && citizenData.getEntity().isPresent()
            && citizenData.getColony().isActive()
            && !(citizenData.getJob() instanceof JobHealer)
                 && immunityTicks <= 0
            && citizenData.getColony().getCitizenManager().getCurrentCitizenCount() > initialCitizenCount;
    }

    @Override
    public void onCollission(final ICitizenData citizen)
    {
        if (citizen.getCitizenDiseaseHandler().isSick()
              && canBecomeSick()
              && citizen.getRandom().nextInt(ONE_HUNDRED_PERCENT) < 1)
        {
            if (citizen.getColony().getResearchManager().getResearchEffects().getEffectStrength(MASKS) <= 0 || citizen.getRandom().nextBoolean())
            {
                this.disease = citizen.getCitizenDiseaseHandler().getDisease();
            }
        }
    }

    @Override
    public boolean isHurt()
    {
        return citizenData.getEntity().isPresent() && !(citizenData.getJob() instanceof AbstractJobGuard) && citizenData.getEntity().get().getHealth() < SEEK_DOCTOR_HEALTH
            && citizenData.getSaturation() > LOW_SATURATION;
    }

    @Override
    public boolean isSick()
    {
        return disease != null;
    }

    @Override
    public void write(final CompoundTag compound)
    {
        CompoundTag diseaseTag = new CompoundTag();
        if (disease != null)
        {
            diseaseTag.putString(TAG_DISEASE_ID, disease.id().toString());
        }
        diseaseTag.putInt(TAG_IMMUNITY, immunityTicks);
        compound.put(TAG_DISEASE, diseaseTag);
    }

    @Override
    public void read(final CompoundTag compound)
    {
        if (!compound.contains(TAG_DISEASE, Tag.TAG_COMPOUND))
        {
            return;
        }

        CompoundTag diseaseTag = compound.getCompound(TAG_DISEASE);
        if (diseaseTag.contains(TAG_DISEASE_ID))
        {
            this.disease = DiseasesListener.getDisease(new ResourceLocation(diseaseTag.getString(TAG_DISEASE_ID)));
        }
        this.immunityTicks = diseaseTag.getInt(TAG_IMMUNITY);
    }

    @Override
    @Nullable
    public Disease getDisease()
    {
        return this.disease;
    }

    @Override
    public void cure()
    {
        this.disease = null;
        sleepsAtHospital = false;
        if (citizenData.isAsleep() && citizenData.getEntity().isPresent())
        {
            citizenData.getEntity().get().stopSleeping();
            final BlockPos hospitalPos = citizenData.getColony().getBuildingManager().getBestBuilding(citizenData.getEntity().get(), BuildingCook.class);
            final IColony colony = citizenData.getColony();
            final IBuilding hospital = colony.getBuildingManager().getBuilding(hospitalPos);
            if (hospital != null)
            {
                hospital.onWakeUp();
            }

            if (citizenData.getColony().getResearchManager().getResearchEffects().getEffectStrength(VACCINES) > 0)
            {
                immunityTicks = IMMUNITY_TIME * VACCINE_MODIFIER;
            }
            else
            {
                immunityTicks = IMMUNITY_TIME;
            }
        }
        else
        {
            // Less immunity time if not cored in bed, but still have immunity time.
            immunityTicks = IMMUNITY_TIME / 2;
        }

        citizenData.getColony().getStatisticsManager().increment(CITIZENS_HEALED, citizenData.getColony().getDay());
        citizenData.markDirty(0);
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
