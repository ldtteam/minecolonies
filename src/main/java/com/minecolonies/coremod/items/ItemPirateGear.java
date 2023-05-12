package com.minecolonies.coremod.items;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

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
     * @param tab             the item group tab.
     */
    public ItemPirateGear(
      @NotNull final String name,
      @NotNull final CreativeModeTab tab,
      @NotNull final ArmorMaterial materialIn,
      @NotNull final EquipmentSlot equipmentSlotIn,
      final Item.Properties properties)
    {
        super(materialIn, equipmentSlotIn, properties.tab(tab));
    }
}
