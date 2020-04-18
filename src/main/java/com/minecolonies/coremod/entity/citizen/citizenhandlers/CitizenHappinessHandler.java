package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.interactionhandling.InteractionValidatorRegistry;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenHappinessHandler;
import com.minecolonies.api.entity.citizen.happiness.*;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteractionResponseHandler;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.colony.jobs.JobPupil;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.util.constant.HappinessConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * The new happiness handler for the citizen.
 */
public class CitizenHappinessHandler implements ICitizenHappinessHandler
{
    /**
     * The different happiness factor.
     */
    public Map<String, IHappinessModifier> happinessFactors = new HashMap<>();

    /**
     * Create a new instance of the citizen happiness handler.
     *
     * @param data the data to handle.
     */
    public CitizenHappinessHandler(final ICitizenData data)
    {
        add(new TimeBasedHappinessModifier(HOMELESSNESS, 4.0, () -> data.getHomeBuilding() == null ? 0.25 : data.getHomeBuilding().getBuildingLevel() / 2.5, new Tuple[] {new Tuple<>(COMPLAIN_DAYS_WITHOUT_HOUSE, 0.75), new Tuple<>(DEMANDS_DAYS_WITHOUT_HOUSE, 0.5)}));
        add(new TimeBasedHappinessModifier(UNEMPLOYMENT, 2.0, () -> data.isChild() ? 1.0 : (data.getWorkBuilding() == null ? 0.5 : data.getWorkBuilding().getBuildingLevel() > 3 ? 2.0 : 1.0), new Tuple[] {new Tuple<>(COMPLAIN_DAYS_WITHOUT_JOB, 0.75), new Tuple<>(DEMANDS_DAYS_WITHOUT_JOB, 0.5)}));
        add(new TimeBasedHappinessModifier(HEALTH, 2.0, () -> data.getCitizenEntity().isPresent() ? (data.getCitizenEntity().get().getCitizenDiseaseHandler().isSick() ? 0.5 : 1.0) : 1.0, new Tuple[] {new Tuple<>(COMPLAIN_DAYS_SICK, 0.5), new Tuple<>(DEMANDS_CURE_SICK, 0.1)}));
        add(new TimeBasedHappinessModifier(IDLEATJOB, 1.0, () -> data.isIdleAtJob() ? 0.5 : 1.0, new Tuple[] {new Tuple<>(IDLE_AT_JOB_COMPLAINS_DAYS, 0.5), new Tuple<>(IDLE_AT_JOB_DEMANDS_DAYS, 0.1)}));

        add(new StaticHappinessModifier(SCHOOL, 1.0, () -> data.isChild() ? data.getJob() instanceof JobPupil ? 2 : 0 : 1));
        add(new StaticHappinessModifier(SECURITY, 5.0, () -> getGuardFactor(data.getColony())));
        add(new StaticHappinessModifier(SOCIAL, 2.0, () -> getSocialModifier(data.getColony())));
        add(new StaticHappinessModifier(SATURATION, 1.0, () -> data.getSaturation() / 10.0));

        add(new ExpirationBasedHappinessModifier(DAMAGE, 1.0, () -> 0.0, 1));
        add(new ExpirationBasedHappinessModifier(DEATH, 2.0, () -> 0.0, 3));
        add(new ExpirationBasedHappinessModifier(RAIDWITHOUTDEATH, 1.0, () -> 2.0, 3));
        add(new ExpirationBasedHappinessModifier(SLEPTTONIGHT, 2.0, () -> data.getJob() instanceof AbstractJobGuard ? 1 : 0.0, 3, true));
    }

    /**
     * Create an empty happiness handler for the client side.
     */
    public CitizenHappinessHandler()
    {
        add(new ClientHappinessModifier(HOMELESSNESS, 4.0));
        add(new ClientHappinessModifier(UNEMPLOYMENT, 2.0));
        add(new ClientHappinessModifier(HEALTH, 2.0));
        add(new ClientHappinessModifier(IDLEATJOB, 1.0));

        add(new ClientHappinessModifier(SCHOOL, 1.0));
        add(new ClientHappinessModifier(SECURITY, 2.0));
        add(new ClientHappinessModifier(SOCIAL, 2.0));
        add(new ClientHappinessModifier(SATURATION, 1.0));

        add(new ClientHappinessModifier(DAMAGE, 1.0));
        add(new ClientHappinessModifier(DEATH, 2.0));
        add(new ClientHappinessModifier(RAIDWITHOUTDEATH, 1.0));
        add(new ClientHappinessModifier(SLEPTTONIGHT, 2.0));
    }

    @Override
    public void resetModifier(final String name)
    {
        if (happinessFactors.containsKey(name))
        {
            happinessFactors.get(name).reset();
        }
    }

    @Override


    public IHappinessModifier getModifier(final String name)
    {
        return happinessFactors.get(name);
    }

    @Override
    public void processDailyHappiness(final ICitizenData citizenData)
    {
        for (final IHappinessModifier happinessModifier : happinessFactors.values())
        {
            happinessModifier.dayEnd();
            if (InteractionValidatorRegistry.hasValidator(new TranslationTextComponent(NO + happinessModifier.getId())))
            {
                citizenData.triggerInteraction(new StandardInteractionResponseHandler(new TranslationTextComponent(NO + happinessModifier.getId()), ChatPriority.CHITCHAT));
            }
            if (InteractionValidatorRegistry.hasValidator(new TranslationTextComponent(DEMANDS + happinessModifier.getId())))
            {
                citizenData.triggerInteraction(new StandardInteractionResponseHandler(new TranslationTextComponent(DEMANDS + happinessModifier.getId()), ChatPriority.CHITCHAT));
            }
        }
    }

    @Override
    public double getHappiness()
    {
        double total = 0.0;
        double totalWeight = 0.0;
        for (final IHappinessModifier happinessModifier : happinessFactors.values())
        {
            total += happinessModifier.getFactor() * happinessModifier.getWeight();
            totalWeight += happinessModifier.getWeight();
        }

        return 10.0 * ( total / totalWeight );
    }

    @Override
    public void read(final CompoundNBT compound)
    {
        final CompoundNBT tag = compound.getCompound(TAG_HAPPINESS);
        for (final IHappinessModifier happinessModifier : happinessFactors.values())
        {
            if (tag.contains(happinessModifier.getId()))
            {
                happinessModifier.read(tag.getCompound(happinessModifier.getId()));
            }
        }
    }

    @Override
    public void write(final CompoundNBT compound)
    {
        final CompoundNBT tag = new CompoundNBT();

        for (final IHappinessModifier happinessModifier : happinessFactors.values())
        {
            final CompoundNBT compoundNbt = new CompoundNBT();
            happinessModifier.write(compoundNbt);
            tag.put(happinessModifier.getId(), compoundNbt);
        }

        compound.put(TAG_HAPPINESS, tag);
    }

    @Override
    public List<String> getModifiers()
    {
        return new ArrayList<>(happinessFactors.keySet());
    }

    /**
     * Add the modifier to the handler.
     *
     * @param modifier the modifier.
     */
    private void add(final IHappinessModifier modifier)
    {
        this.happinessFactors.put(modifier.getId(), modifier);
    }

    /**
     * Get the social modifier for the colony.
     *
     * @param colony the colony.
     * @return true if so.
     */
    private double getSocialModifier(final IColony colony)
    {
        final double total = colony.getCitizenManager().getCitizens().size();
        double unemployment = 0;
        double homelessness = 0;
        double sickPeople = 0;
        double hungryPeople = 0;

        for (final ICitizenData citizen : colony.getCitizenManager().getCitizens())
        {
            if (!citizen.isChild() && citizen.getJob() == null)
            {
                unemployment++;
            }

            if (citizen.getHomeBuilding() == null)
            {
                homelessness++;
            }

            if (citizen.getCitizenEntity().isPresent() && citizen.getCitizenEntity().get().getCitizenDiseaseHandler().isSick())
            {
                sickPeople++;
            }

            if (citizen.getSaturation() <= 1)
            {
                hungryPeople++;
            }
        }

        return (total - (unemployment + homelessness + sickPeople + hungryPeople)) / total;
    }

    /**
     * Get the guard security happiness modifier from the colony.
     *
     * @param colony the colony.
     * @return true if so.
     */
    private double getGuardFactor(final IColony colony)
    {
        double guards = 1;
        double workers = 1;
        for (final ICitizenData citizen : colony.getCitizenManager().getCitizens())
        {
            if (citizen.getJob() instanceof AbstractJobGuard)
            {
                guards += citizen.getJobModifier();
            }
            else
            {
                workers += citizen.getJobModifier();
            }
        }
        return guards / workers;
    }
}
