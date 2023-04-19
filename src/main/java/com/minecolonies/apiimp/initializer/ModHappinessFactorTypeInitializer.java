package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.entity.citizen.happiness.ExpirationBasedHappinessModifier;
import com.minecolonies.api.entity.citizen.happiness.HappinessFactorTypeRegistry;
import com.minecolonies.api.entity.citizen.happiness.StaticHappinessModifier;
import com.minecolonies.api.entity.citizen.happiness.TimeBasedHappinessModifier;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;

import static com.minecolonies.api.entity.citizen.happiness.HappinessFactorTypeRegistry.*;

/**
 * Happiness factory initializer of the values.
 */
public final class ModHappinessFactorTypeInitializer
{
    public final static DeferredRegister<HappinessFactorTypeRegistry.HappinessFactorTypeTypeEntry> DEFERRED_REGISTER_HAPPINESS_FACTOR = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "happinessfactortypes"), Constants.MOD_ID);

    private ModHappinessFactorTypeInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModHappinessFactorTypeInitializer but this is a Utility class.");
    }

    static
    {
        HappinessFactorTypeRegistry.staticHappinessModifier = DEFERRED_REGISTER_HAPPINESS_FACTOR.register(STATIC_MODIFIER.getPath(), () -> new HappinessFactorTypeTypeEntry(StaticHappinessModifier::new));

        HappinessFactorTypeRegistry.expirationBasedHappinessModifier = DEFERRED_REGISTER_HAPPINESS_FACTOR.register(EXPIRATION_MODIFIER.getPath(), () -> new HappinessFactorTypeTypeEntry(ExpirationBasedHappinessModifier::new));

        HappinessFactorTypeRegistry.timeBasedHappinessModifier = DEFERRED_REGISTER_HAPPINESS_FACTOR.register(TIME_PERIOD_MODIFIER.getPath(), () -> new HappinessFactorTypeTypeEntry(TimeBasedHappinessModifier::new));
    }
}
