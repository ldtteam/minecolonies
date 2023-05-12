package com.minecolonies.coremod.enchants;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

import static net.minecraft.world.item.enchantment.EnchantmentCategory.WEAPON;

/**
 * Enchant for adding extra damage against raiders
 */
public class RaiderDamageEnchant extends Enchantment
{
    public RaiderDamageEnchant(final Rarity rarity, final EquipmentSlot[] slotTypes)
    {
        super(rarity, WEAPON, slotTypes);
    }

    @Override
    public int getMinCost(int enchantmentLevel)
    {
        return 10;
    }

    @Override
    public int getMaxCost(int enchantmentLevel)
    {
        return 50;
    }

    @Override
    public int getMinLevel()
    {
        return 1;
    }

    @Override
    public int getMaxLevel()
    {
        return 2;
    }
}
