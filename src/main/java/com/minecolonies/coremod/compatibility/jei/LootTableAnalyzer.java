package com.minecolonies.coremod.compatibility.jei;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
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
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.packs.ModFileResourcePack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

// This class is essentially an attempt to cope with JEI running client-side but loot tables
// mostly only existing server-side.  (And most of their data structure is private and non-
// iterable, and requires a LootContext, which can also only be created server-side.)  In
// theory another option to manage this whole thing would be to calculate the loot tables
// on the server and then sync the needed info to the client, but I'm not sure where the
// best hooks to do that would be.  (And there's still the problem of iteration.)
//
// Currently it only supports a very limited set of conditions and properties, just enough
// for current usage by MineColonies recipes.  If tables are extended with additional
// conditions or properties then this would have to be adjusted to cope as well.
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

                final SimpleReloadableResourceManager serverResourceManger = new SimpleReloadableResourceManager(ResourcePackType.SERVER_DATA);
                final List<IResourcePack> packs = new LinkedList<>();
                packs.add(new VanillaPack("minecraft"));
                for (final ModFileInfo mod : ModList.get().getModFiles())
                {
                    packs.add(new ModFileResourcePack(mod.getFile()));
                }
                packs.forEach(serverResourceManger::addResourcePack);
                serverResourceManger.addReloadListener(manager);

                final CompletableFuture<Unit> completableFuture = serverResourceManger.reloadResourcesAndThen(Util.getServerExecutor(), Minecraft.getInstance(), packs, CompletableFuture.completedFuture(Unit.INSTANCE));
                Minecraft.getInstance().driveUntil(completableFuture::isDone);
            }
            return manager;
        }
        return world.getServer().getLootTableManager();
    }

    public static JsonObject getLootTableJson(@NotNull final LootTable table)
    {
        return LootTableManager.toJson(table).getAsJsonObject();
    }

    public static JsonObject getLootTableJson(@NotNull final ResourceLocation tableId)
    {
        final LootTable table = getManager(null).getLootTableFromLocation(tableId);
        return getLootTableJson(table);
    }

    public static List<LootDrop> toDrops(@NotNull final JsonObject lootTableJson)
    {
        final List<LootDrop> drops = new ArrayList<>();

        final JsonArray pools = JSONUtils.getJsonArray(lootTableJson, "pools");
        for (final JsonElement pool : pools)
        {
            final JsonArray entries = JSONUtils.getJsonArray(pool.getAsJsonObject(), "entries", new JsonArray());
            final float totalWeight = StreamSupport.stream(entries.spliterator(), false)
                    .filter(entry -> {
                        final String type = JSONUtils.getString(entry.getAsJsonObject(), "type");
                        return type.equals("minecraft:empty") || type.equals("minecraft:item") || type.equals("minecraft:tag") || type.equals("minecraft:loot_table");
                    })
                    .mapToInt(entry -> JSONUtils.getInt(entry.getAsJsonObject(), "weight", 1))
                    .sum();

            for (final JsonElement ej : entries)
            {
                final JsonObject entryJson = ej.getAsJsonObject();
                final String type = JSONUtils.getString(entryJson, "type");
                if (type.equals("minecraft:item"))
                {
                    final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(JSONUtils.getString(entryJson, "name")));
                    final float weight = JSONUtils.getFloat(entryJson, "weight", 1);
                    final boolean variableQuality = JSONUtils.getFloat(entryJson, "quality", 0) != 0;

                    drops.add(new LootDrop(new ItemStack(item), weight / totalWeight, variableQuality));
                }
                else if (type.equals("minecraft:loot_table"))
                {
                    final ResourceLocation table = new ResourceLocation(JSONUtils.getString(entryJson, "name"));
                    drops.addAll(toDrops(getLootTableJson(table)));
                }
            }
        }

        return drops;
    }

/*
    public static List<LootPool> getPools(@NotNull final LootTable table)
    {
        // public net.minecraft.loot.LootTable field_186466_c # pools
        return ObfuscationReflectionHelper.getPrivateValue(LootTable.class, table, "field_186466_c");
    }

    public static List<LootEntry> getLootEntries(@NotNull final LootPool pool)
    {
        // public net.minecraft.loot.LootPool field_186453_a # lootEntries
        return ObfuscationReflectionHelper.getPrivateValue(LootPool.class, pool, "field_186453_a");
    }

    public static List<ILootCondition> getLootConditions(@NotNull final LootPool pool)
    {
        // public net.minecraft.loot.LootPool field_186454_b # conditions
        return ObfuscationReflectionHelper.getPrivateValue(LootPool.class, pool, "field_186454_b");
    }

    public static List<LootDrop> toDrops(@Nullable final World world, @Nullable final LootTable table)
    {
        final List<LootDrop> drops = new ArrayList<>();

        final LootTableManager manager = getManager(world);

        getPools(table).forEach(
                pool -> {
                    final float totalWeight = getLootEntries(pool).stream()
                            .filter(entry -> entry instanceof StandaloneLootEntry).map(entry -> (StandaloneLootEntry) entry)
                            .mapToInt(entry -> entry.weight).sum();
                    final List<ILootCondition> poolConditions = getLootConditions(pool);
                    getLootEntries(pool).stream()
                            .filter(entry -> entry instanceof ItemLootEntry).map(entry -> (ItemLootEntry) entry)
                            .map(entry -> new LootDrop(new ItemStack(entry.item), entry.weight / totalWeight, entry.quality != 0, poolConditions, entry.conditions, entry.functions))
                            .forEach(drops::add);

                    getLootEntries(pool).stream()
                            .filter(entry -> entry instanceof TableLootEntry).map(entry -> (TableLootEntry) entry)
                            .map(entry -> toDrops(world, manager.getLootTableFromLocation(entry.table))).forEach(drops::addAll);
                }
        );

        drops.removeIf(Objects::isNull);
        return drops;
    }

    public static List<LootDrop> toDrops(@Nullable final World world, @Nullable final ResourceLocation lootTable)
    {
        return toDrops(world, getManager(world).getLootTableFromLocation(lootTable));
    }
*/

    public static class LootDrop
    {
        private final ItemStack stack;
        private final float probability;
        private final boolean variableQuality;

        public LootDrop(@NotNull final ItemStack stack,
                        final float probability, final boolean variableQuality)
        {
            this.stack = stack;
            this.probability = probability;
            this.variableQuality = variableQuality;
        }

        @NotNull public ItemStack getItemStack() { return this.stack; }
        public float getProbability() { return this.probability; }
        public boolean getVariableQuality() { return this.variableQuality; }
    }
}
