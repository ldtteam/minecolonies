package com.minecolonies.coremod.items;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;

import net.minecraftforge.common.util.EnumHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Class handling the Pirate Gear.
 */
public class ItemPirateGear extends ArmorItem
{
    public static final ArmorMaterial PIRATE_ARMOR_1 =
      EnumHelper.addArmorMaterial("minecolonies:pirate", "minecolonies:pirate", 33, new int[] {3, 6, 8, 3}, 5, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 1F);
    public static final ArmorMaterial PIRATE_ARMOR_2 =
      EnumHelper.addArmorMaterial("minecolonies:pirate2", "minecolonies:pirate2", 15, new int[] {2, 5, 7, 2}, 5, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 4F);

    /**
     * Constructor method for the Chief Sword Item
     */
    public ItemPirateGear(
      @NotNull final String name,
      @NotNull final CreativeTabs tab,
      @NotNull final ArmorMaterial materialIn,
      final int renderIndexIn,
      @NotNull final EquipmentSlotType equipmentSlotIn)
    {
        super(materialIn, renderIndexIn, equipmentSlotIn);
        setTranslationKey(name);
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" +  name);
        setCreativeTab(tab);
    }
}
