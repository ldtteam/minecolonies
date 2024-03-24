package com.minecolonies.api.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger.SimpleInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CitizenEatFoodTrigger extends SimpleCriterionTrigger<CitizenEatFoodTrigger.CitizenEatFoodTriggerInstance>
{
    /**
     * Triggers the listener checks if there are any listening in
     * 
     * @param player        the player the check regards
     * @param foodItemStack the food eaten by the citizen
     */
    public void trigger(final ServerPlayer player, final ItemStack foodItemStack)
    {
        trigger(player, trigger -> trigger.test(foodItemStack));
    }

    @Override
    public Codec<CitizenEatFoodTriggerInstance> codec()
    {
        return CitizenEatFoodTriggerInstance.CODEC;
    }

    public static record CitizenEatFoodTriggerInstance(Optional<ContextAwarePredicate> player, List<ItemPredicate> itemPredicates) implements SimpleInstance
    {
        public static final List<ItemPredicate> DEFAULT_OUTPUT_ITEM_PREDICATES = Collections.emptyList();

        public static final Codec<CitizenEatFoodTriggerInstance> CODEC = RecordCodecBuilder.create(builder -> builder
            .group(ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(CitizenEatFoodTriggerInstance::player),
                ExtraCodecs.strictOptionalField(ItemPredicate.CODEC.listOf(), "items", DEFAULT_OUTPUT_ITEM_PREDICATES).forGetter(CitizenEatFoodTriggerInstance::itemPredicates))
            .apply(builder, CitizenEatFoodTriggerInstance::new));

        public static Criterion<CitizenEatFoodTriggerInstance> citizenEatFood()
        {
            return citizenEatFood(DEFAULT_OUTPUT_ITEM_PREDICATES);
        }

        /**
         * Construct the check with a single item condition
         * 
         * @param itemPredicates the food item that has to be eaten to succeed
         */
        public static Criterion<CitizenEatFoodTriggerInstance> citizenEatFood(final List<ItemPredicate> itemPredicates)
        {
            return AdvancementTriggers.CITIZEN_EAT_FOOD.get()
                .createCriterion(new CitizenEatFoodTriggerInstance(Optional.empty(), itemPredicates));
        }

        /**
         * Performs the check for the conditions
         * 
         * @param  foodItemStack the stack of food that was just consumed
         * @return               whether the check succeeded
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
    }
}
