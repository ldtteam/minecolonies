package com.minecolonies.core.items;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;

import static com.minecolonies.apiimp.initializer.ModItemsInitializer.DEFERRED_REGISTER;

/**
 * Class handling the Pirate Gear.
 */
public class ItemPirateGear extends ArmorItem
{
    public static final Holder<ArmorMaterial> PIRATE_ARMOR_1 = DEFERRED_REGISTER.register("pirate_armor_1", () -> new ArmorMaterial(
      // Determines the defense value of this armor material, depending on what armor piece it is.
      Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
          map.put(ArmorItem.Type.BOOTS, 2);
          map.put(ArmorItem.Type.LEGGINGS, 5);
          map.put(ArmorItem.Type.CHESTPLATE, 6);
          map.put(ArmorItem.Type.HELMET, 2);
      }),
      5,
      SoundEvents.ARMOR_EQUIP_LEATHER,
      () -> Ingredient.of(Items.DIAMOND),
      List.of(),
      0,
      0
    ));

    public static final Holder<ArmorMaterial> PIRATE_ARMOR_2 = DEFERRED_REGISTER.register("pirate_armor_2", () -> new ArmorMaterial(
      // Determines the defense value of this armor material, depending on what armor piece it is.
      Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
          map.put(ArmorItem.Type.BOOTS, 3);
          map.put(ArmorItem.Type.LEGGINGS, 6);
          map.put(ArmorItem.Type.CHESTPLATE, 8);
          map.put(ArmorItem.Type.HELMET, 3);
      }),
      5,
      SoundEvents.ARMOR_EQUIP_LEATHER,
      () -> Ingredient.of(Items.DIAMOND),
      List.of(),
      2,
      0
    ));

    /**
     * Constructor method for the Pirate Gear
     *
     * @param properties      the item properties.
     * @param equipmentSlotIn the equipment slot of it.
     * @param materialIn      the material of the armour.
     * @param name            the name.
     */
    public ItemPirateGear(
      @NotNull final String name,
      @NotNull final Holder<ArmorMaterial> materialIn,
      @NotNull final Type equipmentSlotIn,
      final Item.Properties properties)
    {
        super(materialIn, equipmentSlotIn, properties);
    }
}
