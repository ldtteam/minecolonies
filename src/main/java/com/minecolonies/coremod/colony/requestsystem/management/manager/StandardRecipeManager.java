package com.minecolonies.coremod.colony.requestsystem.management.manager;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeManager;
import com.minecolonies.api.crafting.RecipeStorage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class StandardRecipeManager implements IRecipeManager
{
    /**
     * Store the token tag to nbt.
     */
    private static final String TOKEN_TAG = "tokenTag";

    /**
     * The Tag to store the recipes to NBT.
     */
    private static final String TAG_RECIPES = "recipes";

    /**
     * Map of all recipes which have been discovered globally already.
     */
    private static final BiMap<IToken, RecipeStorage> recipes = HashBiMap.create();

    @Override
    public Map<IToken, RecipeStorage> getRecipes()
    {
        return ImmutableMap.copyOf(recipes);
    }

    @Override
    public IToken addRecipe(final RecipeStorage storage)
    {
        recipes.put(storage.getToken(), storage);
        return storage.getToken();
    }

    @Override
    public IToken checkOrAddRecipe(final RecipeStorage storage)
    {
        final IToken token = getRecipeId(storage);
        if(token == null)
        {
            return addRecipe(storage);
        }
        return token;
    }

    @Override
    public IToken getRecipeId(final RecipeStorage storage)
    {
        for(final Map.Entry<IToken, RecipeStorage> tempStorage: recipes.entrySet())
        {
            if(tempStorage.getValue().equals(storage))
            {
                return tempStorage.getKey();
            }
        }
        return null;
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        @NotNull final NBTTagList recipesTagList = new NBTTagList();
        for (@NotNull final Map.Entry<IToken, RecipeStorage> entry : recipes.entrySet())
        {
            @NotNull final NBTTagCompound recipeTagCompound = new NBTTagCompound();
            recipeTagCompound.setTag(TOKEN_TAG, StandardFactoryController.getInstance().serialize(entry.getKey()));
            StandardFactoryController.getInstance().serialize(entry.getValue());
            recipesTagList.appendTag(recipeTagCompound);
        }
        compound.setTag(TAG_RECIPES, recipesTagList);
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        final NBTTagList recipesTags = compound.getTagList(TAG_RECIPES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < recipesTags.tagCount(); ++i)
        {
            final NBTTagCompound recipeTag = recipesTags.getCompoundTagAt(i);
            final IToken token = StandardFactoryController.getInstance().deserialize(recipeTag.getCompoundTag(TOKEN_TAG));
            final RecipeStorage storage = StandardFactoryController.getInstance().deserialize(recipeTag);

            recipes.put(token, storage);
        }
    }
}
