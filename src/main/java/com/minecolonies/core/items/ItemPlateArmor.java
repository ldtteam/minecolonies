package com.minecolonies.core.items;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;

/**
 * Class handling the Plate Armor.
 */
public class ItemPlateArmor extends ArmorItem
{
    public static final ArmorMaterial PLATE_ARMOR =
      new MineColoniesArmorMaterial("minecolonies:plate_armor", 20, new int[] {4, 7, 9, 4}, 6, SoundEvents.ARMOR_EQUIP_IRON, 2F, Ingredient.of(Items.IRON_INGOT));

    /**
     * Constructor method for the Plate Armor
     *
     * @param properties      the item properties.
     * @param equipmentSlotIn the equipment slot of it.
     * @param materialIn      the material of the armour.
     * @param name            the name.
     * @param tab             the item group tab.
     */
    public ItemPlateArmor(
      @NotNull final String name,
      @NotNull final CreativeModeTab tab,
      @NotNull final ArmorMaterial materialIn,
      @NotNull final EquipmentSlot equipmentSlotIn,
      final Properties properties)
    {
        super(materialIn, equipmentSlotIn, properties.tab(tab));
    }
}
