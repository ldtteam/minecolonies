package com.minecolonies.api.crafting;

import com.google.common.collect.ImmutableList;
import com.ldtteam.structurize.items.ModItems;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.CraftingUtils;
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

    @NotNull
    private final List<ItemStorage> cleanedInput;

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
    private final IToken<?> token;

    /**
     * Create an instance of the recipe storage.
     *
     * @param token the token of the storage.
     * @param input           the list of input items (required for the recipe).
     * @param gridSize        the required grid size to make it.
     * @param primaryOutput   the primary output of the recipe.
     * @param intermediate    the intermediate to use (e.g furnace).
     */
    public RecipeStorage(final IToken<?> token, final List<ItemStack> input, final int gridSize, @NotNull final ItemStack primaryOutput, final Block intermediate)
    {
        this.input = Collections.unmodifiableList(input);
        this.cleanedInput = this.calculateCleanedInput();
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
    public List<ItemStorage> getCleanedInput()
    {
        return this.cleanedInput;
    }

    public List<ItemStorage> calculateCleanedInput()
    {
        final List<ItemStorage> items = new ArrayList<>();

        for(final ItemStack stack: input)
        {
            if(ItemStackUtils.isEmpty(stack) || stack.getItem() == ModItems.buildTool)
            {
                continue;
            }

            ItemStorage storage = new ItemStorage(stack.copy());
            if(items.contains(storage))
            {
                final int index = items.indexOf(storage);
                final ItemStorage tempStorage = items.remove(index);
                tempStorage.setAmount(tempStorage.getAmount() + storage.getAmount());
                storage = tempStorage;
            }
            items.add(storage);
        }
        return items;
    }

    @NotNull
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
     * @param qty the quantity to craft.
     * @param inventories the inventories to check.
     * @return true if possible, else false.
     */
    @Override
    public boolean canFullFillRecipe(final int qty, @NotNull final IItemHandler... inventories)
    {
        final int neededMultiplier = CraftingUtils.calculateMaxCraftingCount(qty, this);
        final List<ItemStorage> items = getCleanedInput();

        for (final ItemStorage stack : items)
        {
            final int availableCount = InventoryUtils.getItemCountInItemHandlers(
              ImmutableList.copyOf(inventories),
              itemStack -> !ItemStackUtils.isEmpty(itemStack)
                             && itemStack.isItemEqual(stack.getItemStack()));

            if (availableCount < stack.getAmount() * neededMultiplier)
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
        secondaryStacks.add(getPrimaryOutput());
        if(secondaryStacks.size() > getInput().size())
        {
            int freeSpace = 0;
            for (final IItemHandler handler : handlers)
            {
                freeSpace+= handler.getSlots() - InventoryUtils.getAmountOfStacksInItemHandler(handler);
            }

            return freeSpace >= secondaryStacks.size() - getInput().size();
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
        if(!checkForFreeSpace(handlers) || !canFullFillRecipe(1, handlers.toArray(new IItemHandler[0])))
        {
            return false;
        }

        for (final ItemStorage stack : getCleanedInput())
        {
            int amountNeeded = stack.getAmount();

            if (amountNeeded == 0)
            {
                break;
            }

            for (final IItemHandler handler : handlers)
            {
                int slotOfStack = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(handler, itemStack -> !ItemStackUtils.isEmpty(itemStack) && itemStack.isItemEqual(stack.getItemStack()));

                while (slotOfStack != -1 && amountNeeded > 0)
                {
                    final int count = ItemStackUtils.getSize(handler.getStackInSlot(slotOfStack));
                    final ItemStack extractedStack = handler.extractItem(slotOfStack, amountNeeded, false).copy();

                    //This prevents the AI and for that matter the server from getting stuck in case of an emergency.
                    //Deletes some items, but hey.
                    if (ItemStackUtils.isEmpty(extractedStack) || extractedStack.getCount() < amountNeeded)
                    {
                        handler.insertItem(slotOfStack, extractedStack, false);
                        return false;
                    }

                    amountNeeded -= count;
                    if (amountNeeded > 0)
                    {
                        slotOfStack = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(handler,
                          itemStack -> !ItemStackUtils.isEmpty(itemStack) && itemStack.isItemEqual(stack.getItemStack()));
                    }
                }

                // stop looping handlers if we have what we need
                if (amountNeeded <= 0)
                {
                    break;
                }
            }
        }

        insertCraftedItems(handlers);
        return true;
    }

    @Override
    public IToken<?> getToken()
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
            if (stack.getItem() == ModItems.buildTool)
            {
                continue;
            }

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
