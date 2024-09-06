package com.minecolonies.api.tools.registry;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * An entry in the ToolType registry that defines the types of
 * tools within the colony.
 */
public final class ToolTypeEntry
{
    /**
     * The registry identifier for this tool type.
     */
    private final ResourceLocation registryName;

    /**
     * The component for the human readable name.
     */
    private final Component displayName;

    /**
     * Predicate to determine whether a given ItemStack
     * can act as this tool type.
     */
    private final BiPredicate<ItemStack, ToolTypeEntry> isTool;

    /**
     * A function to return the integer item level of a
     * given ItemStack.
     */
    private final BiFunction<ItemStack, ToolTypeEntry, Integer> itemLevel;

    /**
     * Constructor.
     *
     * @param displayName  The human readable name of the tool type
     * @param isTool       A predicate for determining if an itemstack is the tool type
     * @param itemLevel    A function to return the item level of an item stack
     * @param registryName The forge registry location of the tool type
     */
    private ToolTypeEntry(final Component displayName, final BiPredicate<ItemStack, ToolTypeEntry> isTool, final BiFunction<ItemStack, ToolTypeEntry, Integer> itemLevel, final ResourceLocation registryName)
    {
        this.displayName = displayName;
        this.isTool = isTool;
        this.itemLevel = itemLevel;
        this.registryName = registryName;
    }

    /**
     * Get the name of the forge registry location for the tool type
     *
     * @return The resource location
     */
    public ResourceLocation getRegistryName()
    {
        return registryName;
    }

    /**
     * Get the display name of the tool type
     *
     * @return the component for the human readable name.
     */
    public Component getDisplayName()
    {
        return displayName;
    }

    /**
     * Determine whether an item stack works as this tool.
     *
     * @param itemStack to test
     * @return Whether the item stack can act as the tool.
     */
    public boolean checkIsTool(ItemStack itemStack)
    {
        return isTool.test(itemStack, this);
    }

    /**
     * Get the item level for this tool type for a given item stack
     *
     * @param itemStack to test
     * @return The item level
     */
    public int getMiningLevel(ItemStack itemStack)
    {
        return isTool.test(itemStack, this) ? itemLevel.apply(itemStack, this) : -1;
    }

    /**
     * A builder that can construct new ToolTypeEntries.
     */
    public static class Builder
    {
        /**
         * The registry identifier for this tool type.
         */
        private ResourceLocation registryName;

        /**
         * The component for the human readable name.
         */
        private Component displayName;

        /**
         * Predicate to determine whether a given ItemStack
         * can act as this tool type.
         */
        private BiPredicate<ItemStack, ToolTypeEntry> isTool;

        /**
         * A function to return the integer item level of a
         * given ItemStack.
         */
        private BiFunction<ItemStack, ToolTypeEntry, Integer> itemLevel;

        /**
         * Set the registry identifier for this tool type.
         *
         * @param registryName The registry identifier
         * @return this
         */
        public Builder setRegistryName(final ResourceLocation registryName) {
            this.registryName = registryName;
            return this;
        }

        /**
         * Set the display name for the new ToolTypeEntry
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
         * Set the predicate for determining whether an item stack is the tool type
         *
         * @param isTool The predicate
         * @return this
         */
        public Builder setIsTool(final BiPredicate<ItemStack, ToolTypeEntry> isTool)
        {
            this.isTool = isTool;
            return this;
        }

        /**
         * Set the function for getting the item level of an item stack for this tool type
         *
         * @param itemLevel The function
         * @return this
         */
        public Builder setToolLevel(final BiFunction<ItemStack, ToolTypeEntry, Integer> itemLevel)
        {
            this.itemLevel = itemLevel;
            return this;
        }

        /**
         * Constructs the actual ToolTypeEntry
         *
         * @return the new ToolTypeEntry
         */
        public ToolTypeEntry build()
        {
            return new ToolTypeEntry(displayName, isTool, itemLevel, registryName);
        }
    }

    /**
     * The comparator used to compare two ToolTypeEntries. The names
     * are used for the comparison.
     */
    public static class Comparator implements java.util.Comparator<ToolTypeEntry> {
        public int compare(ToolTypeEntry o1, ToolTypeEntry o2) {
            return o1.registryName.compareTo(o2.registryName);
        }
    }

    /**
     * Parse a resource location from a serialized version for tooltypes.
     * This is to help migrate to the new tooltype serialization which originally
     * used names and now uses resource locations.
     *
     * @param serialized The string representation of the tool type
     * @return The correct resource location
     */
    public static ResourceLocation parseResourceLocation(final String serialized) {
        ResourceLocation result = new ResourceLocation(serialized);
        return parseResourceLocation(result);
    }

    /**
     * Parse a resource location from a serialized version for tooltypes.
     * This is to help migrate to the new tooltype serialization which originally
     * used names and now uses resource locations.
     *
     * @param serialized A resource location read from a buffer
     * @return The correct resource location
     */
    public static ResourceLocation parseResourceLocation(final ResourceLocation serialized) {
        // Minecraft will never register a tool with us so these are
        // the old non-resource-location serialized tool types.
        if (serialized.getNamespace().equals("minecraft")) {
            return new ResourceLocation(Constants.MOD_ID, serialized.getPath());
        }

        return serialized;
    }
}
