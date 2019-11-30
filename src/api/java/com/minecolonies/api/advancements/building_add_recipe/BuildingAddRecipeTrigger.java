package com.minecolonies.api.advancements.building_add_recipe;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.minecolonies.api.advancements.AbstractCriterionTrigger;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;


public class BuildingAddRecipeTrigger extends AbstractCriterionTrigger<BuildingAddRecipeListeners, BuildingAddRecipeCriterionInstance>
{
    public BuildingAddRecipeTrigger()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_BUILDING_ADD_RECIPE), BuildingAddRecipeListeners::new);
    }

    public void trigger(final EntityPlayerMP player, final IRecipeStorage recipeStorage)
    {
        final BuildingAddRecipeListeners listeners = this.getListeners(player.getAdvancements());
        if (listeners != null)
        {
            listeners.trigger(recipeStorage);
        }
    }

    @NotNull
    @Override
    public BuildingAddRecipeCriterionInstance deserializeInstance(@NotNull final JsonObject jsonObject, @NotNull final JsonDeserializationContext jsonDeserializationContext)
    {
        if (jsonObject.has("items"))
        {
            final ItemPredicate[] outputItemPredicates = ItemPredicate.deserializeArray(jsonObject.get("items"));
            if (jsonObject.has("crafting_size"))
            {
                final int craftingSize = JsonUtils.getInt(jsonObject, "crafting_size");
                return new BuildingAddRecipeCriterionInstance(outputItemPredicates, craftingSize);
            }
            return new BuildingAddRecipeCriterionInstance(outputItemPredicates);
        }
        return new BuildingAddRecipeCriterionInstance();
    }
}
