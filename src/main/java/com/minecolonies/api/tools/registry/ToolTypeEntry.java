package com.minecolonies.api.tools.registry;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.tools.ModToolTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;
import java.util.function.Predicate;

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
     * The name of the tool type.
     */
    private final String name;

    /**
     * The component for the human readable name.
     */
    private final Component displayName;

    /**
     * Predicate to determine whether a given ItemStack
     * can act as this tool type.
     */
    private final Predicate<ItemStack> isTool;

    /**
     * A function to return the integer item level of a
     * given ItemStack.
     */
    private final Function<ItemStack, Integer> itemLevel;

    /**
     * Constructor.
     *
     * @param name        The name of the tool type
     * @param displayName The human readable name of the tool type
     * @param isTool      A predicate for determining if an itemstack is the tool type
     * @param itemLevel   A function to return the item level of an item stack
     */
    private ToolTypeEntry(final String name, final Component displayName, final Predicate<ItemStack> isTool, final Function<ItemStack, Integer> itemLevel, final ResourceLocation registryName)
    {
        this.name = name;
        this.displayName = displayName;
        this.isTool = isTool;
        this.itemLevel = itemLevel;
        this.registryName = registryName;
    }

    public ResourceLocation getRegistryName()
    {
        return registryName;
    }

    /**
     * @return The tool type's name
     */
    public String getName()
    {
        return name;
    }

    /**
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
        return isTool.test(itemStack);
    }

    /**
     * Get the item level for this tool type for a given item stack
     *
     * @param itemStack to test
     * @return The item level
     */
    public int getMiningLevel(ItemStack itemStack)
    {
        return isTool.test(itemStack) ? itemLevel.apply(itemStack) : -1;
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
         * The name of the tool type.
         */
        private String name;

        /**
         * The component for the human readable name.
         */
        private Component displayName;

        /**
         * Predicate to determine whether a given ItemStack
         * can act as this tool type.
         */
        private Predicate<ItemStack> isTool;

        /**
         * A function to return the integer item level of a
         * given ItemStack.
         */
        private Function<ItemStack, Integer> itemLevel;

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
         * Set the name for the new ToolTypeEntry
         *
         * @param name the new name
         * @return this
         */
        public Builder setName(final String name)
        {
            this.name = name;
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
        public Builder setIsTool(final Predicate<ItemStack> isTool)
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
        public Builder setToolLevel(final Function<ItemStack, Integer> itemLevel)
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
            return new ToolTypeEntry(name, displayName, isTool, itemLevel, registryName);
        }
    }

    /**
     * The comparator used to compare two ToolTypeEntries. The names
     * are used for the comparison.
     */
    public static class Comparator implements java.util.Comparator<ToolTypeEntry> {
        public int compare(ToolTypeEntry o1, ToolTypeEntry o2) {
            return o1.name.compareTo(o2.name);
        }
    }
}
