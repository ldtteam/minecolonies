package com.minecolonies.api.util;

import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.ModTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.common.util.MutableHashedLinkedMap;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Utility class that handles crafting duties
 */
public final class CraftingUtils
{

    private CraftingUtils()
    {
        throw new IllegalStateException("Tried to initialize: CraftingUtils but this is a Utility class.");
    }

    /**
     * Calculate the max time a recipe has to be executed.
     *
     * @param outputStack the output stack.
     * @param storage     the storage.
     * @return the quantity.
     */
    public static int calculateMaxCraftingCount(@NotNull final ItemStack outputStack, @NotNull final IRecipeStorage storage)
    {
        //Calculate the initial crafting count from the request and the storage output.
        int craftingCount = (int) Math.ceil(
          Math.max(ItemStackUtils.getSize(outputStack), ItemStackUtils.getSize(storage.getPrimaryOutput())) / (double) ItemStackUtils.getSize(storage.getPrimaryOutput()));

        //Now check if we excede an ingredients max stack size.
        for (final ItemStorage ingredientStorage : storage.getCleanedInput())
        {
            final ItemStack ingredient = ingredientStorage.getItemStack();
            //Calculate the input count for the ingredient.
            final int ingredientInputCount = ItemStackUtils.getSize(ingredient) * craftingCount;
            //Check if we are above the max stacksize.
            if (ingredientInputCount > ingredient.getMaxStackSize())
            {
                //Recalculate the crafting limit using the maxstacksize of the ingredient.
                craftingCount = Math.max(ingredient.getMaxStackSize(), ItemStackUtils.getSize(storage.getPrimaryOutput())) / ItemStackUtils.getSize(storage.getPrimaryOutput());
            }
        }

        return craftingCount;
    }

    /**
     * Calculate the max time a recipe has to be executed.
     *
     * @param count   the count.
     * @param storage the storage.
     * @return the quantity.
     */
    public static int calculateMaxCraftingCount(final int count, @NotNull final IRecipeStorage storage)
    {
        //Calculate the crafting count from the request and the storage output.
        return (int) Math.ceil(Math.max(count, ItemStackUtils.getSize(storage.getPrimaryOutput())) / (double) ItemStackUtils.getSize(storage.getPrimaryOutput()));
    }

    /**
     * Generates an {@link OptionalPredicate} that reports whether a particular
     * stack is allowed, banned, or undecided when used as a crafting product
     * by the specified job, based on the associated tags.
     *
     * @param crafterJobName The name of the crafting job (defines which tags to check).
     * @return a validator that checks the crafting product tags.
     */
    public static OptionalPredicate<ItemStack> getProductValidatorBasedOnTags(@NotNull final String crafterJobName)
    {
        return stack ->
        {
            // Check against excluded products
            final TagKey<Item> excludedProducts = ModTags.crafterProductExclusions.get(crafterJobName);
            if (excludedProducts != null && stack.is(excludedProducts))
            {
                return Optional.of(false);
            }

            // Check against allowed products
            final TagKey<Item> allowedProducts = ModTags.crafterProduct.get(crafterJobName);
            if (allowedProducts != null && stack.is(allowedProducts))
            {
                return Optional.of(true);
            }

            return Optional.empty();
        };
    }

    /**
     * Generates an {@link OptionalPredicate} that reports whether a particular
     * stack is allowed, banned, or undecided when used as a crafting ingredient
     * by the specified job, based on the associated tags.
     *
     * @param crafterJobName The name of the crafting job (defines which tags to check).
     * @return a validator that checks the crafting ingredient tags.
     */
    public static OptionalPredicate<ItemStack> getIngredientValidatorBasedOnTags(@NotNull final String crafterJobName)
    {
        return getIngredientValidatorBasedOnTags(crafterJobName, false);
    }
    /**
     * Generates an {@link OptionalPredicate} that reports whether a particular
     * stack is allowed, banned, or undecided when used as a crafting ingredient
     * by the specified job, based on the associated tags.
     *
     * @param crafterJobName The name of the crafting job (defines which tags to check).
     * @param includeDoRules True if applying DO overrides
     * @return a validator that checks the crafting ingredient tags.
     */
    public static OptionalPredicate<ItemStack> getIngredientValidatorBasedOnTags(@NotNull final String crafterJobName, boolean includeDoRules)
    {
        return stack ->
        {
            if(includeDoRules)
            {
                final TagKey<Item> includedDoIngredients = ModTags.crafterDoIngredient.get(crafterJobName);
                if (includedDoIngredients != null && stack.is(includedDoIngredients))
                {
                    return Optional.of(true);
                }
            }
            // Check against excluded ingredients
            final TagKey<Item> excludedIngredients = ModTags.crafterIngredientExclusions.get(crafterJobName);
            if (excludedIngredients != null && stack.is(excludedIngredients))
            {
                return Optional.of(false);
            }

            // Check against allowed ingredients
            final TagKey<Item> allowedIngredients = ModTags.crafterIngredient.get(crafterJobName);
            if (allowedIngredients != null && stack.is(allowedIngredients))
            {
                return Optional.of(true);
            }

            return Optional.empty();
        };
    }

    /**
     * Checks the tags associated with the specified job to see whether
     * the provided recipe includes products and/or ingredients that are
     * marked as compatible or incompatible with this job.
     *
     * @param recipe The recipe to check.
     * @param crafterJobName The name of the crafting job (defines which tags to check).
     * @return True if the recipe is compatible, false if not compatible,
     *         or empty if no decision either way.
     */
    public static Optional<Boolean> isRecipeCompatibleBasedOnTags(@NotNull final IGenericRecipe recipe, @NotNull final String crafterJobName)
    {
        return OptionalPredicate.combine(recipe.matchesOutput(getProductValidatorBasedOnTags(crafterJobName)),
                () -> recipe.matchesInput(getIngredientValidatorBasedOnTags(crafterJobName)));
    }

    /**
     * Find all items registered in creative tabs and do something with them.
     *
     * @param displayParams the display parameters.
     * @param consumer      the action to perform.
     */
    public static void forEachCreativeTabItems(@NotNull final CreativeModeTab.ItemDisplayParameters displayParams,
                                               @NotNull final BiConsumer<CreativeModeTab, Collection<ItemStack>> consumer)
    {
        final HolderLookup.RegistryLookup<CreativeModeTab> registry = displayParams.holders().lookup(Registries.CREATIVE_MODE_TAB).get();
        final Map<CreativeModeTab, ResourceKey<CreativeModeTab>> tabKeys = registry.listElements()
                .distinct()     // some mods are dumb
                .collect(Collectors.toMap(Holder::value, Holder.Reference::key));

        for (final CreativeModeTab tab : CreativeModeTabs.allTabs())
        {
            if (tab.getType() == CreativeModeTab.Type.CATEGORY)
            {
                final Collection<ItemStack> stacks;
                if (tab.getDisplayItems().isEmpty())
                {
                    stacks = new HashSet<>();
                    try
                    {
                        onCreativeModeTabBuildContents(tab, Objects.requireNonNull(tabKeys.get(tab), "unregistered tab"),
                                tab.displayItemsGenerator, displayParams, (stack, vis) -> stacks.add(stack));
                    }
                    catch (final Throwable ex)
                    {
                        Log.getLogger().warn("Error populating items for " + tab.getDisplayName().getString(), ex);
                    }
                }
                else
                {
                    stacks = tab.getDisplayItems();
                }

                consumer.accept(tab, stacks);
            }
        }
    }

    /**
     * Extracted from Forge {@link ClientHooks} to avoid classloading problems (since we call on server).
     */
    private static void onCreativeModeTabBuildContents(CreativeModeTab tab, ResourceKey<CreativeModeTab> tabKey, CreativeModeTab.DisplayItemsGenerator originalGenerator, CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output)
    {
        final var entries = new MutableHashedLinkedMap<ItemStack, CreativeModeTab.TabVisibility>(ItemStackLinkedSet.TYPE_AND_TAG,
                (key, left, right) -> {
                    //throw new IllegalStateException("Accidentally adding the same item stack twice " + key.getDisplayName().getString() + " to a Creative Mode Tab: " + tab.getDisplayName().getString());
                    // Vanilla adds enchanting books twice in both visibilities.
                    // This is just code cleanliness for them. For us lets just increase the visibility and merge the entries.
                    return CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
                }
        );

        originalGenerator.accept(params, (stack, vis) -> {
            if (stack.getCount() != 1)
                throw new IllegalArgumentException("The stack count must be 1");
            entries.put(stack, vis);
        });

        ModLoader.get().postEvent(new BuildCreativeModeTabContentsEvent(tab, tabKey, params, entries));

        for (var entry : entries)
            output.accept(entry.getKey(), entry.getValue());
    }
}
