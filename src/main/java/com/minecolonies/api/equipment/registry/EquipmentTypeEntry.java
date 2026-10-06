package com.minecolonies.api.equipment.registry;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * An entry in the EquipmentType registry that defines the types of equipment within the colony. Concrete
 * equipment types are implemented as subclasses, one per family of behavior (e.g. diggers, armor,
 * durability-based items), rather than being assembled from builder-supplied lambdas.
 */
public abstract class EquipmentTypeEntry
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
     * Additional predicates, registered by mod compat, that can mark an item stack as this equipment
     * type on top of whatever {@link #isEquipment(ItemStack)} already covers. Checked in registration
     * order; the first match wins.
     */
    private final List<BiPredicate<ItemStack, EquipmentTypeEntry>> customPredicates = new ArrayList<>();

    /**
     * Additional level functions, registered by mod compat, that can report the equipment level of an
     * item stack of this equipment type. Checked in registration order, ahead of {@link #getLevel(ItemStack)};
     * the first non-null result wins. Returns null if not applicable to the given stack.
     */
    private final List<Function<ItemStack, Integer>> customLevelFunctions = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param registryName the forge registry location of the equipment type
     * @param displayName  the human-readable name of the equipment type
     */
    protected EquipmentTypeEntry(final ResourceLocation registryName, final Component displayName)
    {
        this.registryName = registryName;
        this.displayName = displayName;
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
        return new ResourceLocation(namespace, serialized.getPath());
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
     * Determine whether a given item stack can act as this equipment type.
     *
     * @param itemStack to test
     * @return whether the item stack can act as the equipment.
     */
    protected abstract boolean isEquipment(ItemStack itemStack);

    /**
     * Get the equipment level of a given item stack. Only called once {@link #isEquipment(ItemStack)} (or
     * a registered custom predicate) has confirmed the stack is this equipment type. Implementations are
     * responsible for clamping to their own family's valid range.
     *
     * @param itemStack to test
     * @return the equipment level.
     */
    protected abstract int getLevel(ItemStack itemStack);

    /**
     * Determine whether this equipment type is the correct one to use on a given block, and if so, the
     * equipment level required to harvest it. Not every equipment type is applicable to blocks (e.g.
     * armor) - those should always return null here.
     *
     * @param state the block state to test.
     * @return the required equipment level, or null if this equipment type isn't correct for the block.
     */
    @Nullable
    protected abstract Integer getBlockRequirement(BlockState state);

    /**
     * Determine whether an item stack works as this equipment.
     *
     * @param itemStack to test
     * @return whether the item stack can act as the equipment.
     */
    public final boolean checkIsEquipment(ItemStack itemStack)
    {
        if (isEquipment(itemStack))
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
    public final boolean isCorrectForBlock(final BlockState state)
    {
        return getBlockRequirement(state) != null;
    }

    /**
     * Get the equipment level required to harvest a given block with this equipment type.
     * Only call this once {@link #isCorrectForBlock(BlockState)} has confirmed this equipment type applies.
     *
     * @param state the block state to test.
     * @return the required equipment level, or -1 if this equipment type isn't correct for the block.
     */
    public final int getRequiredLevelForBlock(final BlockState state)
    {
        final Integer level = getBlockRequirement(state);
        return level == null ? -1 : level;
    }

    /**
     * Get the item level for this equipment type for a given item stack
     *
     * @param itemStack to test
     * @return the item level
     */
    public final int getMiningLevel(ItemStack itemStack)
    {
        for (final Function<ItemStack, Integer> customLevelFunction : customLevelFunctions)
        {
            final Integer customLevel = customLevelFunction.apply(itemStack);
            if (customLevel != null)
            {
                return Mth.clamp(customLevel, 0, 5);
            }
        }

        return checkIsEquipment(itemStack) ? Mth.clamp(getLevel(itemStack), 0, 5) : -1;
    }

    /**
     * Register an additional predicate that can mark an item stack as this equipment type, on top of
     * whatever this equipment type's own behavior already covers. Intended for mod compat hooks (e.g. a
     * Tinkers' Construct integration registering its modifiable tools as pickaxes).
     *
     * @param predicate the predicate to register. Should return false for stacks it doesn't recognize.
     */
    public final void registerCustomPredicate(@NotNull final BiPredicate<ItemStack, EquipmentTypeEntry> predicate)
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
    public final void registerCustomLevelFunction(@NotNull final Item item, final int level)
    {
        registerCustomLevelFunction(stack -> stack.is(item) ? level : null);
    }

    /**
     * Register a function that dynamically determines the equipment level for item stacks of this
     * equipment type. The function receives an item stack and returns its level, or null if not
     * applicable. Checked ahead of this equipment type's own level function; the first non-null result wins.
     * Intended for mod compat hooks.
     *
     * @param function a function mapping an item stack to its equipment level in the range [0, 5], or null.
     */
    public final void registerCustomLevelFunction(@NotNull final Function<ItemStack, Integer> function)
    {
        customLevelFunctions.add(function);
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
