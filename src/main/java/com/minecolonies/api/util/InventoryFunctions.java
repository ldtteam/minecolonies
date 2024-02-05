package com.minecolonies.api.util;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.*;

/**
 * Java8 functional interfaces for inventories. Most methods will be remapping of parameters to reduce duplication. Because of erasure clashes, not
 * all combinations are supported.
 */
public final class InventoryFunctions
{
    /**
     * Private constructor to hide implicit one.
     */
    private InventoryFunctions()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Search for a stack in an Inventory matching the predicate.
     *
     * @param provider the provider to search in
     * @param tester   the function to use for testing slots
     * @return true if it found a stack
     */
    public static boolean matchFirstInProvider(final ICapabilityProvider provider, @NotNull final Predicate<ItemStack> tester)
    {
        return matchInProvider(provider, inv -> slot -> tester, true);
    }

    /**
     * Topmost function to actually loop over the provider. Will return if it found something.
     *
     * @param provider       the provider to loop over
     * @param tester         the function to use for testing slots
     * @param stopAfterFirst if it should stop executing after finding one stack that applies
     * @return true if it found a stack
     */
    private static boolean matchInProvider(
      @Nullable final ICapabilityProvider provider,
      @NotNull final Function<ICapabilityProvider, Function<Integer, Predicate<ItemStack>>> tester,
      final boolean stopAfterFirst)
    {
        if (provider == null)
        {
            return false;
        }

        boolean foundOne = false;
        for (final IItemHandler handler : InventoryUtils.getItemHandlersFromProvider(provider))
        {
            final int size = handler.getSlots();
            for (int slot = 0; slot < size; slot++)
            {
                final ItemStack stack = handler.getStackInSlot(slot);
                //Unchain the function and apply it
                if (tester.apply(provider).apply(slot).test(stack))
                {
                    foundOne = true;
                    if (stopAfterFirst)
                    {
                        return true;
                    }
                }
            }
        }

        return foundOne;
    }

    /**
     * Topmost matchFirst function, will stop after it finds the first
     * itemstack.
     *
     * @param provider the provider to search in
     * @param tester   the function to use for testing slots
     * @return true if it found a stack
     */
    /*
    private static boolean matchFirstInProvider(
                                                 final ICapabilityProvider provider,
                                                 @NotNull final Function<ICapabilityProvider, Function<Integer, Predicate<ItemStack>>> tester)
    {
        return matchInProvider(provider, tester, true);
    }
    */

    /**
     * Search for a stack in an Inventory matching the predicate.
     *
     * @param provider the provider to search in
     * @param tester   the function to use for testing slots
     * @param action   the function to use if a slot matches
     * @return true if it found a stack
     */
    public static boolean matchFirstInProviderWithAction(
      final ICapabilityProvider provider,
      @NotNull final Predicate<ItemStack> tester,
      @NotNull final IMatchActionResult action)
    {
        return matchInProvider(
          provider,
          inv -> slot -> stack ->
          {
              if (tester.test(stack))
              {
                  action.accept(provider, slot);
                  return true;
              }
              return false;
          },
          true);
    }

    /**
     * Search for a stack in an Inventory matching the predicate.
     *
     * @param itemHandler the handler to search in
     * @param tester      the function to use for testing slots
     * @param action      the function to use if a slot matches
     * @return true if it found a stack
     */
    public static boolean matchFirstInHandlerWithAction(
      @NotNull final IItemHandler itemHandler,
      @NotNull final Predicate<ItemStack> tester,
      @NotNull final IMatchActionResultHandler action)
    {
        return matchInHandler(
          itemHandler,
          inv -> slot -> stack ->
          {
              if (tester.test(stack))
              {
                  action.accept(itemHandler, slot);
                  return true;
              }
              return false;
          });
    }

    /**
     * Will return if it found something in the handler.
     *
     * @param handler the handler to check
     * @param tester  the function to use for testing slots
     * @return true if it found a stack
     */
    private static boolean matchInHandler(
      @Nullable final IItemHandler handler,
      @NotNull final Function<IItemHandler, Function<Integer, Predicate<ItemStack>>> tester)
    {
        if (handler == null)
        {
            return false;
        }

        final int size = handler.getSlots();
        for (int slot = 0; slot < size; slot++)
        {
            final ItemStack stack = handler.getStackInSlot(slot);
            //Unchain the function and apply it
            if (tester.apply(handler).apply(slot).test(stack))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Search for a stack in an Inventory matching the predicate.
     *
     * @param provider the provider to search in
     * @param tester   the function to use for testing slots
     * @param action   the function to use if a slot matches
     * @return true if it found a stack
     */
    public static boolean matchFirstInProviderWithSimpleAction(
      final ICapabilityProvider provider,
      @NotNull final Predicate<ItemStack> tester,
      @NotNull final Consumer<Integer> action)
    {
        return matchInProvider(
          provider,
          inv -> slot -> stack ->
          {
              if (tester.test(stack))
              {
                  action.accept(slot);
                  return true;
              }
              return false;
          },
          true);
    }

    /**
     * Search for a stack in an Inventory matching the predicate. (IInventory, Integer) -&gt; Boolean
     *
     * @param inventory the inventory to search in
     * @param tester    the function to use for testing slots
     * @return true if it found a stack
     */
    public static boolean matchFirstInProvider(final ICapabilityProvider inventory, @NotNull final BiPredicate<Integer, ItemStack> tester)
    {
        return matchInProvider(inventory, inv -> slot -> stack -> tester.test(slot, stack), true);
    }

    /**
     * Functional interface describing a Action that is executed ones a Match (the given ItemStack) is found in the given slot.
     */
    @FunctionalInterface
    public interface IMatchActionResult extends ObjIntConsumer<ICapabilityProvider>
    {
        /**
         * Method executed when a match has been found.
         *
         * @param provider  The itemstack that matches the predicate for the search.
         * @param slotIndex The slotindex in which this itemstack was found.
         */
        @Override
        void accept(ICapabilityProvider provider, int slotIndex);
    }

    /**
     * Functional interface describing a Action that is executed ones a Match (the given ItemStack) is found in the given slot.
     */
    @FunctionalInterface
    public interface IMatchActionResultHandler extends ObjIntConsumer<IItemHandler>
    {
        /**
         * Method executed when a match has been found.
         *
         * @param handler   The itemstack that matches the predicate for the search.
         * @param slotIndex The slotindex in which this itemstack was found.
         */
        @Override
        void accept(IItemHandler handler, int slotIndex);
    }
}
