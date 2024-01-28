package com.minecolonies.api.research.effects;

import com.minecolonies.api.research.effects.registry.ResearchEffectEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * The effect of a research.
 *
 * @param <T> the type of the value.
 */
public interface IResearchEffect<T>
{
    /**
     * Get the absolute effect of the research.
     *
     * @return the effect.
     */
    T getEffect();

    /**
     * Set the research effect.
     *
     * @param effect the value to set it to.
     */
    void setEffect(T effect);

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
    TranslatableContents getDesc();

    /**
     * Human-readable effect subtitle description, or a translation key.
     *
     * @return the Subtitle desc.
     */
    TranslatableContents getSubtitle();

    /**
     * Does this effect override another effect with the same id?
     *
     * @param other the effect to check.
     * @return true if so, generally meaning a higher magnitude effect.
     */
    boolean overrides(@NotNull final IResearchEffect<?> other);

    /**
     * Get the {@link ResearchEffectEntry} for this Research Effect.
     *
     * @return a registry entry.
     */
    ResearchEffectEntry getRegistryEntry();

    /**
     * Write the ResearchEffect's traits to NBT, to simplify serialization for client-viewable data.
     * @return an NBT file containing at least the necessary traits to reassemble user-visible traits of the effect.
     */
    CompoundTag writeToNBT();
}
