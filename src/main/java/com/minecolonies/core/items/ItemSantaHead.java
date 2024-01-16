package com.minecolonies.core.items;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

/**
 * Class handling the Santa hat.
 */
public class ItemSantaHead extends ArmorItem
{
    public static final ArmorMaterial SANTA_HAT =
      new MineColoniesArmorMaterial("minecolonies:santa_hat", 500, new int[] {0, 0, 0, 0}, 5, SoundEvents.ARMOR_EQUIP_LEATHER, 0F, Ingredient.EMPTY);

    /**
     * Constructor method for the Chief Sword Item
     *
     * @param name            the name.
     * @param tab             the item tab.
     * @param materialIn      the material.
     * @param equipmentSlotIn the equipment slot.
     * @param properties      the item properties.
     */
    public ItemSantaHead(
      @NotNull final String name,
      @NotNull final CreativeModeTab tab,
      @NotNull final ArmorMaterial materialIn,
      @NotNull final EquipmentSlot equipmentSlotIn,
      final Item.Properties properties)
    {
        super(materialIn, equipmentSlotIn, properties.tab(tab));
    }
}
