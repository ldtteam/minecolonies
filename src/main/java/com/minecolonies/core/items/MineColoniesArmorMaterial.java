package com.minecolonies.core.items;

import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Implementation for the ArmorMaterial interface, so we can add custom armor types.
 */
public class MineColoniesArmorMaterial implements ArmorMaterial
{
    private static final EnumMap<ArmorItem.Type, Integer> HEALTH_FUNCTION_FOR_TYPE = Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
        map.put(Type.BOOTS, 13);
        map.put(Type.LEGGINGS, 15);
        map.put(Type.CHESTPLATE, 16);
        map.put(Type.HELMET, 11);
    });

    private final String                      name;
    private final int                         durabilityMultiplier;
    private final Map<Type, Integer>          protectionFunctionForType;
    private final int                         enchantmentValue;
    private final SoundEvent                  sound;
    private final float                       toughness;
    private final float                       knockbackResistance;
    private final LazyLoadedValue<Ingredient> repairIngredient;

    public MineColoniesArmorMaterial(
      final String name,
      final int durabilityMultiplier,
      final Map<Type, Integer> protectionFunctionForType,
      final int enchantmentValue,
      final SoundEvent sound,
      final float toughness,
      final float knockbackResistance,
      final Supplier<Ingredient> repairIngredient)
    {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.protectionFunctionForType = protectionFunctionForType;
        this.enchantmentValue = enchantmentValue;
        this.sound = sound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = new LazyLoadedValue<>(repairIngredient);
    }

    public int getDurabilityForType(@NotNull ArmorItem.Type type)
    {
        return HEALTH_FUNCTION_FOR_TYPE.get(type) * this.durabilityMultiplier;
    }

    public int getDefenseForType(@NotNull ArmorItem.Type type)
    {
        return this.protectionFunctionForType.get(type);
    }

    public int getEnchantmentValue()
    {
        return this.enchantmentValue;
    }

    @NotNull
    public SoundEvent getEquipSound()
    {
        return this.sound;
    }

    @NotNull
    public Ingredient getRepairIngredient()
    {
        return this.repairIngredient.get();
    }

    @NotNull
    public String getName()
    {
        return this.name;
    }

    public float getToughness()
    {
        return this.toughness;
    }

    public float getKnockbackResistance()
    {
        return this.knockbackResistance;
    }
}
