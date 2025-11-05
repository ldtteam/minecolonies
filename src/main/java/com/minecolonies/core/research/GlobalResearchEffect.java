package com.minecolonies.core.research;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.research.IResearchEffect;
import com.minecolonies.api.research.ModResearchEffects;
import com.minecolonies.core.util.GsonHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.minecolonies.core.datalistener.ResearchListener.*;

/**
 * An instance of a Research Effect at a specific strength, to be applied to a specific colony.
 */
public class GlobalResearchEffect implements IResearchEffect
{
    /**
     * Generator functions for default parsed values.
     */
    public static final Function<ResourceLocation, String> DEFAULT_RESEARCH_EFFECT_NAME   =
        (effectId) -> String.format("com.%s.research.%s.description", effectId.getNamespace(), effectId.getPath().replaceAll("[ /]", "."));
    public static final Supplier<JsonArray>                DEFAULT_RESEARCH_EFFECT_LEVELS = () -> {
        final JsonArray defaultArray = new JsonArray();
        defaultArray.add(1);
        return defaultArray;
    };

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
     * The unique effect Id.
     */
    private final ResourceLocation id;

    /**
     * The optional text description of the effect. If empty, a translation key will be derived from id.
     */
    private final TranslatableContents name;

    /**
     * The optional subtitle text description of the effect. If empty, a translation key will be derived from id.
     */
    private final TranslatableContents subtitle;

    /**
     * The absolute effect strength to apply.
     */
    private final double effect;

    /**
     * The relative strength of effect to display
     */
    private final double displayEffect;

    /**
     * The constructor to build a new global research effect from an NBT.
     *
     * @param nbt the nbt containing the traits for the global research.
     */
    public GlobalResearchEffect(final CompoundTag nbt)
    {
        this.id = new ResourceLocation(nbt.getString(TAG_ID));
        this.effect = nbt.getDouble(TAG_EFFECT);
        this.displayEffect = nbt.getDouble(TAG_DISPLAY_EFFECT);
        this.name = new TranslatableContents(nbt.getString(TAG_DESC), null, List.of(displayEffect, effect, Math.round(displayEffect * 100), Math.round(effect * 100)).toArray());
        this.subtitle = new TranslatableContents(nbt.getString(TAG_SUBTITLE), null, TranslatableContents.NO_ARGS);
    }

    /**
     * The constructor to build a new global research effect from json.
     *
     * @param id          the id to unlock.
     * @param effectLevel the level of the effect.
     * @param json        the json data.
     */
    public GlobalResearchEffect(final ResourceLocation id, final int effectLevel, final JsonObject json)
    {
        final String effectName = GsonHelper.getAsString(json, RESEARCH_NAME_PROP, DEFAULT_RESEARCH_EFFECT_NAME, id);
        final String effectSubtitle = GsonHelper.getAsString(json, RESEARCH_SUBTITLE_PROP, "");

        final List<Double> levelsAbsolute = new ArrayList<>(List.of(0d));
        final List<Double> levelsRelative = new ArrayList<>(List.of(0d));
        for (final JsonElement levelElement : GsonHelper.getAsJsonArray(json, EFFECT_LEVELS_PROP, DEFAULT_RESEARCH_EFFECT_LEVELS))
        {
            if (GsonHelper.isNumberValue(levelElement))
            {
                final double level = levelElement.getAsNumber().doubleValue();
                levelsRelative.add(level - levelsAbsolute.get(levelsAbsolute.size() - 1));
                levelsAbsolute.add(level);
            }
        }

        final int targetLevel = Math.max(1, Math.min(levelsAbsolute.size() - 1, effectLevel));

        this.id = id;
        this.effect = levelsAbsolute.get(targetLevel);
        this.displayEffect = levelsRelative.get(targetLevel);
        this.name = new TranslatableContents(effectName, null, List.of(displayEffect, effect, Math.round(displayEffect * 100), Math.round(effect * 100)).toArray());
        this.subtitle = new TranslatableContents(effectSubtitle, null, TranslatableContents.NO_ARGS);
    }

    @Override
    public ModResearchEffects.ResearchEffectEntry getRegistryEntry()
    {
        return ModResearchEffects.globalResearchEffect.get();
    }

    @Override
    public ResourceLocation getId()
    {
        return this.id;
    }

    @Override
    @NotNull
    public Component getName()
    {
        return MutableComponent.create(this.name);
    }

    @Override
    @NotNull
    public Component getSubtitle()
    {
        return MutableComponent.create(this.subtitle);
    }

    @Override
    public double getEffect()
    {
        return this.effect;
    }

    @Override
    public boolean overrides(@NotNull final IResearchEffect other)
    {
        return other instanceof GlobalResearchEffect globalResearchEffect && Math.abs(effect) > Math.abs(globalResearchEffect.effect);
    }

    @Override
    public CompoundTag writeToNBT()
    {
        final CompoundTag nbt = new CompoundTag();
        nbt.putString(TAG_ID, id.toString());
        nbt.putString(TAG_DESC, name.getKey());
        nbt.putString(TAG_SUBTITLE, subtitle.getKey());
        nbt.putDouble(TAG_EFFECT, effect);
        nbt.putDouble(TAG_DISPLAY_EFFECT, displayEffect);
        return nbt;
    }
}
