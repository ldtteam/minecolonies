package com.minecolonies.core.datalistener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.minecolonies.core.colony.buildings.modules.TavernBuildingModule.DIAMOND_SKILL_LEVEL;
import static com.minecolonies.core.colony.buildings.modules.TavernBuildingModule.IRON_SKILL_LEVEL;

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
     * The current list of recruitment costs.
     */
    private static List<RecruitCost> RECRUIT_COSTS = new ArrayList<>();

    /**
     * A possible recruit cost item.
     *
     * @param item   the item to recruit with.
     * @param rarity the rarity of the given item.
     */
    public record RecruitCost(Item item, int rarity)
    {
        /**
         * Generate an itemstack for the given recruit cost.
         *
         * @param level the recruitment level.
         * @return the newly generated itemstack.
         */
        public ItemStack toItemStack(final int level)
        {
            return new ItemStack(item, (int) Math.round(level * 3.0 / rarity));
        }
    }

    /**
     * Default constructor.
     */
    public RecruitmentItemsListener()
    {
        super(GSON, "recruitment_items");
    }

    /**
     * Get a random recruit cost using the input random source.
     *
     * @param source the random source.
     * @param level  the recruitment level.
     * @return a random recruit cost.
     */
    public static RecruitCost getRandomRecruitCost(final RandomSource source, final int level)
    {
        int minimumRarity;
        if (level > DIAMOND_SKILL_LEVEL)
        {
            minimumRarity = 3;
        }
        else if (level > IRON_SKILL_LEVEL)
        {
            minimumRarity = 2;
        }
        else
        {
            minimumRarity = 0;
        }
        final List<RecruitCost> recruitCostStream = RECRUIT_COSTS.stream().filter(f -> f.rarity <= minimumRarity).toList();
        return recruitCostStream.get(source.nextInt(recruitCostStream.size()));
    }

    @Override
    protected void apply(
      final @NotNull Map<ResourceLocation, JsonElement> jsonElementMap,
      final @NotNull ResourceManager resourceManager,
      final @NotNull ProfilerFiller profiler)
    {
        final List<RecruitCost> recruitCosts = new ArrayList<>();
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

            recruitCosts.add(new RecruitCost(item, rarity));
        }

        RECRUIT_COSTS = Collections.unmodifiableList(recruitCosts);
    }
}
