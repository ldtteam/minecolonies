package com.minecolonies.coremod.util;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Java8 functional interfaces for {@link net.minecraft.inventory.IInventory}
 * Most methods will be remapping of parameters to reduce duplication.
 * Because of erasure clashes, not all combinations are supported.
 */
public class InventoryFunctions
{

    /**
     * A NOOP Consumer to use for any function.
     *
     * @param o will be consumed and ignored
     */
    public static void doNothing(final Object... o)
    {
        //Intentionally left blank to do nothing.
    }

    /**
     * Search for a stack in an Inventory matching the predicate.
     *
     * @param inventory the inventory to search in
     * @param tester    the function to use for testing slots
     * @param action    the function to use if a slot matches
     * @return true if it found a stack
     */
    public static boolean matchFirstInInventory(
                                                 final IInventory inventory, @NotNull final Predicate<ItemStack> tester,
                                                 @NotNull final Consumer<Integer> action)
    {
        return matchFirstInInventory(inventory, inv -> slot -> stack ->
        {
            if (tester.test(stack))
            {
                action.accept(slot);
                return true;
            }
            return false;
        });
    }

    /**
     * Search for a stack in an Inventory matching the predicate.
     *
     * @param inventory the inventory to search in
     * @param tester    the function to use for testing slots
     * @param action    the function to use if a slot matches
     * @return true if it found a stack
     */
    public static boolean matchFirstInInventoryWithInventory(
            final IInventory inventory, @NotNull final Predicate<ItemStack> tester,
            @NotNull final Consumer<Tuple<Integer, IInventory>> action)
    {
        return matchFirstInInventory(inventory, inv -> slot -> stack ->
        {
            if (tester.test(stack))
            {
                action.accept(new Tuple<>(slot, inventory));
                return true;
            }
            return false;
        });
    }

    /**
     * Topmost matchFirst function, will stop after it finds the first itemstack.
     *
     * @param inventory the inventory to search in
     * @param tester    the function to use for testing slots
     * @return true if it found a stack
     */
    private static boolean matchFirstInInventory(
                                                  final IInventory inventory, @NotNull final Function<IInventory, Function<Integer,
                                                                                                                            Predicate<ItemStack>>> tester)
    {
        return matchInInventory(inventory, tester, true);
    }

    /**
     * Topmost function to actually loop over the inventory.
     * Will return if it found something.
     *
     * @param inventory      the inventory to loop over
     * @param tester         the function to use for testing slots
     * @param stopAfterFirst if it should stop executing after finding one stack that applies
     * @return true if it found a stack
     */
    private static boolean matchInInventory(
                                             @Nullable final IInventory inventory,
                                             @NotNull final Function<IInventory, Function<Integer,
                                                                                           Predicate<ItemStack>>> tester,
                                             final boolean stopAfterFirst)
    {
        if (inventory == null)
        {
            return false;
        }
        final int size = inventory.getSizeInventory();
        boolean foundOne = false;
        for (int slot = 0; slot < size; slot++)
        {
            final ItemStack stack = inventory.getStackInSlot(slot);
            //Unchain the function and apply it
            if (tester.apply(inventory).apply(slot).test(stack))
            {
                foundOne = true;
                if (stopAfterFirst)
                {
                    return true;
                }
            }
        }
        return foundOne;
    }

    /**
     * Search for a stack in an Inventory matching the predicate.
     * (IInventory, Integer) -&gt; Boolean
     *
     * @param inventory the inventory to search in
     * @param tester    the function to use for testing slots
     * @return true if it found a stack
     */
    public static boolean matchFirstInInventory(final IInventory inventory, @NotNull final BiPredicate<Integer, ItemStack> tester)
    {
        return matchFirstInInventory(inventory, inv -> slot -> stack -> tester.test(slot, stack));
    }
}
