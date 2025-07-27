package com.minecolonies.core.datalistener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.MathUtils;
import com.minecolonies.api.util.constant.ColonyConstants;
import com.minecolonies.api.util.constant.Constants;
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
     * Base recruitment level.
     */
    public static int BASE_RECRUIT_LEVEL = 15;

    /**
     * Max rarity.
     */
    public static int MAX_RARITY = 9;

    /**
     * Base item count. Per level of rarity it's that much less.
     */
    public static int BASE_ITEM_COUNT = 5;

    /**
     * Gson instance
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Json constants
     */
    public static final String KEY_ITEM   = "item";
    public static final String KEY_RARITY = "rarity";

    /**
     * The current map of recruitment costs.
     */
    private static Map<Integer, List<RecruitCost>> RECRUIT_COSTS = new HashMap<>();

    /**
     *  Map of recruitLevel to boot tier.
     */
    private static final Map<Integer, ItemStack> RARITY_TO_BOOT_MAP = Map.ofEntries(
        Map.entry(1, new ItemStack(Items.LEATHER_BOOTS)),
        Map.entry(2, new ItemStack(Items.LEATHER_BOOTS)),
        Map.entry(3, new ItemStack(Items.GOLDEN_BOOTS)),
        Map.entry(4, new ItemStack(Items.GOLDEN_BOOTS)),
        Map.entry(5, new ItemStack(Items.IRON_BOOTS)),
        Map.entry(6, new ItemStack(Items.IRON_BOOTS)),
        Map.entry(7, new ItemStack(Items.DIAMOND_BOOTS)),
        Map.entry(8, new ItemStack(Items.DIAMOND_BOOTS)),
        Map.entry(9, new ItemStack(Items.NETHERITE_BOOTS))
    );

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
    public static RecruitCost getRandomRecruitCost(final int buildingLevel)
    {
        // Number between 1-9
        final int limit = 3 * buildingLevel + 1;
        int rarity = (int) MathUtils.RANDOM.nextGaussian(limit/2.0,0.5);
        if (rarity < 0)
        {
            rarity = 1;
        }
        else if (rarity >= limit)
        {
            rarity = limit - 1;
        }
        final List<RecruitCost> recruitCostsAtTier = RECRUIT_COSTS.get(rarity);
        return recruitCostsAtTier.get(ColonyConstants.rand.nextInt(recruitCostsAtTier.size()));
    }

    @Override
    protected void apply(final @NotNull Map<ResourceLocation, JsonElement> jsonElementMap, final @NotNull ResourceManager resourceManager, final @NotNull ProfilerFiller profiler)
    {
        final Map<Integer, List<RecruitCost>> recruitCosts = new HashMap<>();

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
                Log.getLogger().error("Recruit cost '" + entry.getKey() + "' item not allowed to be air");
                continue;
            }

            if (rarity > MAX_RARITY || rarity < 1)
            {
                Log.getLogger().error("Recruit cost with invalid recruitLevel {} needs to be between 1-9", rarity);
                continue;
            }

            //todo this calc results in the low levels being correct, and the high levels all around the 15 base recruit + random, this doesn't work.
            final int count = BASE_ITEM_COUNT * (MAX_RARITY + 1 - rarity);
            final int recruitLevel = BASE_RECRUIT_LEVEL + rarity * rarity / 2;
            recruitCosts.putIfAbsent(rarity, new ArrayList<>());
            recruitCosts.get(rarity).add(new RecruitCost(new ItemStack(item, count), recruitLevel, RARITY_TO_BOOT_MAP.get(rarity)));
        }

        for (int i = 1; i <= MAX_RARITY; i++)
        {
            if (recruitCosts.getOrDefault(i, new ArrayList<>()).isEmpty())
            {
                Log.getLogger().error("No recruitment items found for rarity of {}.", i);
            }
        }

        RECRUIT_COSTS = Collections.unmodifiableMap(recruitCosts);
    }

    /**
     * A recruit cost item.
     * @param boots the boots the visitor will wear.
     * @param recruitItem   the item to recruit with.
     * @param recruitLevel the recruitLevel of the given item.
     */
    public record RecruitCost(
        ItemStack recruitItem,
        int recruitLevel,
        ItemStack boots)
    {}
}
