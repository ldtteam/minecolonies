package com.minecolonies.api.crafting;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ldtteam.structurize.items.ModItems.buildTool;

/** Standard implementation of IGenericRecipe.*/
public class GenericRecipe implements IGenericRecipe
{
    @Nullable
    public static IGenericRecipe of(@Nullable final IRecipe<?> recipe)
    {
        if (recipe == null) return null;
        final List<List<ItemStack>> inputs = recipe.getIngredients().stream()
                .map(ingredient -> Arrays.asList(ingredient.getMatchingStacks()))
                .collect(Collectors.toList());
        final int size;
        final Block intermediate;
        if (recipe instanceof FurnaceRecipe)
        {
            size = 1;
            intermediate = Blocks.FURNACE;
        }
        else
        {
            size = recipe.canFit(2, 2) ? 2 : 3;
            intermediate = Blocks.AIR;
        }
        return new GenericRecipe(recipe.getRecipeOutput(), calculateSecondaryOutputs(recipe), inputs,
                size, intermediate, null, new ArrayList<>(), -1);
    }

    @Nullable
    public static IGenericRecipe of(@Nullable final IRecipeStorage storage, @NotNull final List<ITextComponent> restrictions, final int levelSort)
    {
        if (storage == null) return null;
        final List<List<ItemStack>> inputs = storage.getCleanedInput().stream()
                .map(input -> Collections.singletonList(toItemStack(input)))
                .collect(Collectors.toList());
        return new GenericRecipe(storage.getPrimaryOutput(), storage.getAlternateOutputs(),
                storage.getSecondaryOutputs(), inputs, storage.getGridSize(),
                storage.getIntermediate(), storage.getLootTable(), restrictions, levelSort);
    }

    @Nullable
    public static IGenericRecipe of(@Nullable final IRecipeStorage storage)
    {
        return of(storage, new ArrayList<>(), -1);
    }

    @Nullable
    public static IGenericRecipe of(@Nullable final IToken<?> recipeToken)
    {
        if (recipeToken == null) return null;
        return of(IColonyManager.getInstance().getRecipeManager().getRecipes().get(recipeToken));
    }

    private final ItemStack output;
    private final List<ItemStack> allMultiOutputs;
    private final List<ItemStack> additionalOutputs;
    private final List<List<ItemStack>> inputs;
    private final int gridSize;
    private final Block intermediate;
    private final ResourceLocation lootTable;
    private final List<ITextComponent> restrictions;
    private final int levelSort;

    public GenericRecipe(@NotNull final ItemStack output,
                         @NotNull final List<ItemStack> additionalOutputs,
                         @NotNull final List<List<ItemStack>> inputs,
                         final int gridSize, @NotNull final Block intermediate,
                         @Nullable final ResourceLocation lootTable,
                         @NotNull final List<ITextComponent> restrictions,
                         final int levelSort)
    {
        this.output = output;
        this.allMultiOutputs = Collections.singletonList(output);
        this.additionalOutputs = Collections.unmodifiableList(additionalOutputs);
        this.inputs = Collections.unmodifiableList(inputs);
        this.gridSize = gridSize;
        this.intermediate = intermediate;
        this.lootTable = lootTable;
        this.restrictions = Collections.unmodifiableList(restrictions);
        this.levelSort = levelSort;
    }

    public GenericRecipe(@NotNull final ItemStack output,
                         @NotNull final List<ItemStack> altOutputs,
                         @NotNull final List<ItemStack> additionalOutputs,
                         @NotNull final List<List<ItemStack>> inputs,
                         final int gridSize, @NotNull final Block intermediate,
                         @Nullable final ResourceLocation lootTable,
                         @NotNull final List<ITextComponent> restrictions,
                         final int levelSort)
    {
        this.output = output;
        this.allMultiOutputs = Collections.unmodifiableList(
                Stream.concat(Stream.of(output),
                    altOutputs.stream()).collect(Collectors.toList()));
        this.additionalOutputs = Collections.unmodifiableList(additionalOutputs);
        this.inputs = Collections.unmodifiableList(inputs);
        this.gridSize = gridSize;
        this.intermediate = intermediate;
        this.lootTable = lootTable;
        this.restrictions = Collections.unmodifiableList(restrictions);
        this.levelSort = levelSort;
    }

    @Override
    public int getGridSize() { return this.gridSize; }

    @Override
    @NotNull
    public ItemStack getPrimaryOutput()
    {
        return this.output;
    }

    @NotNull
    public List<ItemStack> getAllMultiOutputs()
    {
        return this.allMultiOutputs;
    }

    @NotNull
    @Override
    public List<ItemStack> getAdditionalOutputs()
    {
        return this.additionalOutputs;
    }

    @Override
    @NotNull
    public List<List<ItemStack>> getInputs()
    {
        return this.inputs;
    }

    @NotNull
    @Override
    public List<ITextComponent> getRestrictions()
    {
        return this.restrictions;
    }

    @Override
    public int getLevelSort()
    {
        return this.levelSort;
    }

    @Override
    public boolean matchesOutput(@NotNull final Predicate<ItemStack> predicate)
    {
        return predicate.test(this.output);
    }

    @Override
    public boolean matchesInput(@NotNull final Predicate<ItemStack> predicate)
    {
        for (final List<ItemStack> slot : this.inputs)
        {
            for (final ItemStack stack : slot)
            {
                if (predicate.test(stack))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @NotNull
    @Override
    public Block getIntermediate()
    {
        return this.intermediate;
    }

    @Nullable
    @Override
    public ResourceLocation getLootTable() { return this.lootTable; }

    @Override
    public String toString()
    {
        return "GenericRecipe{output=" + output +'}';
    }

    @NotNull
    private static ItemStack toItemStack(@NotNull final ItemStorage input)
    {
        final ItemStack result = input.getItemStack().copy();
        result.setCount(input.getAmount());
        return result;
    }

    @NotNull
    private static List<ItemStack> calculateSecondaryOutputs(@NotNull final IRecipe<?> recipe)
    {
        if (recipe instanceof ICraftingRecipe)
        {
            final List<Ingredient> inputs = recipe.getIngredients();
            final CraftingInventory inv = new CraftingInventory(new Container(ContainerType.CRAFTING, 0)
            {
                @Override
                public boolean canInteractWith(@NotNull final PlayerEntity playerIn)
                {
                    return false;
                }
            }, 3, 3);
            for (int slot = 0; slot < inputs.size(); ++slot)
            {
                final ItemStack[] stacks = inputs.get(slot).getMatchingStacks();
                if (stacks.length > 0)
                {
                    inv.setInventorySlotContents(slot, stacks[0]);
                }
            }
            if (((ICraftingRecipe) recipe).matches(inv, Minecraft.getInstance().world))
            {
                return ((ICraftingRecipe) recipe).getRemainingItems(inv).stream()
                        .filter(ItemStackUtils::isNotEmpty)
                        .filter(stack -> stack.getItem() != buildTool.get())  // this is filtered out of the inputs too
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}
