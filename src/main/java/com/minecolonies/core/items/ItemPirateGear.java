package com.minecolonies.core.items;

import net.minecraft.Util;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;

/**
 * Class handling the Pirate Gear.
 */
public class ItemPirateGear extends ArmorItem
{
    public static final ArmorMaterial PIRATE_ARMOR_1 = new MineColoniesArmorMaterial("minecolonies:pirate", 33, Util.make(new EnumMap<>(Type.class), map -> {
        map.put(Type.BOOTS, 2);
        map.put(Type.LEGGINGS, 5);
        map.put(Type.CHESTPLATE, 6);
        map.put(Type.HELMET, 2);
    }), 5, SoundEvents.ARMOR_EQUIP_LEATHER, 0F, 0F, () -> Ingredient.of(Items.DIAMOND));

    public static final ArmorMaterial PIRATE_ARMOR_2 = new MineColoniesArmorMaterial("minecolonies:pirate2", 15, Util.make(new EnumMap<>(Type.class), map -> {
        map.put(Type.BOOTS, 3);
        map.put(Type.LEGGINGS, 6);
        map.put(Type.CHESTPLATE, 8);
        map.put(Type.HELMET, 3);
    }), 5, SoundEvents.ARMOR_EQUIP_LEATHER, 2F, 0F, () -> Ingredient.of(Items.DIAMOND));

    /**
     * Constructor method for the Pirate Gear
     *
     * @param properties      the item properties.
     * @param equipmentSlotIn the equipment slot of it.
     * @param materialIn      the material of the armour.
     * @param name            the name.
     */
    public ItemPirateGear(
      @NotNull final String name,
      @NotNull final ArmorMaterial materialIn,
      @NotNull final Type equipmentSlotIn,
      final Item.Properties properties)
    {
        super(materialIn, equipmentSlotIn, properties);
    }
}
