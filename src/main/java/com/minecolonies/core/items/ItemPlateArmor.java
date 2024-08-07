package com.minecolonies.core.items;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.minecolonies.apiimp.initializer.ModItemsInitializer.DEFERRED_REGISTER;

/**
 * Class handling the Plate Armor.
 */
public class ItemPlateArmor extends ArmorItem
{
    public static final Holder<ArmorMaterial> PLATE_ARMOR = DEFERRED_REGISTER.register("plate_armor", () -> new ArmorMaterial(
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

    /**
     * Constructor method for the Plate Armor
     *
     * @param properties      the item properties.
     * @param equipmentSlotIn the equipment slot of it.
     * @param materialIn      the material of the armour.
     * @param name            the name.
     */
    public ItemPlateArmor(
      @NotNull final String name,
      @NotNull final Holder<ArmorMaterial> materialIn,
      @NotNull final ArmorItem.Type equipmentSlotIn,
      final Properties properties)
    {
        super(materialIn, equipmentSlotIn, properties);
    }
}
