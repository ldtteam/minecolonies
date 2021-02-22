package com.minecolonies.coremod.items;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Class handling the Pirate Gear.
 */
public class ItemPlateArmor extends ArmorItem
{
    public static final IArmorMaterial PLATE_ARMOR =
      new MineColoniesArmorMaterial("minecolonies:plate_armor", 33, new int[] {4, 7, 9, 4}, 6, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 2F, Ingredient.fromItems(Items.DIAMOND));

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
      @NotNull final ItemGroup tab,
      @NotNull final IArmorMaterial materialIn,
      @NotNull final EquipmentSlotType equipmentSlotIn,
      final Properties properties)
    {
        super(materialIn, equipmentSlotIn, properties.group(tab));
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + name);
    }

    /** TODOTG
     * Called when item is crafted/smelted. Used only by maps so far.
     @Override
     public void onCreated(ItemStack stack, World worldIn, PlayerEntity playerIn) {

     }
     */
}
