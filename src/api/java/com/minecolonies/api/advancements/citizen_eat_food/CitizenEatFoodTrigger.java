package com.minecolonies.api.advancements.citizen_eat_food;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.minecolonies.api.advancements.AbstractCriterionTrigger;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;


public class CitizenEatFoodTrigger extends AbstractCriterionTrigger<CitizenEatFoodListeners, CitizenEatFoodCriterionInstance>
{
    public CitizenEatFoodTrigger()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CITIZEN_EAT_FOOD), CitizenEatFoodListeners::new);
    }

    public void trigger(final EntityPlayerMP player, final ItemStack foodItemStack)
    {
        final CitizenEatFoodListeners listeners = this.getListeners(player.getAdvancements());
        if (listeners != null)
        {
            listeners.trigger(foodItemStack);
        }
    }

    @NotNull
    @Override
    public CitizenEatFoodCriterionInstance deserializeInstance(@NotNull final JsonObject jsonObject, @NotNull final JsonDeserializationContext jsonDeserializationContext)
    {
        if (jsonObject.has("items"))
        {
            final ItemPredicate[] itemPredicates = ItemPredicate.deserializeArray(jsonObject.get("items"));
            return new CitizenEatFoodCriterionInstance(itemPredicates);
        }
        return new CitizenEatFoodCriterionInstance();
    }
}
