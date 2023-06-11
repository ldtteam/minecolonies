package com.minecolonies.coremod.generation;

import com.mojang.datafixers.util.Pair;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Wrapper around the vanilla loot table provider which makes it easier to use.
 * Just override getName and registerTables
 */
public abstract class SimpleLootTableProvider extends LootTableProvider
{
    protected SimpleLootTableProvider(PackOutput output)
    {
        super(output, new HashSet<>(), new ArrayList<>());
    }

    protected abstract void registerTables(@NotNull final LootTableRegistrar registrar);

    @NotNull
    @Override
    public final List<SubProviderEntry> getTables()
    {
        final Map<ResourceLocation, Pair<LootContextParamSet, LootTable.Builder>> tables = new HashMap<>();

        registerTables((id, type, table) -> tables.put(id, Pair.of(type, table)));

        return tables.entrySet().stream()
                .map(w -> new SubProviderEntry(() -> new LootTableSubProvider() {
                    @Override
                    public void generate(final @NotNull BiConsumer<ResourceLocation, LootTable.Builder> builder)
                    {
                        builder.accept(w.getKey(), w.getValue().getSecond());
                    }
                }, w.getValue().getFirst()))
                .collect(Collectors.toList());
    }

    private static Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>
        make(@NotNull final ResourceLocation id,
             @NotNull final LootContextParamSet type,
             @NotNull final LootTable.Builder table)
    {
        return Pair.of(() -> (BiConsumer<ResourceLocation, LootTable.Builder> register) -> register.accept(id, table), type);
    }

    @Override
    protected void validate(@NotNull final Map<ResourceLocation, LootTable> map,
                            @NotNull final ValidationContext validationtracker)
    {
        map.forEach((id, table) -> table.validate(validationtracker));
    }

    @FunctionalInterface
    public interface LootTableRegistrar
    {
        void register(@NotNull ResourceLocation id, @NotNull LootContextParamSet type, @NotNull LootTable.Builder table);
    }

    /**
     * Helper method to make a loot entry builder for an ItemStack
     * @param stack The loot ItemStack
     * @return A loot entry builder for this stack
     */
    public static LootPoolSingletonContainer.Builder<?> itemStack(@NotNull final ItemStack stack)
    {
        if (!stack.isEmpty())
        {
            final LootPoolSingletonContainer.Builder<?> builder = LootItem.lootTableItem(stack.getItem());
            if (stack.hasTag())
            {
                assert stack.getTag() != null;
                builder.apply(SetNbtFunction.setTag(stack.getTag()));
            }
            if (stack.getCount() > 1)
            {
                builder.apply(SetItemCountFunction.setCount(ConstantValue.exactly(stack.getCount())));
            }
            return builder;
        }
        return EmptyLootItem.emptyItem();
    }
}
