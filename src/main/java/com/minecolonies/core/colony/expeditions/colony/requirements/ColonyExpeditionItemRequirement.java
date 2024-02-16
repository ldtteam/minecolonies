package com.minecolonies.core.colony.expeditions.colony.requirements;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

/**
 * Colony expedition requirements for providing any kind of item, with a minimum amount.
 */
public class ColonyExpeditionItemRequirement extends ColonyExpeditionRequirement
{
    /**
     * The item to request.
     */
    private final Item item;

    /**
     * The minimum amount to fulfill this requirement.
     */
    private final int amount;

    /**
     * Default constructor.
     *
     * @param item   the item to request.
     * @param amount the minimum amount.
     */
    public ColonyExpeditionItemRequirement(final Item item, final int amount)
    {
        this.item = item;
        this.amount = amount;
    }

    @Override
    @NotNull
    public ResourceLocation getId()
    {
        return new ResourceLocation(Constants.MOD_ID, "item/" + item.getDescriptionId());
    }

    @Override
    public RequirementHandler createHandler(final InventorySupplier inventory)
    {
        return new ItemRequirementHandler(inventory);
    }

    /**
     * Item handler instance used for verifying if the given item handler contains the exact item.
     */
    public class ItemRequirementHandler extends RequirementHandler
    {
        /**
         * Default constructor.
         *
         * @param inventory supplier for obtaining the inventory.
         */
        private ItemRequirementHandler(final InventorySupplier inventory)
        {
            super(inventory);
        }

        @Override
        public ResourceLocation getId()
        {
            return ColonyExpeditionItemRequirement.this.getId();
        }

        @Override
        public Predicate<ItemStack> getItemPredicate()
        {
            return stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, item.getDefaultInstance());
        }

        @Override
        public ItemStack getDefaultItemStack()
        {
            return item.getDefaultInstance();
        }

        @Override
        public Component getName()
        {
            return Component.translatable("com.minecolonies.core.expedition.gui.items.requirement.type.item", item.getDescription());
        }

        @Override
        public List<ItemStack> getIcon()
        {
            return List.of(new ItemStack(item));
        }

        @Override
        public int getAmount()
        {
            return amount;
        }
    }
}