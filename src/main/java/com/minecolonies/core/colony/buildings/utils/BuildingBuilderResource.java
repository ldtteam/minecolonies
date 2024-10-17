package com.minecolonies.core.colony.buildings.utils;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.core.client.gui.generic.ResourceItem.Resource;
import com.minecolonies.core.client.gui.generic.ResourceItem.ResourceAvailability;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Information about a resource. - How many are needed to finish the build - How many are available to the builder - How many are in the player's inventory (client side only)
 */
public class BuildingBuilderResource extends ItemStorage implements Resource
{
    private int amountAvailable;
    private int amountPlayer;

    /**
     * The amount currently being delivered
     */
    private int amountInDelivery = 0;

    /**
     * Constructor for a resource but with available items.
     *
     * @param stack     the stack.
     * @param amount    the amount.
     * @param available the amount available.
     */
    public BuildingBuilderResource(@NotNull final ItemStack stack, final int amount, final int available)
    {
        this(stack, amount);
        this.amountAvailable = available;
    }

    /**
     * Constructor for a resource.
     *
     * @param stack  the stack.
     * @param amount amount for this resource.
     */
    public BuildingBuilderResource(@NotNull final ItemStack stack, final int amount)
    {
        super(stack, amount, false);
        this.amountAvailable = 0;
        this.amountPlayer = 0;
    }

    /**
     * Add to the current amount available.
     *
     * @param amount to add to the current amount available
     */
    public void addAvailable(final int amount)
    {
        this.amountAvailable += amount;
    }

    @Override
    public String toString()
    {
        final int itemId = Item.getId(getItem());
        final int hashCode = getItemStack().hasTag() ? getItemStack().getTag().hashCode() : 0;
        return getName() + "(p:"
                 + amountPlayer + " a:"
                 + amountAvailable + " n:" + getAmount()
                 + " id=" + itemId
                 + " damage=" + getDamageValue() + "-"
                 + hashCode
                 + ") => " + getAvailabilityStatus().name();
    }

    @Override
    public Component getName()
    {
        //It is the bet way ?
        return Component.literal(getItemStack().getHoverName().getString());
    }

    @Override
    public List<ItemStack> getIcon()
    {
        return List.of(getItemStack().copyWithCount(1));
    }

    @Override
    public ResourceAvailability getAvailabilityStatus()
    {
        if (getAmount() > amountAvailable)
        {
            if (amountPlayer == 0)
            {
                if (amountInDelivery > 0)
                {
                    return ResourceAvailability.IN_DELIVERY;
                }
                return ResourceAvailability.DONT_HAVE;
            }
            if (amountPlayer < (getAmount() - amountAvailable))
            {
                if (amountInDelivery > 0)
                {
                    return ResourceAvailability.IN_DELIVERY;
                }
                return ResourceAvailability.NEED_MORE;
            }
            return ResourceAvailability.HAVE_ENOUGH;
        }
        return ResourceAvailability.NOT_NEEDED;
    }

    @Override
    public int hashCode()
    {
        return 31 * (31 * super.hashCode() + amountAvailable) + amountPlayer;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (super.equals(o))
        {
            final BuildingBuilderResource that = (BuildingBuilderResource) o;

            return this.getAmountAvailable() == that.getAmountAvailable() && this.getAmountPlayer() == that.getAmountPlayer();
        }

        return false;
    }

    /**
     * get the amount available for this resource.
     * <p>
     * i.e. amount in the chest + amount in the builder's inventory
     *
     * @return the amount available
     */
    @Override
    public int getAmountAvailable()
    {
        return amountAvailable;
    }

    /**
     * Setter for the available resource amount.
     *
     * @param amount this is the new amount available
     */
    public void setAvailable(final int amount)
    {
        amountAvailable = amount;
    }

    @Override
    public int getAmountPlayer()
    {
        return amountPlayer;
    }

    /**
     * set how the player have in its inventory.
     *
     * @param amount of items
     */
    public void setPlayerAmount(final int amount)
    {
        amountPlayer = amount;
    }

    @Override
    public int getAmountInDelivery()
    {
        return amountInDelivery;
    }

    public void setAmountInDelivery(final int amountInDelivery)
    {
        this.amountInDelivery = amountInDelivery;
    }
}