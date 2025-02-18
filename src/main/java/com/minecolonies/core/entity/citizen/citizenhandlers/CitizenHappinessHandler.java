package com.minecolonies.core.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.interactionhandling.InteractionValidatorRegistry;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenFoodHandler;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenHappinessHandler;
import com.minecolonies.api.entity.citizen.happiness.*;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.core.colony.interactionhandling.StandardInteraction;
import com.minecolonies.core.colony.jobs.AbstractJobGuard;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.entity.citizen.happiness.HappinessRegistry.*;
import static com.minecolonies.api.research.util.ResearchConstants.HAPPINESS;
import static com.minecolonies.api.research.util.ResearchConstants.TAG_ID;
import static com.minecolonies.api.util.constant.HappinessConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.DEMANDS;
import static com.minecolonies.api.util.constant.TranslationConstants.NO;

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
     * The cached happiness value.
     */
    private double cachedHappiness = -1.0;

    /**
     * Create a new instance of the citizen happiness handler.
     *
     * @param data the data to handle.
     */
    public CitizenHappinessHandler(final ICitizenData data)
    {
        // Add static modifiers. These modifiers are on/off.
        addModifier(new StaticHappinessModifier(SCHOOL, 1.0, new DynamicHappinessSupplier(SCHOOL_FUNCTION)));
        addModifier(new StaticHappinessModifier(SECURITY, 4.0, new DynamicHappinessSupplier(SECURITY_FUNCTION)));
        addModifier(new StaticHappinessModifier(SOCIAL, 2.0, new DynamicHappinessSupplier(SOCIAL_FUNCTION)));
        addModifier(new StaticHappinessModifier(MYSTICAL_SITE, 1.0, new DynamicHappinessSupplier(MYSTICAL_SITE_FUNCTION)));
        addModifier(new StaticHappinessModifier(FOOD, 3.0, new DynamicHappinessSupplier(FOOD_FUNCTION)));

        // Add time based modifiers. These modifiers change their value over time.
        addModifier(new TimeBasedHappinessModifier(HOMELESSNESS,
          3.0,
          new DynamicHappinessSupplier(HOUSING_FUNCTION),
         new Tuple<>(COMPLAIN_DAYS_WITHOUT_HOUSE, 0.75), new Tuple<>(DEMANDS_DAYS_WITHOUT_HOUSE, 0.5)));

        addModifier(new TimeBasedHappinessModifier(UNEMPLOYMENT,
          2.0,
          new DynamicHappinessSupplier(UNEMPLOYMENT_FUNCTION),
          new Tuple<>(COMPLAIN_DAYS_WITHOUT_JOB, 0.75), new Tuple<>(DEMANDS_DAYS_WITHOUT_JOB, 0.5)));

        addModifier(new TimeBasedHappinessModifier(HEALTH,
          2.0,
          new DynamicHappinessSupplier(HEALTH_FUNCTION),
          new Tuple<>(COMPLAIN_DAYS_SICK, 0.5), new Tuple<>(DEMANDS_CURE_SICK, 0.1)));

        addModifier(new TimeBasedHappinessModifier(IDLEATJOB,
          1.0,
          new DynamicHappinessSupplier(IDLEATJOB_FUNCTION),
          new Tuple<>(IDLE_AT_JOB_COMPLAINS_DAYS, 0.5), new Tuple<>(IDLE_AT_JOB_DEMANDS_DAYS, 0.1)));

        addModifier(new TimeBasedHappinessModifier(SLEPTTONIGHT, 1.5, new DynamicHappinessSupplier(SLEPTTONIGHT_FUNCTION), (modifier, d) -> true, new Tuple<>(0, 2d), new Tuple<>(2, 1.6d), new Tuple<>(3, 1d)));
    }

    /**
     * Create an empty happiness handler for the client side.
     */
    public CitizenHappinessHandler()
    {
        super();
    }

    @Override
    public void addModifier(final IHappinessModifier modifier)
    {
        this.happinessFactors.put(modifier.getId(), modifier);
        cachedHappiness = -1;
    }

    @Override
    public void resetModifier(final String name)
    {
        final IHappinessModifier modifier = happinessFactors.get(name);
        if (modifier instanceof ITimeBasedHappinessModifier timeBasedHappinessModifier)
        {
            timeBasedHappinessModifier.reset();
            cachedHappiness = -1;
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
            if (happinessModifier instanceof ITimeBasedHappinessModifier timeBasedHappinessModifier)
            {
                timeBasedHappinessModifier.dayEnd(citizenData);
            }
            if (InteractionValidatorRegistry.hasValidator(Component.translatable(NO + happinessModifier.getId())))
            {
                citizenData.triggerInteraction(new StandardInteraction(Component.translatable(NO + happinessModifier.getId()), ChatPriority.CHITCHAT));
            }
            if (InteractionValidatorRegistry.hasValidator(Component.translatable(DEMANDS + happinessModifier.getId())))
            {
                citizenData.triggerInteraction(new StandardInteraction(Component.translatable(DEMANDS + happinessModifier.getId()), ChatPriority.CHITCHAT));
            }
        }
        cachedHappiness = -1;
    }

    @Override
    public double getHappiness(final IColony colony, final ICitizenData citizenData)
    {
        if (cachedHappiness == -1)
        {
            double total = 0.0;
            double totalWeight = 0.0;
            for (final IHappinessModifier happinessModifier : happinessFactors.values())
            {
                final double factor = happinessModifier.getFactor(citizenData);
                if (factor == 1.0)
                    continue;
                total += factor * happinessModifier.getWeight();
                totalWeight += happinessModifier.getWeight();
            }

            final double happinessResult = (total / totalWeight) * (1 + colony.getResearchManager().getResearchEffects().getEffectStrength(HAPPINESS));

            cachedHappiness = Math.min(10.0 * happinessResult, 10);
        }
        return cachedHappiness;
    }

    @Override
    public void read(final CompoundTag compound, final boolean persist)
    {
        // Only deserialize for new version. Old can keep the above defaults just fine.
        if (compound.contains(TAG_NEW_HAPPINESS))
        {
            final ListTag tag = compound.getList(TAG_NEW_HAPPINESS, Tag.TAG_COMPOUND);
            for (int i = 0; i < tag.size(); i++)
            {
                final CompoundTag compoundTag = tag.getCompound(i);
                final String id = compoundTag.getString(TAG_ID);
                if (happinessFactors.containsKey(id))
                {
                    happinessFactors.get(id).read(compoundTag, persist);
                }
                else if (VALID_HAPPINESS_MODIFIERS.contains(id))
                {
                    final IHappinessModifier modifier = HappinessRegistry.loadFrom(compoundTag, persist);
                    if (modifier != null)
                    {
                        happinessFactors.put(modifier.getId(), modifier);
                    }
                }
            }
        }
    }

    @Override
    public void write(final CompoundTag compound, final boolean persist)
    {
        final ListTag listTag = new ListTag();
        for (final IHappinessModifier happinessModifier : happinessFactors.values())
        {
            final CompoundTag compoundNbt = new CompoundTag();
            happinessModifier.write(compoundNbt, persist);
            listTag.add(compoundNbt);
        }

        compound.put(TAG_NEW_HAPPINESS, listTag);
    }

    @Override
    public List<String> getModifiers()
    {
        return new ArrayList<>(happinessFactors.keySet());
    }

    // --------------------------------------------- Static Utility Methods --------------------------------------------- //

    /**
     * Get the social modifier for the colony.
     *
     * @param colony the colony.
     * @return the factor.
     */
    public static double getSocialModifier(final IColony colony)
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

            if (citizen.getEntity().isPresent() && citizen.getCitizenDiseaseHandler().isSick())
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
     * @return the factor.
     */
    public static double getGuardFactor(final IColony colony)
    {
        double guards = 1;
        double workers = 1;
        for (final ICitizenData citizen : colony.getCitizenManager().getCitizens())
        {
            if (citizen.getJob() instanceof AbstractJobGuard)
            {
                guards++;
            }
            else
            {
                workers++;
            }
        }
        return Math.min(guards / (workers * 2 / 3), 2);
    }

    /**
     * Get the food happiness modifier from the citizen.
     * Mininmal Happiness:
     * 1: 1 Different types of food, e.g. baked potato
     * 2: 2 Types of food baked potato + mushroom soup
     * 3: 3 Types food baked potato + mushroom soup + 1 Minecolonies food
     * 4: 4 Types food baked potato + mushroom soup + 2 Minecolonies food
     * 5: 5 Types food baked potato + mushroom soup + 3 Minecolonies food
     * @param citizenData the citizen.
     * @return the factor.
     */
    public static double getFoodFactor(final ICitizenData citizenData)
    {
        final int homeBuildingLevel = citizenData.getHomeBuilding() == null ? 0 : citizenData.getHomeBuilding().getBuildingLevel();
        final ICitizenFoodHandler foodHandler = citizenData.getCitizenFoodHandler();
        if (homeBuildingLevel == 0 || !foodHandler.hasFullFoodHistory())
        {
            return 1.0;
        }

        ICitizenFoodHandler.CitizenFoodStats happinessStats = foodHandler.getFoodHappinessStats();

        final double diversityFactor = Math.min(5.0, (double) happinessStats.diversity() / homeBuildingLevel);
        final double qualityFactor = Math.min(5.0, (double) happinessStats.quality() / (Math.max(1.0, homeBuildingLevel - 2.0)));

        return (diversityFactor + qualityFactor) / 2.0;
    }

    /**
     *  Get the mystical site happiness modifier from the colony.
     *      Mystical site happiness is never negative :
     *      Supply vary from 1 to 3.5 max (1 + (Mystical site lvl 5 / 2))
     *
     * @param colony the colony.
     * @return the factor.
     */
    public static double getMysticalSiteFactor(final IColony colony)
    {
        return Math.max(1, ((double)colony.getBuildingManager().getMysticalSiteMaxBuildingLevel() / 2.0));
    }
}
