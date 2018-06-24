package com.minecolonies.api.crafting;

import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class used to represent a recipe in minecolonies.
 */
public class RecipeStorage implements IRecipeStorage
{
    /**
     * Input required for the recipe.
     */
    @NotNull
    private final List<ItemStack> input;

    /**
     * Primary output generated for the recipe.
     */
    @NotNull
    private final ItemStack primaryOutput;

    /**
     * The intermediate required for the recipe (e.g furnace).
     */
    private final Block intermediate;

    /**
     * Grid size required for the recipe.
     */
    private final int gridSize;

    /**
     * The token of the RecipeStorage.
     */
    private final IToken token;

    /**
     * Create an instance of the recipe storage.
     *
     * @param token the token of the storage.
     * @param input           the list of input items (required for the recipe).
     * @param gridSize        the required grid size to make it.
     * @param primaryOutput   the primary output of the recipe.
     * @param intermediate    the intermediate to use (e.g furnace).
     */
    public RecipeStorage(final IToken token, final List<ItemStack> input, final int gridSize, final ItemStack primaryOutput, final Block intermediate)
    {
        this.input = Collections.unmodifiableList(input);
        this.primaryOutput = primaryOutput;
        this.gridSize = gridSize;
        this.intermediate = intermediate;
        this.token = token;
    }

    @Override
    public List<ItemStack> getInput()
    {
        return new ArrayList<>(input);
    }

    @Override
    public ItemStack getPrimaryOutput()
    {
        return primaryOutput;
    }

    @Override
    public int getGridSize()
    {
        return gridSize;
    }

    @Override
    public Block getIntermediate()
    {
        return this.intermediate;
    }

    /**
     * Method to check if with the help of inventories this recipe can be fullfilled.
     *
     * @param inventories the inventories to check.
     * @return true if possible, else false.
     */
    @Override
    public boolean canFullFillRecipe(@NotNull final IItemHandler... inventories)
    {
        final List<ItemStorage> items = new ArrayList<>();

        for(final ItemStack stack: input)
        {
            ItemStorage storage = new ItemStorage(stack);
            if(items.contains(storage))
            {
                final int index = items.indexOf(storage);
                final ItemStorage tempStorage = items.remove(index);
                tempStorage.setAmount(tempStorage.getAmount() + storage.getAmount());
                storage = tempStorage;
            }
            items.add(storage);
        }

        for (final ItemStorage stack : items)
        {
            int amountNeeded = stack.getAmount();
            boolean hasStack = false;
            for (final IItemHandler handler : inventories)
            {
                hasStack = InventoryUtils.hasItemInItemHandler(handler, itemStack -> !ItemStackUtils.isEmpty(itemStack) && itemStack.isItemEqual(stack.getItemStack()));

                if (hasStack)
                {
                    final int count = InventoryUtils.getItemCountInItemHandler(handler, itemStack -> !ItemStackUtils.isEmpty(itemStack) && itemStack.isItemEqual(stack.getItemStack()));
                    if (count >= amountNeeded)
                    {
                        break;
                    }
                    hasStack = false;
                    amountNeeded -= count;
                }
            }

            if (!hasStack)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof RecipeStorage))
        {
            return false;
        }

        final RecipeStorage that = (RecipeStorage) o;

        if (gridSize != that.gridSize
                || input.size() != that.input.size()
                || !primaryOutput.isItemEqualIgnoreDurability(that.primaryOutput))
        {
            return false;
        }

        for(int i = 0; i < input.size(); i++)
        {
            if(!that.input.get(i).isItemEqual(input.get(i)))
            {
                return false;
            }
        }

        return intermediate == null ? (that.intermediate == null) : intermediate.equals(that.intermediate);
    }

    @Override
    public int hashCode()
    {
        int result = input.hashCode();
        result = 31 * result + primaryOutput.hashCode();
        result = 31 * result + (intermediate != null ? intermediate.hashCode() : 0);
        result = 31 * result + gridSize;
        return result;
    }

    /**
     * Check for free space in the handlers.
     * @param handlers the handlers to check.
     * @return true if enough space.
     */
    private boolean checkForFreeSpace(final List<IItemHandler> handlers)
    {
        final List<ItemStack> secondaryStacks = new ArrayList<>();
        for(final ItemStack stack: input)
        {
            final ItemStack container = stack.getItem().getContainerItem(stack);
            if (!ItemStackUtils.isEmpty(container))
            {
                secondaryStacks.add(container);
            }
        }
        if(secondaryStacks.size() > getInput().size())
        {
            int freeSpace = 0;
            for (final IItemHandler handler : handlers)
            {
                freeSpace+= handler.getSlots() - InventoryUtils.getAmountOfStacksInItemHandler(handler);
            }

            if(freeSpace < secondaryStacks.size() - getInput().size())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Check for space, remove items, and insert crafted items.
     * @param handlers the handlers to use.
     * @return true if succesful.
     */
    @Override
    public boolean fullfillRecipe(final List<IItemHandler> handlers)
    {
        if(!checkForFreeSpace(handlers) || !canFullFillRecipe(handlers.toArray(new IItemHandler[handlers.size()])))
        {
            return false;
        }

        for (final ItemStack stack : input)
        {
            int amountNeeded = ItemStackUtils.getSize(stack);
            for (final IItemHandler handler : handlers)
            {
                final int slotOfStack = InventoryUtils.
                        findFirstSlotInItemHandlerNotEmptyWith(handler, itemStack -> !ItemStackUtils.isEmpty(itemStack) && itemStack.isItemEqual(stack));

                while (slotOfStack != -1)
                {
                    final int count = ItemStackUtils.getSize(handler.getStackInSlot(slotOfStack));
                    handler.extractItem(slotOfStack, amountNeeded, false);

                    if (count >= amountNeeded)
                    {
                        break;
                    }
                    amountNeeded -= count;
                }
            }
        }

        insertCraftedItems(handlers);
        return true;
    }

    @Override
    public IToken getToken()
    {
        return token;
    }

    /**
     * Inserted the resulting items into the itemHandlers.
     * @param handlers the handlers.
     */
    private void insertCraftedItems(final List<IItemHandler> handlers)
    {
        for (final IItemHandler handler : handlers)
        {
            if (InventoryUtils.addItemStackToItemHandler(handler, getPrimaryOutput().copy()))
            {
                break;
            }
        }

        final List<ItemStack> secondaryStacks = new ArrayList<>();
        for(final ItemStack stack: input)
        {
            final ItemStack container = stack.getItem().getContainerItem(stack);
            if (!ItemStackUtils.isEmpty(container))
            {
                secondaryStacks.add(container);
            }
        }
        for (final ItemStack stack : secondaryStacks)
        {
            for (final IItemHandler handler : handlers)
            {
                if (InventoryUtils.addItemStackToItemHandler(handler, stack.copy()))
                {
                    break;
                }
            }
        }
    }
}
