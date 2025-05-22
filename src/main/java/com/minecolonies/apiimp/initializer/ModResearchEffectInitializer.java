package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.research.ModResearchEffects;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.apiimp.CommonMinecoloniesAPIImpl;
import com.minecolonies.core.research.GlobalResearchEffect;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.minecolonies.api.research.ModResearchEffects.*;

/**
 * Registry initializer for the {@link ModResearchEffects}.
 */
public class ModResearchEffectInitializer
{
    public final static DeferredRegister<ResearchEffectEntry> DEFERRED_REGISTER =
        DeferredRegister.create(CommonMinecoloniesAPIImpl.RESEARCH_EFFECT_TYPES, Constants.MOD_ID);
    static
    {
        globalResearchEffect = create(GLOBAL_EFFECT_ID, GlobalResearchEffect::new);
    }
    private ModResearchEffectInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModResearchEffectInitializer but this is a Utility class.");
    }

    /**
     * Utility method to aid in the creation of a research effect.
     *
     * @param registryName the registry name for this entry.
     * @param readFromNBT  function to read this item from json.
     * @return the finalized registry object.
     */
    private static DeferredHolder<ResearchEffectEntry, ResearchEffectEntry> create(
        final ResourceLocation registryName,
        final ReadFromNBTFunction readFromNBT)
    {
        return DEFERRED_REGISTER.register(registryName.getPath(), () -> new ResearchEffectEntry(registryName, readFromNBT));
    }
}
