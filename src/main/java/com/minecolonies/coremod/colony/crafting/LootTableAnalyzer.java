package com.minecolonies.coremod.colony.crafting;

import com.google.gson.*;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.generation.DatagenLootTableManager;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ENTITY_TYPE;

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
    private static final Gson GSON = Deserializers.createLootTableSerializer().create();

    private LootTableAnalyzer() { }

    public static List<LootDrop> toDrops(@NotNull final LootDataManager lootTableManager,
                                         @NotNull final ResourceLocation lootTableId)
    {
        return toDrops(lootTableManager, lootTableManager.getLootTable(lootTableId));
    }

    public static List<LootDrop> toDrops(@Nullable final LootDataManager lootTableManager,
                                         @NotNull final LootTable lootTable)
    {
        try
        {
            final JsonObject lootTableJson = GSON.toJsonTree(lootTable).getAsJsonObject();
            return toDrops(lootTableManager, lootTableJson);
        }
        catch (final JsonParseException ex)
        {
            Log.getLogger().error(String.format("Failed to parse loot table from %s",
                    lootTable.getLootTableId()), ex);
            return Collections.emptyList();
        }
    }

    public static List<LootDrop> toDrops(@Nullable final LootDataManager lootTableManager,
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
                        return type.equals("minecraft:empty") || type.equals("minecraft:item") || type.equals("minecraft:tag") || type.equals("minecraft:loot_table") || type.equals("minecraft:alternatives");
                    })
                    .mapToInt(entry -> GsonHelper.getAsInt(entry.getAsJsonObject(), "weight", 1))
                    .sum();

            for (final JsonElement ej : entries)
            {
                final JsonObject entryJson = ej.getAsJsonObject();
                final float weight = GsonHelper.getAsFloat(entryJson, "weight", 1);
                final List<LootDrop> entryDrops = entryToDrops(lootTableManager, entryJson);
                for (final LootDrop drop : entryDrops)
                {
                    drops.add(new LootDrop(drop.getItemStacks(), drop.getProbability() * (weight / totalWeight), drop.getQuality(), drop.getConditional()));
                }
            }
        }

        drops.sort(Comparator.comparing(LootDrop::getProbability).reversed());
        return drops;
    }

    @NotNull
    private static List<LootDrop> entryToDrops(@Nullable final LootDataManager lootTableManager,
                                               @NotNull final JsonObject entryJson)
    {
        final List<LootDrop> drops = new ArrayList<>();
        final String type = GsonHelper.getAsString(entryJson, "type");
        switch (type)
        {
            case "minecraft:item" -> {
                final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(GsonHelper.getAsString(entryJson, "name")));
                final float quality = GsonHelper.getAsFloat(entryJson, "quality", 0);
                float modifier = 1.0F;
                final boolean conditional = GsonHelper.getAsJsonArray(entryJson, "conditions", new JsonArray()).size() > 0;
                ItemStack stack = new ItemStack(item);
                if (entryJson.has("functions"))
                {
                    final Tuple<ItemStack, Float> result = processFunctions(stack, GsonHelper.getAsJsonArray(entryJson, "functions"));
                    stack = result.getA();
                    modifier = result.getB();
                }
                if (stack.getItem().equals(ModItems.adventureToken))
                {
                    final List<LootDrop> mobDrops = expandAdventureToken(lootTableManager, stack);
                    for (final LootDrop drop : mobDrops)
                    {
                        drops.add(new LootDrop(drop.getItemStacks(), drop.getProbability(), drop.getQuality() + quality, drop.getConditional() || conditional));
                    }
                }
                else
                {
                    drops.add(new LootDrop(Collections.singletonList(stack), modifier, quality, conditional));
                }
            }
            case "minecraft:loot_table" -> {
                final ResourceLocation table = new ResourceLocation(GsonHelper.getAsString(entryJson, "name"));
                final List<LootDrop> tableDrops = toDrops(lootTableManager, table);
                final float quality = GsonHelper.getAsFloat(entryJson, "quality", 0);
                final boolean conditional = GsonHelper.getAsJsonArray(entryJson, "conditions", new JsonArray()).size() > 0;
                for (final LootDrop drop : tableDrops)
                {
                    drops.add(new LootDrop(drop.getItemStacks(), drop.getProbability(), drop.getQuality() + quality, drop.getConditional() || conditional));
                }
            }
            case "minecraft:alternatives" -> {
                final JsonArray children = GsonHelper.getAsJsonArray(entryJson, "children", new JsonArray());
                // currently, the only one of these we're dealing with is "silk touch or not", so we'll just find the
                // first one that doesn't have conditions and call it a day, at least for now... (or failing that, just the last)
                final JsonObject childJson = StreamSupport.stream(children.spliterator(), false)
                        .map(JsonElement::getAsJsonObject)
                        .filter(j -> !j.has("conditions"))
                        .findFirst()
                        .orElse(children.get(children.size() - 1).getAsJsonObject());
                drops.addAll(entryToDrops(lootTableManager, childJson));
            }
        }
        return drops;
    }

    @NotNull
    private static List<LootDrop> expandAdventureToken(@NotNull final LootDataManager lootTableManager,
                                                       @NotNull final ItemStack token)
    {
        if (token.hasTag())
        {
            assert token.getTag() != null;
            final String entityType = token.getTag().getString(TAG_ENTITY_TYPE);
            if (!entityType.isEmpty())
            {
                final EntityType<?> mob = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entityType));
                if (mob != null)
                {
                    return toDrops(lootTableManager, mob.getDefaultLootTable());
                }
            }
        }
        return Collections.emptyList();
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

    private static Tuple<ItemStack, Float> processFunctions(@NotNull ItemStack stack, @NotNull final JsonArray functions)
    {
        float modifier = 1.0F;

        for (final JsonElement je : functions)
        {
            final JsonObject function = je.getAsJsonObject();
            final String name = GsonHelper.getAsString(function, "function", "");

            switch (name)
            {
                case "minecraft:set_count":
                    final Tuple<Integer, Float> result = processCount(function.get("count"));
                    stack.setCount(result.getA());
                    modifier *= result.getB();
                    break;

                case "minecraft:set_damage":
                    if (stack.isDamageableItem())
                    {
                        float damage = 1.0F - processNumber(function.get("damage"), 0F);
                        stack.setDamageValue(Mth.floor(damage * stack.getMaxDamage()));
                    }
                    break;

                case "minecraft:set_potion":
                    final String id = GsonHelper.getAsString(function, "id");
                    final Potion potion = ForgeRegistries.POTIONS.getValue(ResourceLocation.tryParse(id));
                    if (potion != null)
                    {
                        PotionUtils.setPotion(stack, potion);
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
                    stack = EnchantmentHelper.enchantItem(RandomSource.create(), stack, levels, treasure);
                    break;

                case "minecraft:apply_bonus":
                case "minecraft:looting_enchant":
                    // just ignore this for now; we could possibly increase the count a little or
                    // add a tooltip to indicate it's boosted by looting/fortune, but meh.
                    break;

                case "minecraft:furnace_smelt":
                    // this is mostly just to cook the meat if an animal is on fire, which
                    // we can safely ignore.
                    break;

                case "minecraft:explosion_decay":
                    // ignore this; we're not expecting explosions
                    break;

                default:
                    Log.getLogger().warn("Unhandled modifier in loot table: " + name);
                    break;
            }
        }

        return new Tuple<>(stack, modifier);
    }

    private static Tuple<Integer, Float> processCount(@Nullable final JsonElement json)
    {
        if (json == null) return new Tuple<>(1, 1.0F);

        if (json.isJsonObject() && GsonHelper.getAsString(json.getAsJsonObject(), "type", "").equals("minecraft:uniform"))
        {
            final int min = GsonHelper.getAsInt(json.getAsJsonObject(), "min", 0);
            final int max = GsonHelper.getAsInt(json.getAsJsonObject(), "max", 1);

            // the extra wrinkle is to alter the probability when 0 is a possible count.
            return new Tuple<>(max, min == 0 ? max / (max + 1.0F) : 1.0F);
        }

        return new Tuple<>(processNumber(json, 1), 1.0F);
    }

    private static int processNumber(@Nullable final JsonElement json, final int defaultValue)
    {
        if (json == null) return defaultValue;

        if (json.isJsonObject())
        {
            switch (GsonHelper.getAsString(json.getAsJsonObject(), "type", ""))
            {
                case "minecraft:constant":
                    return GsonHelper.getAsInt(json.getAsJsonObject(), "value", defaultValue);
                case "minecraft:uniform":
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
                case "minecraft:constant":
                    return GsonHelper.getAsFloat(json.getAsJsonObject(), "value", defaultValue);
                case "minecraft:uniform":
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
