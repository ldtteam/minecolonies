package com.minecolonies.coremod.items;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation for the IArmorMaterial interface so we can add custom armor types.
 */
public class MineColoniesArmorMaterial implements IArmorMaterial
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
    public int getDurability(@NotNull final EquipmentSlotType equipmentSlotType)
    {
        return MAX_DAMAGE_ARRAY[equipmentSlotType.getIndex()] * this.maxDamageFactor;
    }

    @Override
    public int getDamageReductionAmount(@NotNull final EquipmentSlotType equipmentSlotType)
    {
        return this.damageReductionAmountArray[equipmentSlotType.getIndex()];
    }

    @Override
    public int getEnchantability()
    {
        return enchantability;
    }

    @Override
    public SoundEvent getSoundEvent()
    {
        return soundEvent;
    }

    @Override
    public Ingredient getRepairMaterial()
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
}
