package com.minecolonies.api.advancements.citizen_eat_food;

import com.minecolonies.api.advancements.CriterionListeners;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.item.ItemStack;

public class CitizenEatFoodListeners extends CriterionListeners<CitizenEatFoodCriterionInstance>
{
    public CitizenEatFoodListeners(final PlayerAdvancements playerAdvancements)
    {
        super(playerAdvancements);
    }

    public void trigger(final ItemStack foodItemStack)
    {
        trigger(instance -> instance.test(foodItemStack));
    }
}
