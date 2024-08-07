package com.minecolonies.core.items;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;

import static com.minecolonies.apiimp.initializer.ModItemsInitializer.DEFERRED_REGISTER;

/**
 * Class handling the Santa hat.
 */
public class ItemSantaHead extends ArmorItem
{
    public static final Holder<ArmorMaterial> SANTA_HAT = DEFERRED_REGISTER.register("plate_armor", () -> new ArmorMaterial(
      // Determines the defense value of this armor material, depending on what armor piece it is.
      Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
          map.put(ArmorItem.Type.BOOTS, 0);
          map.put(ArmorItem.Type.LEGGINGS, 0);
          map.put(ArmorItem.Type.CHESTPLATE, 0);
          map.put(ArmorItem.Type.HELMET, 0);
      }),
      500,
      SoundEvents.ARMOR_EQUIP_LEATHER,
      () -> Ingredient.EMPTY,
      List.of(),
      0,
      0
    ));

    /**
     * Constructor method for the Chief Sword Item
     *
     * @param name            the name.
     * @param materialIn      the material.
     * @param equipmentSlotIn the equipment slot.
     * @param properties      the item properties.
     */
    public ItemSantaHead(
      @NotNull final String name,
      @NotNull final Holder<ArmorMaterial> materialIn,
      @NotNull final Type equipmentSlotIn,
      final Item.Properties properties)
    {
        super(materialIn, equipmentSlotIn, properties);
    }
}
