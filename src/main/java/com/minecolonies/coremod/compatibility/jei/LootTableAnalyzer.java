package com.minecolonies.coremod.compatibility.jei;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.util.Log;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPredicateManager;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.resources.VanillaPack;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.packs.ModFileResourcePack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This class is essentially an attempt to cope with JEI running client-side but loot tables
 * mostly only existing server-side.  (And most of their data structure is private and non-
 * iterable, and requires a LootContext, which can also only be created server-side.)  In
 * theory another option to manage this whole thing would be to calculate the loot tables
 * on the server and then sync the needed info to the client, but I'm not sure where the
 * best hooks to do that would be -- and we'd either have to be able to guarantee that they
 * all arrive before JEI initialization, or we'd have to block the client thread while waiting
 * for server packets (which is frowned on).  (And there's still the problem of iteration.)
 * Currently it only supports a very limited set of conditions and properties; just enough
 * for current usage by MineColonies recipes.  If tables are extended with additional
 * conditions or properties then this would have to be adjusted to cope as well.
 */
public final class LootTableAnalyzer
{
    private static LootTableManager manager;

    private LootTableAnalyzer() { }

    @NotNull
    public static LootTableManager getManager(@Nullable final World world)
    {
        if (world == null || world.getServer() == null)
        {
            if (manager == null)
            {
                manager = new LootTableManager(new LootPredicateManager());

                // TODO a significant weakness of this approach is that it only loads
                //      the loot tables present in the mod datapacks; it can't see any
                //      modifications made in server or modpack datapacks.
                final SimpleReloadableResourceManager serverResourceManger = new SimpleReloadableResourceManager(ResourcePackType.SERVER_DATA);
                final List<IResourcePack> packs = new LinkedList<>();
                packs.add(new VanillaPack("minecraft"));
                for (final ModFileInfo mod : ModList.get().getModFiles())
                {
                    packs.add(new ModFileResourcePack(mod.getFile()));
                }
                packs.forEach(serverResourceManger::add);
                serverResourceManger.registerReloadListener(manager);

                final CompletableFuture<Unit> completableFuture = serverResourceManger.reload(Util.backgroundExecutor(), Minecraft.getInstance(), packs, CompletableFuture.completedFuture(Unit.INSTANCE));
                Minecraft.getInstance().managedBlock(completableFuture::isDone);
            }
            return manager;
        }
        return world.getServer().getLootTables();
    }

    public static JsonObject getLootTableJson(@NotNull final LootTable table)
    {
        return LootTableManager.serialize(table).getAsJsonObject();
    }

    public static JsonObject getLootTableJson(@NotNull final ResourceLocation tableId)
    {
        final LootTable table = getManager(null).get(tableId);
        return getLootTableJson(table);
    }

    public static List<LootDrop> toDrops(@NotNull final JsonObject lootTableJson)
    {
        final List<LootDrop> drops = new ArrayList<>();

        if (!lootTableJson.has("pools"))
        {
            return drops;
        }

        final JsonArray pools = JSONUtils.getAsJsonArray(lootTableJson, "pools");
        for (final JsonElement pool : pools)
        {
            final JsonArray entries = JSONUtils.getAsJsonArray(pool.getAsJsonObject(), "entries", new JsonArray());
            final float totalWeight = StreamSupport.stream(entries.spliterator(), false)
                    .filter(entry ->
                    {
                        final String type = JSONUtils.getAsString(entry.getAsJsonObject(), "type");
                        return type.equals("minecraft:empty") || type.equals("minecraft:item") || type.equals("minecraft:tag") || type.equals("minecraft:loot_table");
                    })
                    .mapToInt(entry -> JSONUtils.getAsInt(entry.getAsJsonObject(), "weight", 1))
                    .sum();

            for (final JsonElement ej : entries)
            {
                final JsonObject entryJson = ej.getAsJsonObject();
                final String type = JSONUtils.getAsString(entryJson, "type");
                if (type.equals("minecraft:item"))
                {
                    final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(JSONUtils.getAsString(entryJson, "name")));
                    final float weight = JSONUtils.getAsFloat(entryJson, "weight", 1);
                    final boolean variableQuality = JSONUtils.getAsFloat(entryJson, "quality", 0) != 0;
                    CompoundNBT tag = null;
                    final ItemStack stack = new ItemStack(item);
                    if (entryJson.has("functions"))
                    {
                        processFunctions(stack, JSONUtils.getAsJsonArray(entryJson, "functions"));
                    }

                    drops.add(new LootDrop(Collections.singletonList(stack), weight / totalWeight, variableQuality));
                }
                else if (type.equals("minecraft:loot_table"))
                {
                    final ResourceLocation table = new ResourceLocation(JSONUtils.getAsString(entryJson, "name"));
                    drops.addAll(toDrops(getLootTableJson(table)));
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

    private static void processFunctions(@NotNull final ItemStack stack, @NotNull final JsonArray functions)
    {
        for (final JsonElement je : functions)
        {
            final JsonObject function = je.getAsJsonObject();
            switch (JSONUtils.getAsString(function, "function"))
            {
                case "minecraft:set_nbt":
                    try
                    {
                        stack.setTag(JsonToNBT.parseTag(JSONUtils.getAsString(function, "tag")));
                    }
                    catch (CommandSyntaxException e)
                    {
                        Log.getLogger().error("Failed to parse set_nbt in loot table", e);
                    }
                    break;
            }
        }
    }

    /**
     * Represents a single possible drop from a loot table.
     */
    public static class LootDrop
    {
        private final List<ItemStack> stacks;
        private final float probability;
        private final boolean variableQuality;

        public LootDrop(@NotNull final List<ItemStack> stacks, final float probability, final boolean variableQuality)
        {
            this.stacks = stacks;
            this.probability = probability;
            this.variableQuality = variableQuality;
        }

        public LootDrop(@NotNull final List<LootDrop> drops)
        {
            this.stacks = drops.stream().flatMap(d -> d.getItemStacks().stream()).collect(Collectors.toList());
            this.probability = drops.get(0).getProbability();
            this.variableQuality = drops.get(0).getVariableQuality();
        }

        /** The loot item for this drop (as alternatives). */
        @NotNull public List<ItemStack> getItemStacks() { return this.stacks; }
        /** The approximate probability that this item will drop. */
        public float getProbability() { return this.probability; }
        /** If true, the probability is affected by the citizen's skills. */
        public boolean getVariableQuality() { return this.variableQuality; }

        /** This should be unique, covering all the properties except for the stacks */
        @Override
        public int hashCode()
        {
            return Objects.hash(probability, variableQuality);
        }
    }
}
