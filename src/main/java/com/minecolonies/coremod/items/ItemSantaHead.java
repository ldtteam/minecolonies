package com.minecolonies.coremod.items;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvents;
import org.jetbrains.annotations.NotNull;

/**
 * Class handling the Santa hat.
 */
public class ItemSantaHead extends ArmorItem
{
    public static final IArmorMaterial SANTA_HAT =
      new MineColoniesArmorMaterial("minecolonies:santa_hat", 500, new int[] {0, 0, 0, 0}, 5, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0F, Ingredient.fromItems());

    /**
     * Constructor method for the Chief Sword Item
     */
    public ItemSantaHead(
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
