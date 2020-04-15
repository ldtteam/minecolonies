package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.colony.jobs.JobPupil;
import com.minecolonies.coremod.entity.citizen.happiness.ExpirationBasedHappinessModifier;
import com.minecolonies.coremod.entity.citizen.happiness.IHappinessModifier;
import com.minecolonies.coremod.entity.citizen.happiness.StaticHappinessModifier;
import com.minecolonies.coremod.entity.citizen.happiness.TimeBasedHappinessModifier;

import java.util.HashSet;
import java.util.Set;

import static com.minecolonies.api.util.constant.HappinessConstants.*;

public class NewCitizenHappinessHandler
{
    public Set<IHappinessModifier> happinessFactorSet = new HashSet<>();

    public NewCitizenHappinessHandler(final ICitizenData data)
    {
        happinessFactorSet.add(new TimeBasedHappinessModifier("homelessness", 2.0, () -> data.getHomeBuilding() == null ? 0.5 : 1, new Tuple[]{new Tuple<>(COMPLAIN_DAYS_WITHOUT_HOUSE, 0.75), new Tuple<>(DEMANDS_DAYS_WITHOUT_HOUSE, 0.5)}));
        happinessFactorSet.add(new TimeBasedHappinessModifier("unemployment", 2.0, () -> data.isChild() ? 1.0 : (data.getWorkBuilding() == null ? 0.5 : 1), new Tuple[]{new Tuple<>(COMPLAIN_DAYS_WITHOUT_JOB, 0.75), new Tuple<>(DEMANDS_DAYS_WITHOUT_JOB, 0.5)}));

        happinessFactorSet.add(new ExpirationBasedHappinessModifier("death", 2.0, () -> 0.0, 3));

        happinessFactorSet.add(new ExpirationBasedHappinessModifier("raidWithoutDeath", 1.0, () -> 2.0, 3));

        happinessFactorSet.add(new StaticHappinessModifier("school", 1.0, () -> data.isChild() ? data.getJob() instanceof JobPupil ? 2 : 0 : 1));


        happinessFactorSet.add(new StaticHappinessModifier("security", 1.0, () -> (double) data.getJobModifier()));
        happinessFactorSet.add(new StaticHappinessModifier("social", 1.0, () -> (double) data.getJobModifier()));
        happinessFactorSet.add(new StaticHappinessModifier("health", 1.0, () -> (double) data.getJobModifier()));
        happinessFactorSet.add(new StaticHappinessModifier("damage", 1.0, () -> (double) data.getJobModifier()));
        happinessFactorSet.add(new StaticHappinessModifier("saturation", 1.0, () -> (double) data.getJobModifier()));

        happinessFactorSet.add(new StaticHappinessModifier("sleptBonus", 1.0, () -> (double) data.getJobModifier()));
        happinessFactorSet.add(new StaticHappinessModifier("sleptTonight", 1.0, () -> (double) data.getJobModifier()));

        happinessFactorSet.add(new StaticHappinessModifier("idleAtJob", 1.0, () -> (double) data.getJobModifier()));
    }


}
