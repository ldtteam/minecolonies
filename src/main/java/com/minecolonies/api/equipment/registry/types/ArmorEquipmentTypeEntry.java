package com.minecolonies.api.equipment.registry.types;

import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.util.Log;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

/**
 * Equipment type for armor pieces: helmet, leggings, chestplate, boots. There is no vanilla cross-mod
 * tier-ordering concept for armor (unlike {@link DiggerEquipmentTypeEntry}, which can lean on
 * {@link net.minecraftforge.common.TierSortingRegistry}), so level is instead determined by comparing the
 * stack's armor + toughness value against a reference ladder built from real vanilla armor items' actual
 * attribute values, walked from the top down so a stack resolves to the highest tier it meets or exceeds.
 * Armor values differ per slot (e.g. a chestplate has higher defense than a helmet at the same tier), so
 * the ladder is built per slot rather than always comparing against helmets.
 */
public class ArmorEquipmentTypeEntry extends EquipmentTypeEntry
{
    /**
     * Reference ladders of (level, representative item for that level), ordered from highest to lowest,
     * one per equipment slot, used to resolve the level of an arbitrary (including modded) armor piece by
     * its actual attribute values rather than a hardcoded per-item table.
     */
    private static final Map<EquipmentSlot, List<Map.Entry<Integer, Item>>> REFERENCE_LADDERS = new EnumMap<>(Map.of(EquipmentSlot.HEAD,
        List.of(entry(5, Items.NETHERITE_HELMET), entry(4, Items.DIAMOND_HELMET), entry(3, Items.IRON_HELMET), entry(2, Items.CHAINMAIL_HELMET), entry(1, Items.LEATHER_HELMET)),
        EquipmentSlot.CHEST,
        List.of(entry(5, Items.NETHERITE_CHESTPLATE),
            entry(4, Items.DIAMOND_CHESTPLATE),
            entry(3, Items.IRON_CHESTPLATE),
            entry(2, Items.CHAINMAIL_CHESTPLATE),
            entry(1, Items.LEATHER_CHESTPLATE)),
        EquipmentSlot.LEGS,
        List.of(entry(5, Items.NETHERITE_LEGGINGS),
            entry(4, Items.DIAMOND_LEGGINGS),
            entry(3, Items.IRON_LEGGINGS),
            entry(2, Items.CHAINMAIL_LEGGINGS),
            entry(1, Items.LEATHER_LEGGINGS)),
        EquipmentSlot.FEET,
        List.of(entry(5, Items.NETHERITE_BOOTS), entry(4, Items.DIAMOND_BOOTS), entry(3, Items.IRON_BOOTS), entry(2, Items.CHAINMAIL_BOOTS), entry(1, Items.LEATHER_BOOTS))));

    /**
     * The equipment slot this armor type occupies.
     */
    private final EquipmentSlot slot;

    public ArmorEquipmentTypeEntry(final ResourceLocation registryName, final Component displayName, final EquipmentSlot slot)
    {
        super(registryName, displayName);
        this.slot = slot;
    }

    @Override
    protected boolean isEquipment(final ItemStack itemStack)
    {
        return itemStack.getItem() instanceof final ArmorItem armor && slot.equals(armor.getEquipmentSlot());
    }

    @Override
    protected int getLevel(final ItemStack itemStack)
    {
        final double targetValue = getArmorValue(itemStack);

        for (final Map.Entry<Integer, Item> reference : REFERENCE_LADDERS.get(slot))
        {
            if (targetValue >= getArmorValue(reference.getValue().getDefaultInstance()))
            {
                return Mth.clamp(reference.getKey(), 1, 5);
            }
        }
        return 1;
    }

    @Override
    @Nullable
    protected Integer getBlockRequirement(final BlockState state)
    {
        return null;
    }

    /**
     * Calculate the armor level for an item stack.
     * (Level is determined by taking the base armor rating, and 4 points for each toughness level.)
     *
     * @param itemStack the input item stack.
     * @return the armor value.
     */
    private static double getArmorValue(final ItemStack itemStack)
    {
        final double armor = getItemStackAttributeValue(itemStack, Attributes.ARMOR);
        final double toughness = getItemStackAttributeValue(itemStack, Attributes.ARMOR_TOUGHNESS);

        return armor + (toughness * 4);
    }

    /**
     * Get an attribute value for a given item stack.
     *
     * @param itemStack the input item stack.
     * @param attribute the attribute to get the value for.
     * @return the computed value of the attribute with all modifiers.
     */
    private static double getItemStackAttributeValue(final ItemStack itemStack, final Attribute attribute)
    {
        try
        {
            final AttributeInstance instance = new AttributeInstance(attribute, (f) -> {});
            itemStack.getAttributeModifiers(LivingEntity.getEquipmentSlotForItem(itemStack)).get(attribute).forEach(instance::addTransientModifier);
            return instance.getValue();
        }
        catch (final Exception e)
        {
            Log.getLogger().warn("Could not get attribute value for '{}' on item '{}'", attribute.getDescriptionId(), itemStack.getDescriptionId(), e);
            return 0;
        }
    }
}
