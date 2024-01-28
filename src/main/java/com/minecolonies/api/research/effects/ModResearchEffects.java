package com.minecolonies.api.research.effects;

import com.minecolonies.api.research.effects.registry.ResearchEffectEntry;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

/**
 *  Contains a list of research effects by type.  Currently only supports absolute modifiers through Global Research Effect.
 */
public class ModResearchEffects
{
    public static final ResourceLocation GLOBAL_EFFECT_ID     = new ResourceLocation(Constants.MOD_ID, "global");

    public static RegistryObject<ResearchEffectEntry> globalResearchEffect;

    public ModResearchEffects() {throw new IllegalStateException("Tried to initialize: ModResearchEffects, but this is a Utility class.");}
}
