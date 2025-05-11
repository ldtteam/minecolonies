package com.minecolonies.api.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * The effect of research.
 */
public interface IResearchEffect
{
    /**
     * Get the {@link ModResearchEffects.ResearchEffectEntry} for this Research Effect.
     *
     * @return a registry entry.
     */
    ModResearchEffects.ResearchEffectEntry getRegistryEntry();

    /**
     * Getter for the ID of the effect.
     *
     * @return the effect id as a ResourceLocation.
     */
    ResourceLocation getId();

    /**
     * Human-readable effect description, or a translation key.
     *
     * @return the desc.
     */
    TranslatableContents getName();

    /**
     * Human-readable effect subtitle description, or a translation key.
     *
     * @return the Subtitle desc.
     */
    TranslatableContents getSubtitle();

    /**
     * Get the absolute effect of the research.
     *
     * @return the effect.
     */
    double getEffect();

    /**
     * Does this effect override another effect with the same id?
     *
     * @param other the effect to check.
     * @return true if so, generally meaning a higher magnitude effect.
     */
    boolean overrides(@NotNull final IResearchEffect other);

    /**
     * Write the ResearchEffect's traits to NBT, to simplify serialization for client-viewable data.
     *
     * @return an NBT file containing at least the necessary traits to reassemble user-visible traits of the effect.
     */
    CompoundTag writeToNBT();
}
