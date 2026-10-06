package com.minecolonies.api.equipment.registry;

import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

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
     * The component for the human-readable name.
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
     * Function to determine both whether this equipment type is the correct (i.e. most appropriate) one
     * to use on a given BlockState, and if so, the equipment level required to harvest it. Returns null
     * if this equipment type isn't the correct one for the block. Not applicable to every equipment type
     * (e.g. armor, lead) - those simply never match any block.
     */
    private final BiFunction<BlockState, EquipmentTypeEntry, Integer> blockRequirement;

    /**
     * Additional predicates, registered by mod compat, that can mark an item stack as this equipment
     * type on top of whatever {@link #isEquipment} already covers. Checked in registration order;
     * the first match wins.
     */
    private final List<BiPredicate<ItemStack, EquipmentTypeEntry>> customPredicates = new ArrayList<>();

    /**
     * Additional level functions, registered by mod compat, that can report the equipment level of an
     * item stack of this equipment type. Checked in registration order, ahead of {@link #itemLevel};
     * the first non-null result wins. Returns null if not applicable to the given stack.
     */
    private final List<Function<ItemStack, Integer>> customLevelFunctions = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param displayName      the human-readable name of the equipment type
     * @param isEquipment      a predicate for determining if an itemstack is the equipment type
     * @param itemLevel        a function to return the item level of an item stack
     * @param blockRequirement a function returning the required equipment level for a given block, or null
     *                         if this equipment type isn't correct for it or isn't applicable to blocks
     * @param registryName     the forge registry location of the equipment type
     */
    private EquipmentTypeEntry(
      final Component displayName,
      final BiPredicate<ItemStack, EquipmentTypeEntry> isEquipment,
      final BiFunction<ItemStack, EquipmentTypeEntry, Integer> itemLevel,
      @Nullable final BiFunction<BlockState, EquipmentTypeEntry, Integer> blockRequirement,
      final ResourceLocation registryName)
    {
        this.displayName = displayName;
        this.isEquipment = isEquipment;
        this.itemLevel = itemLevel;
        this.blockRequirement = blockRequirement;
        this.registryName = registryName;
    }

    /**
     * Parse a resource location from a serialized version for EquipmentTypes.
     * This is to help migrate to the new EquipmentType serialization which originally
     * used names and now uses resource locations.
     *
     * @param serialized the string representation of the equipment type
     * @return the correct resource location
     */
    public static ResourceLocation parseResourceLocation(final String serialized)
    {
        ResourceLocation result = new ResourceLocation(serialized);
        return parseResourceLocation(result);
    }

    /**
     * Parse a resource location from a serialized version for EquipmentTypes.
     * This is to help migrate to the new EquipmentType serialization which originally
     * used names and now uses resource locations.
     *
     * @param serialized A resource location read from nbt
     * @return the correct resource location
     */
    public static ResourceLocation parseResourceLocation(final ResourceLocation serialized)
    {
        final String namespace = serialized.getNamespace().equals("minecraft") ? Constants.MOD_ID : serialized.getNamespace();
        final String path = serialized.getPath().isEmpty() ? ModEquipmentTypes.none.get().registryName.getPath() : serialized.getPath();
        return new ResourceLocation(namespace, path);
    }

    /**
     * Get the name of the forge registry location for the equipment type
     *
     * @return the resource location
     */
    public ResourceLocation getRegistryName()
    {
        return registryName;
    }

    /**
     * Get the display name of the equipment type
     *
     * @return the component for the human-readable name.
     */
    public Component getDisplayName()
    {
        return displayName;
    }

    /**
     * Determine whether an item stack works as this equipment.
     *
     * @param itemStack to test
     * @return whether the item stack can act as the equipment.
     */
    public boolean checkIsEquipment(ItemStack itemStack)
    {
        if (isEquipment.test(itemStack, this))
        {
            return true;
        }

        for (final BiPredicate<ItemStack, EquipmentTypeEntry> customPredicate : customPredicates)
        {
            if (customPredicate.test(itemStack, this))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine whether this equipment type is the correct one to use on a given block.
     * Not every equipment type is applicable to blocks (e.g. armor, lead) - those always return false here.
     *
     * @param state the block state to test.
     * @return whether this equipment type should be used on the given block.
     */
    public boolean isCorrectForBlock(final BlockState state)
    {
        return blockRequirement != null && blockRequirement.apply(state, this) != null;
    }

    /**
     * Get the equipment level required to harvest a given block with this equipment type.
     * Only call this once {@link #isCorrectForBlock(BlockState)} has confirmed this equipment type applies.
     *
     * @param state the block state to test.
     * @return the required equipment level, or -1 if this equipment type isn't correct for the block.
     */
    public int getRequiredLevelForBlock(final BlockState state)
    {
        if (blockRequirement == null)
        {
            return -1;
        }
        final Integer level = blockRequirement.apply(state, this);
        return level == null ? -1 : level;
    }

    /**
     * Get the item level for this equipment type for a given item stack
     *
     * @param itemStack to test
     * @return the item level
     */
    public int getMiningLevel(ItemStack itemStack)
    {
        for (final Function<ItemStack, Integer> customLevelFunction : customLevelFunctions)
        {
            final Integer customLevel = customLevelFunction.apply(itemStack);
            if (customLevel != null)
            {
                return Mth.clamp(customLevel, 0, 5);
            }
        }

        return checkIsEquipment(itemStack) ? Mth.clamp(itemLevel.apply(itemStack, this), 0, 5) : -1;
    }

    /**
     * Register an additional predicate that can mark an item stack as this equipment type, on top of
     * whatever the builder's own predicate already covers. Intended for mod compat hooks (e.g. a
     * Tinkers' Construct integration registering its modifiable tools as pickaxes).
     *
     * @param predicate the predicate to register. Should return false for stacks it doesn't recognize.
     */
    public void registerCustomPredicate(@NotNull final BiPredicate<ItemStack, EquipmentTypeEntry> predicate)
    {
        customPredicates.add(predicate);
    }

    /**
     * Register a specific item with a fixed equipment level for this equipment type.
     * Intended for mod compat hooks.
     *
     * @param item  the item to register.
     * @param level the equipment level to assign, in the range [0, 5].
     */
    public void registerCustomLevelFunction(@NotNull final Item item, final int level)
    {
        registerCustomLevelFunction(stack -> stack.is(item) ? level : null);
    }

    /**
     * Register a function that dynamically determines the equipment level for item stacks of this
     * equipment type. The function receives an item stack and returns its level, or null if not
     * applicable. Checked ahead of the builder's own level function; the first non-null result wins.
     * Intended for mod compat hooks.
     *
     * @param function a function mapping an item stack to its equipment level in the range [0, 5], or null.
     */
    public void registerCustomLevelFunction(@NotNull final Function<ItemStack, Integer> function)
    {
        customLevelFunctions.add(function);
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
         * The component for the human-readable name.
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
         * Function to determine the required equipment level for a given BlockState, or null if not
         * applicable/not correct for this equipment type.
         */
        @Nullable
        private BiFunction<BlockState, EquipmentTypeEntry, Integer> blockRequirement;

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
         * @param displayName the new human-readable name
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
         * @param isEquipment the predicate
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
         * @param itemLevel the function
         * @return this
         */
        public Builder setEquipmentLevel(final BiFunction<ItemStack, EquipmentTypeEntry, Integer> itemLevel)
        {
            this.itemLevel = itemLevel;
            return this;
        }

        /**
         * Set the function determining whether this equipment type is correct for a given block, and if so,
         * the equipment level required to harvest it. Only applicable to equipment types that can be used
         * on blocks (e.g. the vanilla diggers).
         *
         * @param blockRequirement the function, returning null if this equipment type isn't correct for the block
         * @return this
         */
        public Builder setBlockRequirement(final BiFunction<BlockState, EquipmentTypeEntry, Integer> blockRequirement)
        {
            this.blockRequirement = blockRequirement;
            return this;
        }

        /**
         * Constructs the actual EquipmentTypeEntry
         *
         * @return the new EquipmentTypeEntry
         */
        public EquipmentTypeEntry build()
        {
            return new EquipmentTypeEntry(displayName, isEquipment, itemLevel, blockRequirement, registryName);
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
