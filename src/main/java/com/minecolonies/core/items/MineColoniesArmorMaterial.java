package com.minecolonies.core.items;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation for the IArmorMaterial interface so we can add custom armor types.
 */
public class MineColoniesArmorMaterial implements ArmorMaterial
{
    /**
     * Max durability by equipment slot, taken from vanilla.
     */
    private static final int[] MAX_DAMAGE_ARRAY = new int[] {13, 15, 16, 11};

    private final String     name;
    private final int        maxDamageFactor;
    private final int[]      damageReductionAmountArray;
    private final int        enchantability;
    private final SoundEvent soundEvent;
    private final float      toughness;
    private final Ingredient repairMaterial;

    public MineColoniesArmorMaterial(
      @NotNull final String name,
      final int maxDamageFactor,
      final int[] damageReductionAmountArray,
      final int enchantability,
      @NotNull final SoundEvent soundEvent,
      final float toughness,
      @NotNull final Ingredient repairMaterial)
    {
        this.name = name;
        this.maxDamageFactor = maxDamageFactor;
        this.damageReductionAmountArray = damageReductionAmountArray;
        this.enchantability = enchantability;
        this.soundEvent = soundEvent;
        this.toughness = toughness;
        this.repairMaterial = repairMaterial;
    }

    @Override
    public int getDurabilityForSlot(@NotNull final EquipmentSlot equipmentSlotType)
    {
        return MAX_DAMAGE_ARRAY[equipmentSlotType.getIndex()] * this.maxDamageFactor;
    }

    @Override
    public int getDefenseForSlot(@NotNull final EquipmentSlot equipmentSlotType)
    {
        return this.damageReductionAmountArray[equipmentSlotType.getIndex()];
    }

    @Override
    public int getEnchantmentValue()
    {
        return enchantability;
    }

    @Override
    public SoundEvent getEquipSound()
    {
        return soundEvent;
    }

    @Override
    public Ingredient getRepairIngredient()
    {
        return this.repairMaterial;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public float getToughness()
    {
        return toughness;
    }

    @Override
    public float getKnockbackResistance()
    {
        return 0;
    }
}
