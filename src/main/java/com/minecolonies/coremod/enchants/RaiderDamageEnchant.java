package com.minecolonies.coremod.enchants;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EquipmentSlotType;

import static net.minecraft.enchantment.EnchantmentType.WEAPON;

import net.minecraft.enchantment.Enchantment.Rarity;

/**
 * Enchant for adding extra damage against raiders
 */
public class RaiderDamageEnchant extends Enchantment
{
    /**
     * Enchant id
     */
    private final String NAME_ID = "raider_damage_enchant";

    public RaiderDamageEnchant(final Rarity rarity, final EquipmentSlotType[] slotTypes)
    {
        super(rarity, WEAPON, slotTypes);
        setRegistryName(NAME_ID);
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
