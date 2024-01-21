package com.minecolonies.api.advancements.citizen_eat_food;

import com.google.gson.JsonObject;
import com.minecolonies.api.advancements.AbstractCriterionTrigger;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class CitizenEatFoodTrigger extends AbstractCriterionTrigger<CitizenEatFoodListeners, CitizenEatFoodCriterionInstance>
{
    public CitizenEatFoodTrigger()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CITIZEN_EAT_FOOD), CitizenEatFoodListeners::new);
    }

    /**
     * Triggers the listener checks if there are any listening in
     * @param player the player the check regards
     * @param foodItemStack the food eaten by the citizen
     */
    public void trigger(final ServerPlayer player, final ItemStack foodItemStack)
    {
        if (player != null)
        {
            final CitizenEatFoodListeners listeners = this.getListeners(player.getAdvancements());
            if (listeners != null)
            {
                listeners.trigger(foodItemStack);
            }
        }
    }
    
    @Override
    public CitizenEatFoodCriterionInstance createInstance(final JsonObject jsonObject, final DeserializationContext conditionArrayParser)
    {
        return CitizenEatFoodCriterionInstance.deserializeFromJson(jsonObject, conditionArrayParser);
    }
}
