package com.minecolonies.core.generation;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Wrapper around the vanilla loot table provider which makes it easier to use.
 * Just override getName and registerTables
 */
public abstract class SimpleLootTableProvider extends LootTableProvider
{
    protected SimpleLootTableProvider(@NotNull final PackOutput output,
                                      @NotNull final CompletableFuture<HolderLookup.Provider> provider)
    {
        super(output, new HashSet<>(), new ArrayList<>(), provider);
    }

    /**
     * Create a loot table resource key.
     * @param id the location.
     * @return the resource key.
     */
    public static ResourceKey<LootTable> table(@NotNull final ResourceLocation id)
    {
        return ResourceKey.create(Registries.LOOT_TABLE, id);
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
            if (!stack.isComponentsPatchEmpty())
            {
                for (final Map.Entry<DataComponentType<?>, Optional<?>> entry : stack.getComponentsPatch().entrySet())
                {
                    entry.getValue().ifPresent(setComponent(entry, builder));
                }
            }
            if (stack.getCount() > 1)
            {
                builder.apply(SetItemCountFunction.setCount(ConstantValue.exactly(stack.getCount())));
            }
            return builder;
        }
        return EmptyLootItem.emptyItem();
    }

    @NotNull
    private static <T> Consumer<T> setComponent(Map.Entry<DataComponentType<?>, Optional<?>> entry, LootPoolSingletonContainer.Builder<?> builder)
    {
        // idk if there's a better way to do this generic ... but SetComponentsFunction is Mojank anyway because there's
        // no method to set multiple components at once, short of hacking the constructor directly.
        return value -> builder.apply(SetComponentsFunction.setComponent((DataComponentType<T>) entry.getKey(), value));
    }
}
