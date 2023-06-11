package com.minecolonies.api.advancements.citizen_eat_food;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * The test instance to check the "items" condition for the "citizen_eat_food" trigger
 */
public class CitizenEatFoodCriterionInstance extends AbstractCriterionTriggerInstance
{
    private ItemPredicate[] itemPredicates;

    public CitizenEatFoodCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CITIZEN_EAT_FOOD), ContextAwarePredicate.ANY);
    }

    /**
     * Construct the check with a single item condition
     * @param itemPredicates the food item that has to be eaten to succeed
     */
    public CitizenEatFoodCriterionInstance(final ItemPredicate[] itemPredicates)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CITIZEN_EAT_FOOD), ContextAwarePredicate.ANY);

        this.itemPredicates = itemPredicates;
    }

    /**
     * Performs the check for the conditions
     * @param foodItemStack the stack of food that was just consumed
     * @return whether the check succeeded
     */
    public boolean test(final ItemStack foodItemStack)
    {
        if (this.itemPredicates != null)
        {
            for (ItemPredicate itemPredicate : itemPredicates)
            {
                if (itemPredicate.matches(foodItemStack))
                {
                    return true;
                }
            }
            return false;
        }

        return true;
    }

    @NotNull
    public static CitizenEatFoodCriterionInstance deserializeFromJson(@NotNull final JsonObject jsonObject,
                                                                      @NotNull final DeserializationContext context)
    {
        if (jsonObject.has("items"))
        {
            final ItemPredicate[] itemPredicates = ItemPredicate.fromJsonArray(jsonObject.get("items"));
            return new CitizenEatFoodCriterionInstance(itemPredicates);
        }
        return new CitizenEatFoodCriterionInstance();
    }

    @NotNull
    @Override
    public JsonObject serializeToJson(@NotNull final SerializationContext context)
    {
        final JsonObject json = super.serializeToJson(context);
        if (this.itemPredicates != null && this.itemPredicates.length > 0)
        {
            final JsonArray outputItemPredicates = new JsonArray();
            for (ItemPredicate predicate : this.itemPredicates)
            {
                outputItemPredicates.add(predicate.serializeToJson());
            }
            json.add("items", outputItemPredicates);
        }
        return json;
    }
}
