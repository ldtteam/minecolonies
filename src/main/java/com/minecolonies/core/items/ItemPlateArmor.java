package com.minecolonies.core.items;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import org.jetbrains.annotations.NotNull;

/**
 * Class handling the Plate Armor.
 */
public class ItemPlateArmor extends ArmorItem
{
    /**
     * Constructor method for the Plate Armor
     *
     * @param properties      the item properties.
     * @param equipmentSlotIn the equipment slot of it.
     * @param materialIn      the material of the armour.
     * @param name            the name.
     */
    public ItemPlateArmor(
      @NotNull final Holder<ArmorMaterial> materialIn,
      @NotNull final ArmorItem.Type equipmentSlotIn,
      final Properties properties)
    {
        super(materialIn, equipmentSlotIn, properties);
    }
}
