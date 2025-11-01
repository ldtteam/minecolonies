package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.entity.citizen.happiness.ExpirationBasedHappinessModifier;
import com.minecolonies.api.entity.citizen.happiness.HappinessRegistry;
import com.minecolonies.api.entity.citizen.happiness.StaticHappinessModifier;
import com.minecolonies.api.entity.citizen.happiness.TimeBasedHappinessModifier;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.jobs.AbstractJobGuard;
import com.minecolonies.core.colony.jobs.JobPupil;
import com.minecolonies.core.entity.citizen.citizenhandlers.CitizenHappinessHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;

import static com.minecolonies.api.entity.citizen.happiness.HappinessRegistry.*;
import static com.minecolonies.core.entity.citizen.citizenhandlers.CitizenHappinessHandler.*;

/**
 * Happiness factory initializer of the values.
 */
public final class ModHappinessFactorTypeInitializer
{
    public final static DeferredRegister<HappinessFactorTypeEntry> DEFERRED_REGISTER_HAPPINESS_FACTOR = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "happinessfactortypes"), Constants.MOD_ID);
    public final static DeferredRegister<HappinessFunctionEntry> DEFERRED_REGISTER_HAPPINESS_FUNCTION = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "happinessfunction"), Constants.MOD_ID);

    private ModHappinessFactorTypeInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModHappinessFactorTypeInitializer but this is a Utility class.");
    }

    static
    {
        HappinessRegistry.staticHappinessModifier = DEFERRED_REGISTER_HAPPINESS_FACTOR.register(STATIC_MODIFIER.getPath(), () -> new HappinessFactorTypeEntry(StaticHappinessModifier::new));

        HappinessRegistry.expirationBasedHappinessModifier = DEFERRED_REGISTER_HAPPINESS_FACTOR.register(EXPIRATION_MODIFIER.getPath(), () -> new HappinessFactorTypeEntry(ExpirationBasedHappinessModifier::new));

        HappinessRegistry.timeBasedHappinessModifier = DEFERRED_REGISTER_HAPPINESS_FACTOR.register(TIME_PERIOD_MODIFIER.getPath(), () -> new HappinessFactorTypeEntry(TimeBasedHappinessModifier::new));


        HappinessRegistry.schoolFunction = DEFERRED_REGISTER_HAPPINESS_FUNCTION.register(SCHOOL_FUNCTION.getPath(), () -> new HappinessFunctionEntry(data -> data.isChild() ? data.getJob() instanceof JobPupil ? 2.0 : 0.0 : 1.0));
        HappinessRegistry.securityFunction = DEFERRED_REGISTER_HAPPINESS_FUNCTION.register(SECURITY_FUNCTION.getPath(), () -> new HappinessFunctionEntry(data -> getGuardFactor(data.getColony())));
        HappinessRegistry.socialFunction = DEFERRED_REGISTER_HAPPINESS_FUNCTION.register(SOCIAL_FUNCTION.getPath(), () -> new HappinessFunctionEntry(data -> getSocialModifier(data.getColony())));
        HappinessRegistry.mysticalSiteFunction = DEFERRED_REGISTER_HAPPINESS_FUNCTION.register(MYSTICAL_SITE_FUNCTION.getPath(), () -> new HappinessFunctionEntry(data -> getMysticalSiteFactor(data.getColony())));

        HappinessRegistry.housingFunction = DEFERRED_REGISTER_HAPPINESS_FUNCTION.register(HOUSING_FUNCTION.getPath(), () -> new HappinessFunctionEntry(data -> data.getHomeBuilding() == null ? 0.0 : data.getHomeBuilding().getBuildingLevel() / 3.0));
        HappinessRegistry.unemploymentFunction = DEFERRED_REGISTER_HAPPINESS_FUNCTION.register(UNEMPLOYMENT_FUNCTION.getPath(), () -> new HappinessFunctionEntry(data -> data.isChild() ? 1.0 : (data.getWorkBuilding() == null ? 0.5 : data.getWorkBuilding().getBuildingLevel() > 3 ? 2.0 : 1.0)));
        HappinessRegistry.healthFunction = DEFERRED_REGISTER_HAPPINESS_FUNCTION.register(HEALTH_FUNCTION.getPath(),
            () -> new HappinessFunctionEntry(data -> data.getEntity().isPresent() ? (data.getCitizenDiseaseHandler().isSick() ? 0.5 : 1.0) : 1.0));
        HappinessRegistry.idleatjobFunction = DEFERRED_REGISTER_HAPPINESS_FUNCTION.register(IDLEATJOB_FUNCTION.getPath(), () -> new HappinessFunctionEntry(data -> data.isIdleAtJob() ? 0.5 : 1.0));

        HappinessRegistry.sleptTonightFunction = DEFERRED_REGISTER_HAPPINESS_FUNCTION.register(SLEPTTONIGHT_FUNCTION.getPath(), () -> new HappinessFunctionEntry(data -> data.getJob() instanceof AbstractJobGuard ? 1 : 0.5));
        HappinessRegistry.foodFunction = DEFERRED_REGISTER_HAPPINESS_FUNCTION.register(FOOD_FUNCTION.getPath(), () -> new HappinessFunctionEntry(CitizenHappinessHandler::getFoodFactor));
    }
}
