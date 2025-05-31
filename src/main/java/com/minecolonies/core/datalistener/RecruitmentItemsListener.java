package com.minecolonies.core.datalistener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.ColonyConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.core.generation.DataGeneratorConstants.COLONY_RECRUITMENT_ITEMS_DIR;

/**
 * Loads and listens to recruitment costs data.
 */
public class RecruitmentItemsListener extends SimpleJsonResourceReloadListener
{
    /**
     * Gson instance
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Json constants
     */
    private static final String KEY_ITEM   = "item";
    private static final String KEY_RARITY = "rarity";

    /**
     * The current map of recruitment costs.
     */
    private static Map<RecruitmentTiers, List<RecruitCost>> RECRUIT_COSTS = new HashMap<>();

    /**
     * The result for the recruit cost, indicating the stack to recruit with, the level for the citizen and which boots they have to get.
     *
     * @param itemStack    the recruit cost.
     * @param recruitLevel the recruit level.
     * @param boots        the boots they have to wear.
     */
    public record RecruitCostResult(
        ItemStack itemStack,
        int recruitLevel,
        Item boots)
    {}

    /**
     * Default constructor.
     */
    public RecruitmentItemsListener()
    {
        super(GSON, COLONY_RECRUITMENT_ITEMS_DIR);
    }

    /**
     * Get a random recruit cost using the input random source.
     *
     * @param buildingLevel the building level.
     * @return a random recruit cost.
     */
    @Nullable
    public static RecruitCostResult getRandomRecruitCost(final int buildingLevel)
    {
        final int recruitTier = ColonyConstants.rand.nextInt(1, 10 * buildingLevel + 1);
        final Map.Entry<RecruitmentTiers, List<RecruitCost>> tierAndCosts = RECRUIT_COSTS.entrySet()
            .stream()
            .filter(f -> !f.getValue().isEmpty())
            .filter(f -> recruitTier <= f.getKey().maxLevel)
            .min(Comparator.comparingInt(f -> f.getKey().maxLevel))
            .orElse(null);
        if (tierAndCosts == null)
        {
            return null;
        }

        final int recruitLevel = recruitTier + 15;
        final RecruitCost recruitCost = tierAndCosts.getValue().get(ColonyConstants.rand.nextInt(tierAndCosts.getValue().size()));
        return new RecruitCostResult(new ItemStack(recruitCost.item, (int) Math.round(recruitLevel * 3.0d / recruitCost.rarity)), recruitLevel, tierAndCosts.getKey().boots);
    }

    @Override
    protected void apply(final @NotNull Map<ResourceLocation, JsonElement> jsonElementMap, final @NotNull ResourceManager resourceManager, final @NotNull ProfilerFiller profiler)
    {
        final Map<RecruitmentTiers, List<RecruitCost>> recruitCosts = new HashMap<>();

        if (jsonElementMap.isEmpty())
        {
            Log.getLogger().error("No recruitment items found, please ensure to add at least one recruitment item, otherwise visitors will be unable to spawn.");
            return;
        }

        for (final Map.Entry<ResourceLocation, JsonElement> entry : jsonElementMap.entrySet())
        {
            if (!entry.getValue().isJsonObject())
            {
                return;
            }

            final JsonObject object = entry.getValue().getAsJsonObject();
            final Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(GsonHelper.getAsString(object, KEY_ITEM)));
            final int rarity = GsonHelper.getAsInt(object, KEY_RARITY);

            if (item == Items.AIR)
            {
                throw new IllegalArgumentException("Recruit cost '" + entry.getKey() + "' item not allowed to be air");
            }

            for (final RecruitmentTiers tier : RecruitmentTiers.values())
            {
                if (rarity >= tier.minRarity)
                {
                    recruitCosts.putIfAbsent(tier, new ArrayList<>());
                    recruitCosts.get(tier).add(new RecruitCost(item, rarity));
                }
            }
        }

        for (final Map.Entry<RecruitmentTiers, List<RecruitCost>> entry : recruitCosts.entrySet())
        {
            if (entry.getValue().isEmpty())
            {
                Log.getLogger().error("No recruitment items found for tier {}. This tier requires items with a rarity of at least {}.", entry.getKey(), entry.getKey().minRarity);
            }
        }

        RECRUIT_COSTS = Collections.unmodifiableMap(recruitCosts);
    }

    private enum RecruitmentTiers
    {
        LEATHER(5, 0, Items.LEATHER_BOOTS),
        GOLD(10, 2, Items.GOLDEN_BOOTS),
        IRON(20, 4, Items.IRON_BOOTS),
        DIAMOND(30, 6, Items.DIAMOND_BOOTS);

        private final int maxLevel;

        private final int minRarity;

        private final Item boots;

        RecruitmentTiers(final int maxLevel, final int minRarity, final Item boots)
        {
            this.maxLevel = maxLevel;
            this.minRarity = minRarity;
            this.boots = boots;
        }
    }

    /**
     * A possible recruit cost item.
     *
     * @param item   the item to recruit with.
     * @param rarity the rarity of the given item.
     */
    private record RecruitCost(
        Item item,
        int rarity)
    {}
}
