package com.minecolonies.core.items;

import net.minecraft.Util;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;

/**
 * Class handling the Santa hat.
 */
public class ItemSantaHead extends ArmorItem
{
    public static final ArmorMaterial SANTA_HAT = new MineColoniesArmorMaterial("minecolonies:santa_hat", 500, Util.make(new EnumMap<>(Type.class), map -> {
        map.put(Type.BOOTS, 0);
        map.put(Type.LEGGINGS, 0);
        map.put(Type.CHESTPLATE, 0);
        map.put(Type.HELMET, 0);
    }), 5, SoundEvents.ARMOR_EQUIP_LEATHER, 0F, 0.0F, () -> Ingredient.EMPTY);

    /**
     * Constructor method for the Chief Sword Item
     *
     * @param name            the name.
     * @param materialIn      the material.
     * @param equipmentSlotIn the equipment slot.
     * @param properties      the item properties.
     */
    public ItemSantaHead(
      @NotNull final String name,
      @NotNull final ArmorMaterial materialIn,
      @NotNull final Type equipmentSlotIn,
      final Item.Properties properties)
    {
        super(materialIn, equipmentSlotIn, properties);
    }
}
