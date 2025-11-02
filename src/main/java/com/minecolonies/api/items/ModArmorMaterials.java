package com.minecolonies.api.items;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
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
    public final static DeferredRegister<ArmorMaterial> DEFERRED_REGISTER = DeferredRegister.create(Registries.ARMOR_MATERIAL, Constants.MOD_ID);

    public static final Holder<ArmorMaterial> GOGGLES = DEFERRED_REGISTER.register("build_goggles", () -> new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
        map.put(ArmorItem.Type.BOOTS, 0);
        map.put(ArmorItem.Type.LEGGINGS, 0);
        map.put(ArmorItem.Type.CHESTPLATE, 0);
        map.put(ArmorItem.Type.HELMET, 0);
    }),
        20,
        SoundEvents.ARMOR_EQUIP_LEATHER,
        () -> Ingredient.EMPTY,
        List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "build_goggles"), "", true)),
        0,
        0));

    public static final Holder<ArmorMaterial> SANTA_HAT = DEFERRED_REGISTER.register("santa_hat", () -> new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
        map.put(ArmorItem.Type.BOOTS, 0);
        map.put(ArmorItem.Type.LEGGINGS, 0);
        map.put(ArmorItem.Type.CHESTPLATE, 0);
        map.put(ArmorItem.Type.HELMET, 0);
    }),
        500,
        SoundEvents.ARMOR_EQUIP_LEATHER,
        () -> Ingredient.EMPTY,
        List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "santa_hat"), "", true)),
        0,
        0));

    public static final Holder<ArmorMaterial> PLATE_ARMOR =
        DEFERRED_REGISTER.register("plate_armor", () -> new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.BOOTS, 3);
            map.put(ArmorItem.Type.LEGGINGS, 6);
            map.put(ArmorItem.Type.CHESTPLATE, 8);
            map.put(ArmorItem.Type.HELMET, 3);
        }),
            37,
            SoundEvents.ARMOR_EQUIP_IRON,
            () -> Ingredient.of(Items.IRON_INGOT),
            List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "plate_armor"), "", true)),
            0,
            0));

    public static final Holder<ArmorMaterial> PIRATE_ARMOR_1 =
        DEFERRED_REGISTER.register("pirate_armor_1", () -> new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.BOOTS, 2);
            map.put(ArmorItem.Type.LEGGINGS, 5);
            map.put(ArmorItem.Type.CHESTPLATE, 6);
            map.put(ArmorItem.Type.HELMET, 2);
        }),
            5,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            () -> Ingredient.of(Items.DIAMOND),
            List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "pirate"), "", true)),
            0,
            0));

    public static final Holder<ArmorMaterial> PIRATE_ARMOR_2 =
        DEFERRED_REGISTER.register("pirate_armor_2", () -> new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.BOOTS, 3);
            map.put(ArmorItem.Type.LEGGINGS, 6);
            map.put(ArmorItem.Type.CHESTPLATE, 8);
            map.put(ArmorItem.Type.HELMET, 3);
        }),
            5,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            () -> Ingredient.of(Items.DIAMOND),
            List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "pirate2"), "", true)),
            2,
            0));
}
