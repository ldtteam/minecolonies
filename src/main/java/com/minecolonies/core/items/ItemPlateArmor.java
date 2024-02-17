package com.minecolonies.core.items;

import net.minecraft.Util;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;

/**
 * Class handling the Plate Armor.
 */
public class ItemPlateArmor extends ArmorItem
{
    public static final ArmorMaterial PLATE_ARMOR = new MineColoniesArmorMaterial("minecolonies:plate_armor", 37, Util.make(new EnumMap<>(Type.class), map -> {
        map.put(Type.BOOTS, 3);
        map.put(Type.LEGGINGS, 6);
        map.put(Type.CHESTPLATE, 8);
        map.put(Type.HELMET, 3);
    }), 9, SoundEvents.ARMOR_EQUIP_IRON, 0F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT));

    /**
     * Constructor method for the Plate Armor
     *
     * @param properties      the item properties.
     * @param equipmentSlotIn the equipment slot of it.
     * @param materialIn      the material of the armour.
     * @param name            the name.
     */
    public ItemPlateArmor(
      @NotNull final String name,
      @NotNull final ArmorMaterial materialIn,
      @NotNull final ArmorItem.Type equipmentSlotIn,
      final Properties properties)
    {
        super(materialIn, equipmentSlotIn, properties);
    }
}
