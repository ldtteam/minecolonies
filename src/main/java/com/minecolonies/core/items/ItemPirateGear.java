package com.minecolonies.core.items;

import net.minecraft.core.Holder;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;

/**
 * Class handling the Pirate Gear.
 */
public class ItemPirateGear extends ArmorItem
{
    /**
     * Constructor method for the Pirate Gear
     *
     * @param properties      the item properties.
     * @param equipmentSlotIn the equipment slot of it.
     * @param materialIn      the material of the armour.
     * @param name            the name.
     */
    public ItemPirateGear(
      @NotNull final Holder<ArmorMaterial> materialIn,
      @NotNull final Type equipmentSlotIn,
      final Item.Properties properties)
    {
        super(materialIn, equipmentSlotIn, properties);
    }
}
