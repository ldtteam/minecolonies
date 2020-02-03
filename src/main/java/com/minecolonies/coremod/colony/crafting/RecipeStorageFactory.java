package com.minecolonies.coremod.colony.crafting;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorageFactory;
import com.minecolonies.api.crafting.RecipeStorage;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_TOKEN;

/**
 * Factory implementation taking care of creating new instances, serializing and deserializing RecipeStorages.
 */
public class RecipeStorageFactory implements IRecipeStorageFactory
{
    /**
     * Compound tag for the grid size.
     */
    private static final String TAG_GRID = "grid";

    /**
     * Tag to store the blockstate.
     */
    private static final String BLOCK_TAG = "block";

    /**
     * Compound tag for the input.
     */
    private static final String INPUT_TAG = "input";

    @NotNull
    @Override
    public TypeToken<RecipeStorage> getFactoryOutputType()
    {
        return TypeConstants.RECIPE;
    }

    @NotNull
    @Override
    public TypeToken<? extends IToken> getFactoryInputType()
    {
        return TypeConstants.ITOKEN;
    }

    @NotNull
    @Override
    public RecipeStorage getNewInstance(@NotNull final IToken token, @NotNull final List<ItemStack> input, final int gridSize, @NotNull final ItemStack primaryOutput, final Block intermediate)
    {
        return new RecipeStorage(token, input, gridSize, primaryOutput, intermediate);
    }

    @NotNull
    @Override
    public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final RecipeStorage recipeStorage)
    {
        final CompoundNBT compound = new CompoundNBT();
        @NotNull final ListNBT inputTagList = new ListNBT();
        for (@NotNull final ItemStack stack : recipeStorage.getInput())
        {
            @NotNull final CompoundNBT neededRes = new CompoundNBT();
            stack.write(neededRes);
            inputTagList.add(neededRes);
        }
        compound.put(INPUT_TAG, inputTagList);
        recipeStorage.getPrimaryOutput().write(compound);

        if(recipeStorage.getIntermediate() != null)
        {
            compound.put(BLOCK_TAG, NBTUtil.writeBlockState(recipeStorage.getIntermediate().getDefaultState()));
        }
        compound.putInt(TAG_GRID, recipeStorage.getGridSize());
        compound.put(TAG_TOKEN, StandardFactoryController.getInstance().serialize(recipeStorage.getToken()));
        return compound;
    }

    @NotNull
    @Override
    public RecipeStorage deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
    {
        final List<ItemStack> input = new ArrayList<>();
        final ListNBT inputTagList = nbt.getList(INPUT_TAG, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < inputTagList.size(); ++i)
        {
            final CompoundNBT inputTag = inputTagList.getCompound(i);
            input.add(ItemStack.read(inputTag));
        }

        final ItemStack primaryOutput = ItemStack.read(nbt);
        final Block intermediate = NBTUtil.readBlockState(nbt.getCompound(BLOCK_TAG)).getBlock();
        final int gridSize = nbt.getInt(TAG_GRID);
        final IToken token = StandardFactoryController.getInstance().deserialize(nbt.getCompound(TAG_TOKEN));

        return this.getNewInstance(token, input, gridSize, primaryOutput, intermediate);
    }
}
