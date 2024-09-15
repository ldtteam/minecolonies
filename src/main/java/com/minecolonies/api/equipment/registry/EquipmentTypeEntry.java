package com.minecolonies.api.equipment.registry;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * An entry in the EquipmentType registry that defines the types of
 * equipment within the colony.
 */
public final class EquipmentTypeEntry
{
    /**
     * The registry identifier for this equipment type.
     */
    private final ResourceLocation registryName;

    /**
     * The component for the human readable name.
     */
    private final Component displayName;

    /**
     * Predicate to determine whether a given ItemStack
     * can act as this equipment type.
     */
    private final BiPredicate<ItemStack, EquipmentTypeEntry> isEquipment;

    /**
     * A function to return the integer item level of a
     * given ItemStack.
     */
    private final BiFunction<ItemStack, EquipmentTypeEntry, Integer> itemLevel;

    /**
     * Constructor.
     *
     * @param displayName  The human readable name of the equipment type
     * @param isEquipment  A predicate for determining if an itemstack is the equipment type
     * @param itemLevel    A function to return the item level of an item stack
     * @param registryName The forge registry location of the equipment type
     */
    private EquipmentTypeEntry(
      final Component displayName,
      final BiPredicate<ItemStack, EquipmentTypeEntry> isEquipment,
      final BiFunction<ItemStack, EquipmentTypeEntry, Integer> itemLevel,
      final ResourceLocation registryName)
    {
        this.displayName = displayName;
        this.isEquipment = isEquipment;
        this.itemLevel = itemLevel;
        this.registryName = registryName;
    }

    /**
     * Parse a resource location from a serialized version for equipmenttypes.
     * This is to help migrate to the new equipmenttype serialization which originally
     * used names and now uses resource locations.
     *
     * @param serialized The string representation of the equipment type
     * @return The correct resource location
     */
    public static ResourceLocation parseResourceLocation(final String serialized)
    {
        ResourceLocation result = new ResourceLocation(serialized);
        return parseResourceLocation(result);
    }

    /**
     * Parse a resource location from a serialized version for equipmenttypes.
     * This is to help migrate to the new equipmenttype serialization which originally
     * used names and now uses resource locations.
     *
     * @param serialized A resource location read from a buffer
     * @return The correct resource location
     */
    public static ResourceLocation parseResourceLocation(final ResourceLocation serialized)
    {
        // Minecraft will never register an equipment type with us so these are
        // the old non-resource-location serialized equipment types.
        if (serialized.getNamespace().equals("minecraft")) {
            return new ResourceLocation(Constants.MOD_ID, serialized.getPath());
        }

        return serialized;
    }

    /**
     * Get the name of the forge registry location for the equipment type
     *
     * @return The resource location
     */
    public ResourceLocation getRegistryName()
    {
        return registryName;
    }

    /**
     * Get the display name of the equipment type
     *
     * @return the component for the human readable name.
     */
    public Component getDisplayName()
    {
        return displayName;
    }

    /**
     * Determine whether an item stack works as this equipment.
     *
     * @param itemStack to test
     * @return Whether the item stack can act as the equipment.
     */
    public boolean checkIsEquipment(ItemStack itemStack)
    {
        return isEquipment.test(itemStack, this);
    }

    /**
     * Get the item level for this equipment type for a given item stack
     *
     * @param itemStack to test
     * @return The item level
     */
    public int getMiningLevel(ItemStack itemStack)
    {
        return isEquipment.test(itemStack, this) ? itemLevel.apply(itemStack, this) : -1;
    }

    /**
     * A builder that can construct new EquipmentTypeEntries.
     */
    public static class Builder
    {
        /**
         * The registry identifier for this equipment type.
         */
        private ResourceLocation registryName;

        /**
         * The component for the human readable name.
         */
        private Component displayName;

        /**
         * Predicate to determine whether a given ItemStack
         * can act as this equipment type.
         */
        private BiPredicate<ItemStack, EquipmentTypeEntry> isEquipment;

        /**
         * A function to return the integer item level of a
         * given ItemStack.
         */
        private BiFunction<ItemStack, EquipmentTypeEntry, Integer> itemLevel;

        /**
         * Set the registry identifier for this equipment type.
         *
         * @param registryName The registry identifier
         * @return this
         */
        public Builder setRegistryName(final ResourceLocation registryName)
        {
            this.registryName = registryName;
            return this;
        }

        /**
         * Set the display name for the new EquipmentTypeEntry
         *
         * @param displayName the new human readable name
         * @return this
         */
        public Builder setDisplayName(final Component displayName)
        {
            this.displayName = displayName;
            return this;
        }

        /**
         * Set the predicate for determining whether an item stack is the equipment type
         *
         * @param isEquipment The predicate
         * @return this
         */
        public Builder setIsEquipment(final BiPredicate<ItemStack, EquipmentTypeEntry> isEquipment)
        {
            this.isEquipment = isEquipment;
            return this;
        }

        /**
         * Set the function for getting the item level of an item stack for this tool type
         *
         * @param itemLevel The function
         * @return this
         */
        public Builder setEquipmentLevel(final BiFunction<ItemStack, EquipmentTypeEntry, Integer> itemLevel)
        {
            this.itemLevel = itemLevel;
            return this;
        }

        /**
         * Constructs the actual EquipmentTypeEntry
         *
         * @return the new EquipmentTypeEntry
         */
        public EquipmentTypeEntry build()
        {
            return new EquipmentTypeEntry(displayName, isEquipment, itemLevel, registryName);
        }
    }

    /**
     * The comparator used to compare two EquipmentTypeEntries. The names
     * are used for the comparison.
     */
    public static class Comparator implements java.util.Comparator<EquipmentTypeEntry>
    {
        public int compare(EquipmentTypeEntry o1, EquipmentTypeEntry o2)
        {
            return o1.registryName.compareTo(o2.registryName);
        }
    }
}
