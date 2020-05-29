package com.minecolonies.coremod.items;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvents;
import org.jetbrains.annotations.NotNull;

/**
 * Class handling the Pirate Gear.
 */
public class ItemPirateGear extends ArmorItem
{
    public static final IArmorMaterial PIRATE_ARMOR_1 =
      new MineColoniesArmorMaterial("minecolonies:pirate", 33, new int[] {3, 6, 8, 3}, 5, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 1F, Ingredient.fromItems(Items.DIAMOND));
    public static final IArmorMaterial PIRATE_ARMOR_2 =
      new MineColoniesArmorMaterial("minecolonies:pirate2", 15, new int[] {2, 5, 7, 2}, 5, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 4F, Ingredient.fromItems(Items.DIAMOND));

    /**
     * Constructor method for the Pirate Gear
     * @param properties the item properties.
     * @param equipmentSlotIn the equipment slot of it.
     * @param materialIn the material of the armour.
     * @param name the name.
     * @param tab the item group tab.
     */
    public ItemPirateGear(
      @NotNull final String name,
      @NotNull final ItemGroup tab,
      @NotNull final IArmorMaterial materialIn,
      @NotNull final EquipmentSlotType equipmentSlotIn,
      final Item.Properties properties)
    {
        super(materialIn, equipmentSlotIn, properties.group(tab));
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + name);
    }
}
