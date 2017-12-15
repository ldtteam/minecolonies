package com.minecolonies.api.crafting;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RecipeStorageFactory<I> implements IFactory<I, IRecipeStorage>
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

    @NotNull
    @Override
    public TypeToken<? extends IRecipeStorage> getFactoryOutputType()
    {
        return null;
    }

    @NotNull
    @Override
    public TypeToken<? extends I> getFactoryInputType()
    {
        return null;
    }

    @NotNull
    @Override
    public IRecipeStorage getNewInstance(@NotNull final IFactoryController factoryController, @NotNull final I i, @NotNull final Object... context)
    {
        return null;
    }

    @NotNull
    @Override
    public NBTTagCompound serialize(@NotNull final IFactoryController controller, @NotNull final IRecipeStorage iRecipeStorage)
    {
        return null;
    }

    @NotNull
    @Override
    public IRecipeStorage deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt)
    {
        return null;
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
}
