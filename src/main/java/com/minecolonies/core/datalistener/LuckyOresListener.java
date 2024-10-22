package com.minecolonies.core.datalistener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.core.util.RandomCollection;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.minecolonies.api.util.constant.Constants.MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.Constants.ONE_HUNDRED_PERCENT;

/**
 * Loads and listens to lucky blocks data.
 */
public class LuckyOresListener extends SimpleJsonResourceReloadListener
{
    /**
     * Gson instance
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Json constants
     */
    private static final String KEY_ITEM           = "item";
    private static final String KEY_RARITY         = "rarity";
    private static final String KEY_BUILDING_LEVEL = "building_level";

    /**
     * The current map of lucky ores.
     */
    private static Map<Integer, RandomCollection<Item>> LUCKY_ORES = new HashMap<>();

    /**
     * Default constructor.
     */
    public LuckyOresListener()
    {
        super(GSON, "lucky_ores");
    }

    /**
     * Get a random lucky ore, if any.
     *
     * @param chance        the chance
     * @param buildingLevel the level of the target building.
     * @param random        the input random source.
     * @return the lucky ore, or null if none exist.
     */
    @Nullable
    public static ItemStack getRandomLuckyOre(final double chance, final int buildingLevel, final @NotNull RandomSource random)
    {
        final RandomCollection<Item> collection = LUCKY_ORES.get(buildingLevel);
        if (collection == null)
        {
            return null;
        }

        if (random.nextDouble() * ONE_HUNDRED_PERCENT <= MinecoloniesAPIProxy.getInstance().getConfig().getServer().luckyBlockChance.get() * chance)
        {
            return collection.next(random).getDefaultInstance();
        }
        return null;
    }

    @Override
    protected void apply(
      final @NotNull Map<ResourceLocation, JsonElement> jsonElementMap,
      final @NotNull ResourceManager resourceManager,
      final @NotNull ProfilerFiller profiler)
    {
        final Map<Integer, RandomCollection<Item>> luckyOres = new HashMap<>();
        for (final Map.Entry<ResourceLocation, JsonElement> entry : jsonElementMap.entrySet())
        {
            if (!entry.getValue().isJsonObject())
            {
                return;
            }

            final JsonObject object = entry.getValue().getAsJsonObject();
            final Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(GsonHelper.getAsString(object, KEY_ITEM)));
            final double rarity = GsonHelper.getAsDouble(object, KEY_RARITY);
            final int minimumBuildingLevel = GsonHelper.getAsInt(object, KEY_BUILDING_LEVEL, 0);

            if (rarity == 0)
            {
                throw new IllegalArgumentException("Recruit cost '" + entry.getKey() + "': rarity must be higher than 0");
            }

            if (item == Items.AIR)
            {
                throw new IllegalArgumentException("Recruit cost '" + entry.getKey() + "': item not allowed to be air");
            }

            for (int i = minimumBuildingLevel; i <= MAX_BUILDING_LEVEL; i++)
            {
                luckyOres.putIfAbsent(i, new RandomCollection<>());
                luckyOres.get(i).add(rarity, item);
            }
        }

        LUCKY_ORES = Collections.unmodifiableMap(luckyOres);
    }
}
