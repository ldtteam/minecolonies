package com.minecolonies.coremod.colony.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minecolonies.api.util.Log;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Utility helper that analyzes a loot table to determine a likely list of drops, along with
 * their drop rate, for presentation in JEI (and perhaps other purposes).  This is just
 * informational and in particular shouldn't be used to actually generate loot -- due to
 * constraints with how loot tables are stored this can only produce an approximation of a
 * subset of possible loot results.  Good enough to be indicative but that's all.
 * Currently it only supports a very limited set of conditions and properties; just enough
 * for current usage by MineColonies recipes.  If tables are extended with additional
 * conditions or properties then this would have to be adjusted to cope as well.
 */
public final class LootTableAnalyzer
{
    private LootTableAnalyzer() { }

    public static List<LootDrop> toDrops(@NotNull final LootTables lootTableManager,
                                         @NotNull final ResourceLocation lootTableId)
    {
        return toDrops(lootTableManager, lootTableManager.get(lootTableId));
    }

    public static List<LootDrop> toDrops(@Nullable final LootTables lootTableManager,
                                         @NotNull final LootTable lootTable)
    {
        try
        {
            final JsonObject lootTableJson = LootTables.serialize(lootTable).getAsJsonObject();
            return toDrops(lootTableManager, lootTableJson);
        }
        catch (final JsonParseException ex)
        {
            Log.getLogger().error(String.format("Failed to parse loot table from %s",
                    lootTable.getLootTableId()), ex);
            return Collections.emptyList();
        }
    }

    public static List<LootDrop> toDrops(@Nullable final LootTables lootTableManager,
                                         @NotNull final JsonObject lootTableJson)
    {
        final List<LootDrop> drops = new ArrayList<>();

        if (!lootTableJson.has("pools"))
        {
            return drops;
        }

        final JsonArray pools = GsonHelper.getAsJsonArray(lootTableJson, "pools");
        for (final JsonElement pool : pools)
        {
            final JsonArray entries = GsonHelper.getAsJsonArray(pool.getAsJsonObject(), "entries", new JsonArray());
            final float totalWeight = StreamSupport.stream(entries.spliterator(), false)
                    .filter(entry ->
                    {
                        final String type = GsonHelper.getAsString(entry.getAsJsonObject(), "type");
                        return type.equals("minecraft:empty") || type.equals("minecraft:item") || type.equals("minecraft:tag") || type.equals("minecraft:loot_table");
                    })
                    .mapToInt(entry -> GsonHelper.getAsInt(entry.getAsJsonObject(), "weight", 1))
                    .sum();

            for (final JsonElement ej : entries)
            {
                final JsonObject entryJson = ej.getAsJsonObject();
                final String type = GsonHelper.getAsString(entryJson, "type");
                if (type.equals("minecraft:item"))
                {
                    final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(GsonHelper.getAsString(entryJson, "name")));
                    final float weight = GsonHelper.getAsFloat(entryJson, "weight", 1);
                    final float quality = GsonHelper.getAsFloat(entryJson, "quality", 0);
                    final boolean conditional = GsonHelper.getAsJsonArray(entryJson, "conditions", new JsonArray()).size() > 0;
                    ItemStack stack = new ItemStack(item);
                    if (entryJson.has("functions"))
                    {
                        stack = processFunctions(stack, GsonHelper.getAsJsonArray(entryJson, "functions"));
                    }

                    drops.add(new LootDrop(Collections.singletonList(stack), weight / totalWeight, quality, conditional));
                }
                else if (type.equals("minecraft:loot_table") && lootTableManager != null)
                {
                    final ResourceLocation table = new ResourceLocation(GsonHelper.getAsString(entryJson, "name"));
                    final List<LootTableAnalyzer.LootDrop> tableDrops = toDrops(lootTableManager, table);
                    final float weight = GsonHelper.getAsFloat(entryJson, "weight", 1);
                    final float quality = GsonHelper.getAsFloat(entryJson, "quality", 0);
                    final boolean conditional = GsonHelper.getAsJsonArray(entryJson, "conditions", new JsonArray()).size() > 0;
                    for (final LootTableAnalyzer.LootDrop drop : tableDrops)
                    {
                        drops.add(new LootDrop(drop.getItemStacks(), drop.getProbability() * (weight / totalWeight), drop.getQuality() + quality, drop.getConditional() || conditional));
                    }
                }
            }
        }

        drops.sort(Comparator.comparing(LootDrop::getProbability).reversed());
        return drops;
    }

    @NotNull
    public static List<LootDrop> consolidate(@NotNull final List<LootDrop> input)
    {
        return input.stream()
                .collect(Collectors.groupingBy(LootDrop::hashCode))
                .values().stream()
                .map(LootDrop::new)
                .sorted(Comparator.comparing(LootDrop::getProbability).reversed())
                .collect(Collectors.toList());
    }

    private static ItemStack processFunctions(@NotNull ItemStack stack, @NotNull final JsonArray functions)
    {
        for (final JsonElement je : functions)
        {
            final JsonObject function = je.getAsJsonObject();
            final String name = GsonHelper.getAsString(function, "function", "");

            switch (name)
            {
                case "minecraft:set_count":
                    stack.setCount(processNumber(function.get("count"), 1));
                    break;

                case "minecraft:set_damage":
                    if (stack.isDamageableItem())
                    {
                        float damage = 1.0F - processNumber(function.get("damage"), 0F);
                        stack.setDamageValue(Mth.floor(damage * stack.getMaxDamage()));
                    }
                    break;

                case "minecraft:set_nbt":
                    try
                    {
                        stack.setTag(TagParser.parseTag(GsonHelper.getAsString(function, "tag")));
                    }
                    catch (CommandSyntaxException e)
                    {
                        Log.getLogger().error("Failed to parse set_nbt in loot table", e);
                    }
                    break;

                case "minecraft:enchant_with_levels":
                    final int levels = processNumber(function.get("levels"), 1);
                    final boolean treasure = GsonHelper.getAsBoolean(function, "treasure", false);
                    stack = EnchantmentHelper.enchantItem(ThreadLocalRandom.current(), stack, levels, treasure);
                    break;

                default:
                    Log.getLogger().warn("Unhandled modifier in loot table: " + name);
                    break;
            }
        }

        return stack;
    }

    private static int processNumber(@Nullable final JsonElement json, final int defaultValue)
    {
        if (json == null) return defaultValue;

        if (json.isJsonObject())
        {
            switch (GsonHelper.getAsString(json.getAsJsonObject(), "type", ""))
            {
                case "constant":
                    return GsonHelper.getAsInt(json.getAsJsonObject(), "value", defaultValue);
                case "uniform":
                    return GsonHelper.getAsInt(json.getAsJsonObject(), "max", defaultValue);
                default:
                    return defaultValue;
            }
        }
        else if (json.isJsonPrimitive())
        {
            return json.getAsJsonPrimitive().getAsInt();
        }

        return defaultValue;
    }

    private static float processNumber(@Nullable final JsonElement json, final float defaultValue)
    {
        if (json == null) return defaultValue;

        if (json.isJsonObject())
        {
            switch (GsonHelper.getAsString(json.getAsJsonObject(), "type", ""))
            {
                case "constant":
                    return GsonHelper.getAsFloat(json.getAsJsonObject(), "value", defaultValue);
                case "uniform":
                    return GsonHelper.getAsFloat(json.getAsJsonObject(), "max", defaultValue);
                default:
                    return defaultValue;
            }
        }
        else if (json.isJsonPrimitive())
        {
            return json.getAsJsonPrimitive().getAsFloat();
        }

        return defaultValue;
    }

    /**
     * Represents a single possible drop from a loot table.
     */
    public static class LootDrop
    {
        private final List<ItemStack> stacks;
        private final float probability;
        private final float quality;
        private final boolean conditional;

        public LootDrop(@NotNull final List<ItemStack> stacks, final float probability, final float quality, final boolean conditional)
        {
            this.stacks = stacks;
            this.probability = probability;
            this.quality = quality;
            this.conditional = conditional;
        }

        public LootDrop(@NotNull final List<LootDrop> drops)
        {
            this.stacks = drops.stream().flatMap(d -> d.getItemStacks().stream()).collect(Collectors.toList());
            this.probability = drops.get(0).getProbability();
            this.quality = drops.get(0).getQuality();
            this.conditional = drops.get(0).getConditional();
        }

        /** The loot item for this drop (as alternatives). */
        @NotNull public List<ItemStack> getItemStacks() { return this.stacks; }
        /** The approximate probability that this item will drop. */
        public float getProbability() { return this.probability; }
        /** If non-zero, the probability is affected by the citizen's skills (positively or negatively). */
        public float getQuality() { return this.quality; }
        /** If true, there are special conditions on whether this will drop or not. */
        public boolean getConditional() { return this.conditional; }

        /** This should be unique, covering all the properties except for the stacks */
        @Override
        public int hashCode()
        {
            return Objects.hash(probability, quality, conditional);
        }

        /** Copy a LootDrop to a packet buffer */
        public void serialize(@NotNull final FriendlyByteBuf buffer)
        {
            buffer.writeVarInt(stacks.size());
            for (final ItemStack stack : stacks)
            {
                buffer.writeItem(stack);
            }
            buffer.writeFloat(probability);
            buffer.writeFloat(quality);
            buffer.writeBoolean(conditional);
        }

        /** Recover a LootDrop from a packet buffer */
        public static LootDrop deserialize(@NotNull final FriendlyByteBuf buffer)
        {
            final int size = buffer.readVarInt();
            final List<ItemStack> stacks = new ArrayList<>(size);
            for (int i = 0; i < size; ++i)
            {
                stacks.add(buffer.readItem());
            }
            final float probability = buffer.readFloat();
            final float quality = buffer.readFloat();
            final boolean conditional = buffer.readBoolean();
            return new LootDrop(stacks, probability, quality, conditional);
        }
    }
}
