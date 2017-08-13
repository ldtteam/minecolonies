package com.minecolonies.coremod.entity.ai.util;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Class used to represent a recipe in minecolonies.
 */
public class RecipeStorage
{
    /**
     * Compound tag for the grid size.
     */
    private static final String TAG_GRID = "grid";

    /**
     * Compound tag for the secondary output.
     */
    private static final String SECONDARY_OUTPUT_TAG = "secondaryoutput";

    /**
     * Compound tag for the input.
     */
    private static final String INPUT_TAG = "input";

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
     * Secondary output generated for the recipe.
     */
    @NotNull
    private final List<ItemStack> secondaryOutput;

    /**
     * The intermediate required for the recipe (e.g furnace).
     */
    private final Block intermediate;

    /**
     * Grid size required for the recipe.
     */
    private final int gridSize;

    /**
     * Create an instance of the recipe storage.
     *
     * @param input           the list of input items (required for the recipe).
     * @param gridSize        the required grid size to make it.
     * @param primaryOutput   the primary output of the recipe.
     * @param secondaryOutput the secondary output (like buckets or similar).
     */
    public RecipeStorage(final List<ItemStack> input, final int gridSize, final ItemStack primaryOutput, final ItemStack... secondaryOutput)
    {
        this.input = Collections.unmodifiableList(input);
        this.primaryOutput = primaryOutput;
        this.secondaryOutput = Collections.unmodifiableList(Arrays.asList(secondaryOutput));
        this.gridSize = gridSize;
        this.intermediate = null;
    }

    /**
     * Create an instance of the recipe storage.
     *
     * @param input           the list of input items (required for the recipe).
     * @param gridSize        the required grid size to make it.
     * @param primaryOutput   the primary output of the recipe.
     * @param intermediate    the intermediate to use (e.g furnace).
     * @param secondaryOutput the secondary output (like buckets or similar).
     */
    public RecipeStorage(final List<ItemStack> input, final int gridSize, final ItemStack primaryOutput, final Block intermediate, final ItemStack... secondaryOutput)
    {
        this.input = Collections.unmodifiableList(input);
        this.primaryOutput = primaryOutput;
        this.secondaryOutput = Collections.unmodifiableList(Arrays.asList(secondaryOutput));
        this.gridSize = gridSize;
        this.intermediate = intermediate;
    }

    /**
     * Get the list of input items.
     * Suppressing Sonar Rule Squid:S2384
     * The rule thinks we should return a copy of the list and not the list itself.
     * But in this case the rule does not apply because the list is an unmodifiable list already
     *
     * @return the list.
     */
    @SuppressWarnings("squid:S2384")
    public List<ItemStack> getInput()
    {
        return input;
    }

    /**
     * Get the list of output items.
     *
     * @return the copy of the list.
     */
    @SuppressWarnings("squid:S2384")
    public List<ItemStack> getSecondaryOutput()
    {
        return secondaryOutput;
    }

    /**
     * Getter for the primary output.
     *
     * @return the itemStack to be produced.
     */
    public ItemStack getPrimaryOutput()
    {
        return primaryOutput;
    }

    /**
     * Get the grid size.
     *
     * @return the integer representing it. (2x2 = 4, 3x3 = 9, etc)
     */
    public int getGridSize()
    {
        return gridSize;
    }

    /**
     * Get the required intermediate for the recipe.
     *
     * @return the block.
     */
    public Block getIntermediate()
    {
        return this.intermediate;
    }

    public static RecipeStorage readFromNBT(@NotNull final NBTTagCompound compound)
    {
        final List<ItemStack> input = new ArrayList<>();
        final NBTTagList inputTagList = compound.getTagList(INPUT_TAG, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < inputTagList.tagCount(); ++i)
        {
            final NBTTagCompound inputTag = inputTagList.getCompoundTagAt(i);
            input.add(new ItemStack(inputTag));
        }

        final ItemStack primaryOutput = new ItemStack(compound);

        final List<ItemStack> secondaryOutput = new ArrayList<>();
        final NBTTagList neededResTagList = compound.getTagList(INPUT_TAG, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < neededResTagList.tagCount(); ++i)
        {
            final NBTTagCompound neededRes = neededResTagList.getCompoundTagAt(i);
            secondaryOutput.add(new ItemStack(neededRes));
        }

        final Block intermediate = NBTUtil.readBlockState(compound).getBlock();
        final int gridSize = compound.getInteger(TAG_GRID);

        return new RecipeStorage(input, gridSize, primaryOutput, intermediate, secondaryOutput.toArray(new ItemStack[secondaryOutput.size()]));
    }

    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        @NotNull final NBTTagList inputTagList = new NBTTagList();
        for (@NotNull final ItemStack stack : input)
        {
            @NotNull final NBTTagCompound neededRes = new NBTTagCompound();
            stack.writeToNBT(neededRes);
            inputTagList.appendTag(neededRes);
        }
        compound.setTag(INPUT_TAG, inputTagList);


        primaryOutput.writeToNBT(compound);

        @NotNull final NBTTagList secondaryOutputTAGList = new NBTTagList();
        for (@NotNull final ItemStack stack : secondaryOutput)
        {
            @NotNull final NBTTagCompound neededRes = new NBTTagCompound();
            stack.writeToNBT(neededRes);
            secondaryOutputTAGList.appendTag(neededRes);
        }
        compound.setTag(SECONDARY_OUTPUT_TAG, secondaryOutputTAGList);

        if(intermediate != null)
        {
            NBTUtil.writeBlockState(compound, intermediate.getDefaultState());
        }

        compound.setInteger(TAG_GRID, gridSize);
    }

    /**
     * Method to check if with the help of inventories this recipe can be fullfilled.
     *
     * @param inventories the inventories to check.
     * @return true if possible, else false.
     */
    public boolean canFullFillRecipe(@NotNull final IItemHandler... inventories)
    {
        for (final ItemStack stack : input)
        {
            int amountNeeded = ItemStackUtils.getSize(stack);
            boolean hasStack = false;
            for (final IItemHandler handler : inventories)
            {
                hasStack = InventoryUtils.hasItemInItemHandler(handler, itemStack -> !ItemStackUtils.isEmpty(itemStack) && itemStack.isItemEqual(stack));

                if (hasStack)
                {
                    final int count = InventoryUtils.getItemCountInItemHandler(handler, itemStack -> !ItemStackUtils.isEmpty(itemStack) && itemStack.isItemEqual(stack));
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

    /**
     * Serialize to a bytebuffer the recipeStorage.
     * @param buf the bytebuf.
     */
    public void writeToBuffer(final ByteBuf buf)
    {
        buf.writeInt(input.size());
        for (@NotNull final ItemStack stack : input)
        {
            ByteBufUtils.writeItemStack(buf, stack);
        }

        ByteBufUtils.writeItemStack(buf, primaryOutput);

        buf.writeInt(secondaryOutput.size());
        for (@NotNull final ItemStack stack : secondaryOutput)
        {
            ByteBufUtils.writeItemStack(buf, stack);
        }

        buf.writeBoolean(intermediate != null);
        if(intermediate != null)
        {
            ByteBufUtils.writeUTF8String(buf, intermediate.getRegistryName().toString());
        }

        buf.writeInt(gridSize);
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
                || secondaryOutput.size() != that.secondaryOutput.size()
                || !primaryOutput.equals(that.primaryOutput))
        {
            return false;
        }

        for(final ItemStack stack: input)
        {
            if(!that.input.contains(stack))
            {
                return false;
            }
        }

        for(final ItemStack stack: secondaryOutput)
        {
            if(!that.secondaryOutput.contains(stack))
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
        result = 31 * result + secondaryOutput.hashCode();
        result = 31 * result + (intermediate != null ? intermediate.hashCode() : 0);
        result = 31 * result + gridSize;
        return result;
    }

    /**
     * Serialize from a bytebuffer the recipeStorage.
     * @param buf the byteBuffer.
     * @return a new RecipeStorage.
     */
    public static RecipeStorage createFromByteBuffer(final ByteBuf buf)
    {
        final List<ItemStack> input = new ArrayList<>();
        final int inputSize = buf.readInt();
        for (int i = 0; i < inputSize; i++)
        {
            input.add(ByteBufUtils.readItemStack(buf));
        }

        final ItemStack primaryOutput = ByteBufUtils.readItemStack(buf);

        final List<ItemStack> secondaryOutput = new ArrayList<>();
        final int secondaryOutputSize = buf.readInt();
        for (int i = 0; i < secondaryOutputSize; i++)
        {
            secondaryOutput.add(ByteBufUtils.readItemStack(buf));
        }

        Block intermediate = null;
        if(buf.readBoolean())
        {
            intermediate = Block.getBlockFromName(ByteBufUtils.readUTF8String(buf));
        }
        final int gridSize = buf.readInt();

        return new RecipeStorage(input, gridSize, primaryOutput, intermediate, secondaryOutput.toArray(new ItemStack[secondaryOutput.size()]));
    }

    /**
     * Check for free space in the handlers.
     * @param handlers the handlers to check.
     * @return true if enough space.
     */
    private boolean checkForFreeSpace(final List<IItemHandler> handlers)
    {
        if(getSecondaryOutput().size() > getInput().size())
        {
            int freeSpace = 0;
            for (final IItemHandler handler : handlers)
            {
                freeSpace+= handler.getSlots() - InventoryUtils.getAmountOfStacksInItemHandler(handler);
            }

            if(freeSpace < getSecondaryOutput().size() - getInput().size())
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
    public boolean fullfillRecipe(final List<IItemHandler> handlers)
    {
        if(!checkForFreeSpace(handlers))
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

        for (final ItemStack stack : getSecondaryOutput())
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
