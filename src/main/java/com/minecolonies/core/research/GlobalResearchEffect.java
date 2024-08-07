package com.minecolonies.core.research;

import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.research.effects.ModResearchEffects;
import com.minecolonies.api.research.effects.registry.ResearchEffectEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 *  An instance of a Research Effect at a specific strength, to be applied to a specific colony.
 */
public class GlobalResearchEffect implements IResearchEffect<Double>
{
    /**
     * The NBT tag for an individual effect's identifier, as a ResourceLocation.
     */
    private static final String TAG_ID = "id";

    /**
     * The NBT tag for an individual effect's description, as a human-readable string or TranslationText key.
     */
    private static final String TAG_DESC = "desc";

    /**
     * The NBT tag for an individual effect's subtitle, as a human-readable string or TranslationText key.
     */
    private static final String TAG_SUBTITLE = "subtitle";

    /**
     * The NBT tag for an individual effect's strength, in magnitude.
     */
    private static final String TAG_EFFECT = "effect";

    /**
     * The NBT tag for an individual effect's display value, usually the difference between its strength and the previous level.
     */
    private static final String TAG_DISPLAY_EFFECT = "display";

    /**
     * The absolute effect strength to apply.
     */
    private double effect;

    /**
     * The relative strength of effect to display
     */
    private final double displayEffect;

    /**
     * The unique effect Id.
     */
    private final ResourceLocation id;

    /**
     * The optional text description of the effect. If empty, a translation key will be derived from id.
     */
    private final TranslatableContents desc;

    /**
     * The optional subtitle text description of the effect. If empty, a translation key will be derived from id.
     */
    private final TranslatableContents subtitle;

    /**
     * The constructor to create a new global research effect.
     *
     * @param id            the id to unlock.
     * @param effect        the effect's absolute strength.
     * @param displayEffect the effect's relative strength, for display purposes.
     */
    public GlobalResearchEffect(final ResourceLocation id, final double effect, final double displayEffect)
    {
        this.id = id;
        this.effect = effect;
        this.displayEffect = displayEffect;
        this.desc = new TranslatableContents("com." + this.id.getNamespace() + ".research." + this.id.getPath().replaceAll("[ /:]", ".") + ".description", (String) null,
          List.of(displayEffect, effect, Math.round(displayEffect * 100), Math.round(effect * 100)).toArray());
        this.subtitle = new TranslatableContents("", (String) null, TranslatableContents.NO_ARGS);
    }

    /**
     * The constructor to create a new global research effect, with a statically assigned description.
     *
     * @param id            the id to unlock.
     * @param effect        the effect's absolute strength.
     * @param displayEffect the effect's relative strength, for display purposes.
     * @param desc          the effect's description, for display.
     * @param subtitle      the effect's subtitle description.
     */
    public GlobalResearchEffect(final ResourceLocation id, final double effect, final double displayEffect, final TranslatableContents desc, final TranslatableContents subtitle)
    {
        this.id = id;
        this.effect = effect;
        this.displayEffect = displayEffect;
        final String key = desc.getKey();
        if (key.isEmpty())
        {
            this.desc = new TranslatableContents("com." + this.id.getPath() + ".research." + this.id.getNamespace().replaceAll("[ /:]", ".") + ".description", null,
              List.of(displayEffect, effect, Math.round(displayEffect * 100), Math.round(effect * 100)).toArray());
        }
        else
        {
            this.desc = new TranslatableContents(key, null, List.of(displayEffect, effect, Math.round(displayEffect * 100), Math.round(effect * 100)).toArray());
        }
        this.subtitle = subtitle;
    }

    /**
     * The constructor to build a new global research effect from an NBT.
     *
     * @param nbt the nbt containing the traits for the global research.
     */
    public GlobalResearchEffect(final CompoundTag nbt)
    {
        this.id = ResourceLocation.parse(nbt.getString(TAG_ID));
        this.effect = nbt.getDouble(TAG_EFFECT);
        this.displayEffect = nbt.getDouble(TAG_DISPLAY_EFFECT);
        this.desc = new TranslatableContents(nbt.getString(TAG_DESC),null, List.of(displayEffect, effect, Math.round(displayEffect * 100), Math.round(effect * 100)).toArray());
        this.subtitle = new TranslatableContents(nbt.getString(TAG_SUBTITLE), null, TranslatableContents.NO_ARGS);
    }

    @Override
    public Double getEffect()
    {
        return this.effect;
    }

    @Override
    public void setEffect(Double effect)
    {
        this.effect = effect;
    }

    @Override
    public ResourceLocation getId() { return this.id; }

    @Override
    public TranslatableContents getDesc() { return this.desc; }

    @Override
    public TranslatableContents getSubtitle() { return this.subtitle; }

    @Override
    public boolean overrides(@NotNull final IResearchEffect<?> other)
    {
        return Math.abs(effect) > Math.abs(((GlobalResearchEffect) other).effect);
    }

    @Override
    public ResearchEffectEntry getRegistryEntry() { return ModResearchEffects.globalResearchEffect.get(); }

    @Override
    public CompoundTag writeToNBT()
    {
        final String descKey = desc.getKey();
        final String subtitleKey = subtitle.getKey();

        CompoundTag nbt = new CompoundTag();
        nbt.putString(TAG_ID, id.toString());
        nbt.putString(TAG_DESC, descKey);
        nbt.putString(TAG_SUBTITLE, subtitleKey);
        nbt.putDouble(TAG_EFFECT, effect);
        nbt.putDouble(TAG_DISPLAY_EFFECT, displayEffect);
        return nbt;
    }
}
