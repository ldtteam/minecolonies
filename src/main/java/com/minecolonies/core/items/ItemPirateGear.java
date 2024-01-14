package com.minecolonies.core.items;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Class handling the Pirate Gear.
 */
public class ItemPirateGear extends ArmorItem
{
    public static final ArmorMaterial PIRATE_ARMOR_1 =
      new MineColoniesArmorMaterial("minecolonies:pirate", 33, new int[] {3, 6, 8, 3}, 5, SoundEvents.ARMOR_EQUIP_LEATHER, 1F, Ingredient.of(Items.DIAMOND));
    public static final ArmorMaterial PIRATE_ARMOR_2 =
      new MineColoniesArmorMaterial("minecolonies:pirate2", 15, new int[] {2, 5, 7, 2}, 5, SoundEvents.ARMOR_EQUIP_LEATHER, 4F, Ingredient.of(Items.DIAMOND));

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
