package com.minecolonies.api.advancements.citizen_eat_food;

import com.minecolonies.api.advancements.CriterionListeners;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.world.item.ItemStack;

/**
 * The listener instantiated for every advancement that listens to the associated criterion.
 * A basic class to trigger with the correct arguments
 */
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
