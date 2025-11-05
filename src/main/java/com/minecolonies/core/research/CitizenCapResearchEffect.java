package com.minecolonies.core.research;

import com.google.gson.JsonObject;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.research.ModResearchEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * An instance of a Research Effect at a specific strength, to be applied to a specific colony.
 */
public class CitizenCapResearchEffect extends GlobalResearchEffect
{
    /**
     * The constructor to build a new citizen cap research effect from an NBT.
     *
     * @param nbt the nbt containing the traits for the citizen cap research.
     */
    public CitizenCapResearchEffect(final CompoundTag nbt)
    {
        super(nbt);
    }

    /**
     * The constructor to build a new citizen cap research effect from json.
     *
     * @param id          the id to unlock.
     * @param effectLevel the level of the effect.
     * @param json        the json data.
     */
    public CitizenCapResearchEffect(final ResourceLocation id, final int effectLevel, final JsonObject json)
    {
        super(id, effectLevel, json);
    }

    @Override
    public ModResearchEffects.ResearchEffectEntry getRegistryEntry()
    {
        return ModResearchEffects.citizenCapResearchEffect.get();
    }

    @Override
    @NotNull
    public Component getName()
    {
        final Integer citizenLimit = IMinecoloniesAPI.getInstance().getConfig().getServer().maxCitizenPerColony.get();
        if (getEffect() > citizenLimit)
        {
            final String key = "com.minecolonies.research.effects.citizencapaddition.description";
            final MutableComponent mainText = Component.translatable(key, 0, citizenLimit);
            final MutableComponent finishText = Component.translatable(key + ".over", Math.round(getEffect()));
            return mainText.append(Component.literal(" ")).append(finishText);
        }
        else
        {
            return super.getName();
        }
    }
}
