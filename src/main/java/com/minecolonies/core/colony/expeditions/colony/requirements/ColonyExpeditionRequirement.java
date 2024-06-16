package com.minecolonies.core.colony.expeditions.colony.requirements;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.core.client.gui.generic.ResourceItem.Resource;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Interface for defining different types of colony expedition requirements.
 */
public abstract class ColonyExpeditionRequirement
{
    /**
     * Get a unique ID for this requirement.
     *
     * @return the resource location.
     */
    @NotNull
    public abstract ResourceLocation getId();

    /**
     * Get the minimum amount to fulfill this requirement.
     *
     * @return the amount.
     */
    public abstract int getAmount();

    /**
     * Create the handler for verifying if the given inventory satisfies this requirement.
     *
     * @param inventory the item handler instance.
     * @return the handler instance.
     */
    public abstract RequirementHandler createHandler(final InventorySupplier inventory);

    /**
     * Supplier for providing the reference to the inventory to check this requirement for.
     */
    @FunctionalInterface
    public interface InventorySupplier extends Supplier<IItemHandler>
    {

    }

    /**
     * Handler instance used for verifying if the given item handler satisfies the requirements.
     */
    public abstract static class RequirementHandler implements Resource
    {
        /**
         * Supplier for obtaining the inventory.
         */
        private final InventorySupplier inventory;

        /**
         * Default constructor.
         *
         * @param inventory supplier for obtaining the inventory.
         */
        protected RequirementHandler(final InventorySupplier inventory)
        {
            this.inventory = inventory;
        }

        /**
         * Get a unique ID for this requirement handler, must match the one provided by the {@link ColonyExpeditionRequirement}.
         *
         * @return the resource location.
         */
        public abstract ResourceLocation getId();

        /**
         * Get the predicate used to filter out items from the inventory.
         *
         * @return the predicate function.
         */
        public abstract Predicate<ItemStack> getItemPredicate();

        /**
         * Get the default item stack to provide the visitor with in case they are getting items creatively inserted.
         */
        public abstract ItemStack getDefaultItemStack();

        @Override
        public final int getAmountAvailable()
        {
            return InventoryUtils.getItemCountInItemHandler(inventory.get(), getItemPredicate());
        }

        @Override
        public final int getAmountPlayer()
        {
            return InventoryUtils.getItemCountInItemHandler(getPlayerInventory(), getItemPredicate());
        }

        @Override
        public final int getAmountInDelivery()
        {
            return 0;
        }

        /**
         * Get the player inventory item handler.
         *
         * @return the item handler instance.
         */
        private IItemHandler getPlayerInventory()
        {
            return new InvWrapper(Minecraft.getInstance().player.getInventory());
        }
    }
}