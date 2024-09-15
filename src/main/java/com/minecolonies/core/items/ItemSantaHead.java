package com.minecolonies.core.items;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

/**
 * Class handling the Santa hat.
 */
public class ItemSantaHead extends ArmorItem
{
    /**
     * Constructor method for the Chief Sword Item
     *
     * @param name            the name.
     * @param materialIn      the material.
     * @param equipmentSlotIn the equipment slot.
     * @param properties      the item properties.
     */
    public ItemSantaHead(
      @NotNull final Holder<ArmorMaterial> materialIn,
      @NotNull final Type equipmentSlotIn,
      final Item.Properties properties)
    {
        super(materialIn, equipmentSlotIn, properties);
    }
}
