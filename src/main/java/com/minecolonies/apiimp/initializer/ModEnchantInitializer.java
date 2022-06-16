package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.enchants.ModEnchants;
import com.minecolonies.coremod.enchants.RaiderDamageEnchant;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.entity.EquipmentSlot;

import static com.minecolonies.api.enchants.ModEnchants.ENCHANMENTS;

/**
 * Enchants initializer
 */
public class ModEnchantInitializer
{
    static
    {
        ModEnchants.raiderDamage = ENCHANMENTS.register("raider_damage_enchant", () -> new RaiderDamageEnchant(Enchantment.Rarity.VERY_RARE, new EquipmentSlot[] {EquipmentSlot.MAINHAND}));
    }
}
