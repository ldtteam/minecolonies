package com.minecolonies.api.research.effects;

import com.minecolonies.api.research.effects.registry.ResearchEffectEntry;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.ResourceLocation;

/**
 *  Contains
 */
public class ModResearchEffects
{
    public static final ResourceLocation GLOBAL_EFFECT_ID     = new ResourceLocation(Constants.MOD_ID, "global");

    public static ResearchEffectEntry globalResearchEffect;

    public ModResearchEffects() {throw new IllegalStateException("Tried to initialize: ModResearchEffects, but this is a Utility class.");}
}
