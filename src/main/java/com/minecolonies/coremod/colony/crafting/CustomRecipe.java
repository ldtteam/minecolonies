package com.minecolonies.coremod.colony.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.research.UnlockAbilityResearchEffect;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;

/**
 * This class represents a recipe loaded from custom data that is available to a crafter
 * but not to a player
 */
public class CustomRecipe
{

    /**
     * The property name that indicates type for the recipe
     */
    public static final String RECIPE_TYPE = "type";

    /**
     * The recipe type 
     */
    public static final String RECIPE_TYPE_RECIPE = "recipe";

    /**
     * The remove type 
     */
    public static final String RECIPE_TYPE_REMOVE = "remove";

    /**
     * The property name that indicates crafter type for the recipe
     */
    public static final String RECIPE_CRAFTER = "crafter";

    /**
     * The property name for the inputs array
     */
    public static final String RECIPE_INPUTS = "inputs";

    /**
     * The property name for the result item 
     */
    public static final String RECIPE_RESULT = "result";

    /**
     * The property name for Count, used both in inputs array and for result
     */
    public static final String COUNT = "count";

    /**
     * The property name for the item id in the inputs array
     */
    public static final String ITEM = "item";

    /**
     * The property name for the intermediate block ID
     */
    public static final String RECIPE_INTERMEDIATE = "intermediate";

    /**
     * The property name for the required research id
     */
    public static final String RECIPE_RESEARCHID = "research-id";

    /**
     * The property name for the research id that invalidates this recipe
     */
    public static final String RECIPE_EXCLUDED_RESEARCHID = "not-research-id";

    /**
     * The crafter name for this instance, defaults to 'unknown'
     */
    private String crafter = "unknown";

    /**
     * The recipe id for this instance, used for removal and lookup
     */
    private String recipeId = "";

    private ArrayList<ItemStack> inputs = new ArrayList<>();
    private ItemStack result = null;
    private Block intermediate = Blocks.AIR;

    private String researchId = null;
    private String excludedResearchId = null;


    /**
     * This class can only be created by the parse static
     */
    private CustomRecipe() {

    }
    
    /**
     * Parse a Json object into a Custom recipe
     * @param recipeJson the json representing the recipe
     * @return new instance of CustomRecipe
     */
    public static CustomRecipe parse(JsonObject recipeJson)
    {
        CustomRecipe recipe = new CustomRecipe();
        
        if (recipeJson.has(RECIPE_CRAFTER))
        {
            recipe.crafter = recipeJson.get(RECIPE_CRAFTER).getAsString();
        }
        if (recipeJson.has(RECIPE_INPUTS))
        {
            for(JsonElement e : recipeJson.get(RECIPE_INPUTS).getAsJsonArray())
            {   
                if(e instanceof JsonElement && e.isJsonObject())
                {
                    JsonObject ingredient = e.getAsJsonObject();
                    if(ingredient.has(ITEM))
                    {
                        final String[] split = ingredient.get(ITEM).getAsString().split(":");
                        final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(split[0], split[1]));
                        final ItemStack stack = new ItemStack(item);
                        if(ingredient.has(COUNT))
                        {
                            stack.setCount(ingredient.get(COUNT).getAsInt());
                        }
                        recipe.inputs.add(stack);
                    }

                }
            }
        }
        if (recipeJson.has(RECIPE_RESULT))
        {
            final String[] split = recipeJson.get(RECIPE_RESULT).getAsString().split(":");
            final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(split[0], split[1]));
            recipe.result = new ItemStack(item);
        }
        if (recipeJson.has(COUNT) && recipe.result != null)
        {
            recipe.result.setCount(recipeJson.get(COUNT).getAsInt());
        }
        if (recipeJson.has(RECIPE_INTERMEDIATE))
        {
            final String[] split = recipeJson.get(RECIPE_INTERMEDIATE).getAsString().split(":");
            recipe.intermediate = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(split[0], split[1]));
        }
        if (recipeJson.has(RECIPE_RESEARCHID))
        {
            recipe.researchId = recipeJson.get(RECIPE_RESEARCHID).getAsString();
        }
        if (recipeJson.has(RECIPE_EXCLUDED_RESEARCHID))
        {
            recipe.excludedResearchId = recipeJson.get(RECIPE_EXCLUDED_RESEARCHID).getAsString();
        }

        return recipe;
    }

    /**
     * Get the name of the crafter this recipe applies to
     * @return crafter name
     */
    public String getCrafter()
    {
        return crafter;
    }

    /**
     * Get the ID for this recipe
     * @return
     */
    public String getRecipeId()
    {
        return recipeId;
    }

    /**
     * Set the ID for this recipe
     */
    public void setRecipeId(String recipeId)
    {
        this.recipeId = recipeId;
    }
 
    /**
     * Check to see if the recipe is currently valid for the colony
     * This does research checks, to verify that the appropriate researches are in the correct states
     */
    public boolean isValidForColony(IColony colony)
    {
        IResearchEffect<?> requiredEffect = null;
        IResearchEffect<?> excludedEffect = null;
        
        if (researchId != null)
        {
            requiredEffect = colony.getResearchManager().getResearchEffects().getEffect(researchId, UnlockAbilityResearchEffect.class);
        }

        if (excludedResearchId != null)
        {
            excludedEffect = colony.getResearchManager().getResearchEffects().getEffect(excludedResearchId, UnlockAbilityResearchEffect.class);
        }

        return (researchId == null || requiredEffect != null) && (excludedResearchId == null || excludedEffect == null);
    }

    /**
     * Get a the recipe storage represented by this recipe
     * @return
     */
    public IRecipeStorage getRecipeStorage()
    {
        return StandardFactoryController.getInstance().getNewInstance(
            TypeConstants.RECIPE,
            StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
            inputs,
            1,
            result,
            intermediate);
      }
}