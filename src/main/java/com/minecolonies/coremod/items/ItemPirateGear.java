package com.minecolonies.coremod.items;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;

import net.minecraftforge.common.util.EnumHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Class handling the Pirate Gear.
 */
public class ItemPirateGear extends ItemArmor
{
    public static final ArmorMaterial PIRATE_ARMOR_1 =
      EnumHelper.addArmorMaterial("minecolonies:pirate", "minecolonies:pirate", 1500, new int[] {2, 4, 3, 1}, 5, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 10F);
    public static final ArmorMaterial PIRATE_ARMOR_2 =
      EnumHelper.addArmorMaterial("minecolonies:pirate2", "minecolonies:pirate2", 1500, new int[] {3, 5, 4, 2}, 5, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 6F);

    /**
     * Constructor method for the Chief Sword Item
     */
    public ItemPirateGear(
      @NotNull final String name,
      @NotNull final CreativeTabs tab,
      @NotNull final ArmorMaterial materialIn,
      final int renderIndexIn,
      @NotNull final EntityEquipmentSlot equipmentSlotIn)
    {
        super(materialIn, renderIndexIn, equipmentSlotIn);
        setTranslationKey(name);
        setRegistryName(Constants.MOD_ID + ":" + name);
        setCreativeTab(tab);
    }
}
