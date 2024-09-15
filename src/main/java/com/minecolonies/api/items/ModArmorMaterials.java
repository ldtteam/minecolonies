package com.minecolonies.api.items;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

public class ModArmorMaterials
{
    public final static DeferredRegister<ArmorMaterial> REGISTRY = DeferredRegister.create(BuiltInRegistries.ARMOR_MATERIAL, Constants.MOD_ID);

    public static final Holder<ArmorMaterial> SANTA_HAT = REGISTRY.register("santa_hat", () -> new ArmorMaterial(
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

    public static final Holder<ArmorMaterial> PLATE_ARMOR = REGISTRY.register("plate_armor", () -> new ArmorMaterial(
      // Determines the defense value of this armor material, depending on what armor piece it is.
      Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
          map.put(ArmorItem.Type.BOOTS, 3);
          map.put(ArmorItem.Type.LEGGINGS, 6);
          map.put(ArmorItem.Type.CHESTPLATE, 8);
          map.put(ArmorItem.Type.HELMET, 3);
      }),
      37,
      SoundEvents.ARMOR_EQUIP_IRON,
      () -> Ingredient.of(Items.IRON_INGOT),
      List.of(),
      0,
      0
    ));

    public static final Holder<ArmorMaterial> GOGGLES = REGISTRY.register("build_goggles", () -> new ArmorMaterial(
      // Determines the defense value of this armor material, depending on what armor piece it is.
      Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
          map.put(ArmorItem.Type.BOOTS, 0);
          map.put(ArmorItem.Type.LEGGINGS, 0);
          map.put(ArmorItem.Type.CHESTPLATE, 0);
          map.put(ArmorItem.Type.HELMET, 0);
      }),
      20,
      SoundEvents.ARMOR_EQUIP_LEATHER,
      () -> Ingredient.EMPTY,
      List.of(),
      0,
      0
    ));

    public static final Holder<ArmorMaterial> PIRATE_ARMOR_1 = REGISTRY.register("pirate_armor_1", () -> new ArmorMaterial(
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

    public static final Holder<ArmorMaterial> PIRATE_ARMOR_2 = REGISTRY.register("pirate_armor_2", () -> new ArmorMaterial(
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
}
