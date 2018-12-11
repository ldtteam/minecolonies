package com.minecolonies.coremod.items;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraftforge.common.util.EnumHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Class handling the Santa hat.
 */
public class ItemSantaHead extends ItemArmor
{
    public static final ArmorMaterial SANTA_HAT =
      EnumHelper.addArmorMaterial("minecolonies:santa_hat", "minecolonies:santa_hat", 500, new int[] {0, 0, 0, 0}, 5, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0F);

    /**
     * Constructor method for the Chief Sword Item
     */
    public ItemSantaHead(
      @NotNull final String name,
      @NotNull final CreativeTabs tab,
      @NotNull final ArmorMaterial materialIn,
      final int renderIndexIn,
      @NotNull final EntityEquipmentSlot equipmentSlotIn)
    {
        super(materialIn, renderIndexIn, equipmentSlotIn);
        setTranslationKey(name);
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" +  name);
        setCreativeTab(tab);
    }
}
