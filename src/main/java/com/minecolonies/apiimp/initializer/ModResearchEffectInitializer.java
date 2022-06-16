package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.research.effects.ModResearchEffects;
import com.minecolonies.api.research.effects.registry.ResearchEffectEntry;
import com.minecolonies.coremod.research.GlobalResearchEffect;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;

import static com.minecolonies.api.research.effects.ModResearchEffects.GLOBAL_EFFECT_ID;

public class ModResearchEffectInitializer
{
    private ModResearchEffectInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModResearchEffectInitializer but this is a Utility class.");
    }

    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public static void init(final RegisterEvent event)
    {
        final IForgeRegistry<ResearchEffectEntry> reg = event.getForgeRegistry();

        ModResearchEffects.globalResearchEffect = new ResearchEffectEntry.Builder()
                                                                .setReadFromNBT(GlobalResearchEffect::new)
                                                                .setRegistryName(GLOBAL_EFFECT_ID)
                                                                .createResearchEffectEntry();

        reg.register(GLOBAL_EFFECT_ID, ModResearchEffects.globalResearchEffect);
    }
}