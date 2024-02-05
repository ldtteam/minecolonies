package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.research.effects.ModResearchEffects;
import com.minecolonies.api.research.effects.registry.ResearchEffectEntry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.research.GlobalResearchEffect;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.minecolonies.api.research.effects.ModResearchEffects.GLOBAL_EFFECT_ID;

public class ModResearchEffectInitializer
{
    public final static DeferredRegister<ResearchEffectEntry> DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "researcheffecttypes"), Constants.MOD_ID);

    private ModResearchEffectInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModResearchEffectInitializer but this is a Utility class.");
    }

    static
    {
        ModResearchEffects.globalResearchEffect = DEFERRED_REGISTER.register(GLOBAL_EFFECT_ID.getPath(), () -> new ResearchEffectEntry.Builder()
                                                                .setReadFromNBT(GlobalResearchEffect::new)
                                                                .setRegistryName(GLOBAL_EFFECT_ID)
                                                                .createResearchEffectEntry());
    }
}