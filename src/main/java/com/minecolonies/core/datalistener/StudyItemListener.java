package com.minecolonies.core.datalistener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.util.Log;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads and listens to study items for the {@link com.minecolonies.core.colony.buildings.workerbuildings.BuildingLibrary}.
 */
public class StudyItemListener extends SimpleJsonResourceReloadListener
{
    /**
     * Gson instance
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Json constants
     */
    private static final String KEY_ITEM                  = "item";
    private static final String KEY_SKILL_INCREASE_CHANCE = "skill_increase_chance";
    private static final String KEY_BREAK_CHANCE          = "break_chance";

    /**
     * The current list of study items.
     */
    private static Map<ResourceLocation, StudyItem> ACTIVE_LIST = new HashMap<>();

    /**
     * Container class for any study item.
     *
     * @param item                the item to use.
     * @param skillIncreaseChance chance for skill to increase after using the item.
     * @param breakChance         chance for the item to be used up after using it.
     */
    public record StudyItem(Item item, int skillIncreaseChance, int breakChance)
    {
    }

    /**
     * Default constructor.
     */
    public StudyItemListener()
    {
        super(GSON, "study_items");
    }

    /**
     * Get the current map of study items.
     *
     * @return the map of study items.
     */
    public static Map<ResourceLocation, StudyItem> getAllStudyItems()
    {
        return ACTIVE_LIST;
    }

    /**
     * Check if the given item is a valid study item.
     *
     * @param stack the item stack to check.
     * @return true if so.
     */
    public static boolean isStudyItem(final ItemStack stack)
    {
        return ACTIVE_LIST.containsKey(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    @Override
    protected void apply(
      final @NotNull Map<ResourceLocation, JsonElement> jsonElementMap,
      final @NotNull ResourceManager resourceManager,
      final @NotNull ProfilerFiller profiler)
    {
        final Map<ResourceLocation, StudyItem> newItems = new HashMap<>();
        for (final Map.Entry<ResourceLocation, JsonElement> entry : jsonElementMap.entrySet())
        {
            if (!entry.getValue().isJsonObject())
            {
                return;
            }

            final JsonObject object = entry.getValue().getAsJsonObject();

            final Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(GsonHelper.getAsString(object, KEY_ITEM)));
            final int skillIncreaseChance = percentage(object, KEY_SKILL_INCREASE_CHANCE);
            final int breakChance = percentage(object, KEY_BREAK_CHANCE);

            newItems.put(entry.getKey(), new StudyItem(item, skillIncreaseChance, breakChance));
        }
        ACTIVE_LIST = Collections.unmodifiableMap(newItems);
    }

    /**
     * Parse a json field as a percentage.
     *
     * @param object the input json object.
     * @param field  the field name to parse.
     * @return the percentage.
     */
    private int percentage(final JsonObject object, final String field)
    {
        final int raw = GsonHelper.getAsInt(object, field, 0);
        final int clamped = Mth.clamp(raw, 0, 100);
        if (raw != clamped)
        {
            Log.getLogger()
              .warn(
                "Parsing study item for Library contains a problem. Expected value for {} exceeded the range of [0,100], actual values was {}. Value was automatically clamped to {}.",
                field,
                raw,
                clamped);
        }
        return clamped;
    }
}
