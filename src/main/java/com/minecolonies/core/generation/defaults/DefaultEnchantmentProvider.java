package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.enchants.ModEnchants;
import com.minecolonies.api.items.ModTags;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.MultiplyValue;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

public class DefaultEnchantmentProvider
{
    public static void bootstrap(BootstrapContext<Enchantment> context)
    {
        context.register(
          ModEnchants.raiderDamage,
          Enchantment.enchantment(
              Enchantment.definition(
                context.lookup(Registries.ITEM).getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                1,
                2,
                Enchantment.constantCost(10),
                Enchantment.constantCost(50),
                2,
                EquipmentSlotGroup.MAINHAND
              )
            )
            .withEffect(
              EnchantmentEffectComponents.DAMAGE,
              new MultiplyValue(LevelBasedValue.perLevel(1.0f, 1.0f / 5.0f)),
              LootItemEntityPropertyCondition.hasProperties(
                LootContext.EntityTarget.THIS,
                EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(ModTags.raiders))
              )
            ).build(ModEnchants.raiderDamage.location())
        );
    }
}
