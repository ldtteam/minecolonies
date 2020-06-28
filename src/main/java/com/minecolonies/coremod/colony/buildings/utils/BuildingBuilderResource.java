package com.minecolonies.coremod.colony.buildings.utils;

import com.minecolonies.api.crafting.ItemStorage;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Information about a resource.
 * - How many are needed to finish the build
 * - How many are available to the builder
 * - How many are in the player's inventory (client side only)
 */
public class BuildingBuilderResource extends ItemStorage
{
    private int amountAvailable;
    private int amountPlayer;
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

    /**
     * get how much more is needed from the player.
     * <p>
     * This is taking the builder's inventory + chest into account and the player inventory
     * Negative number is when the player does not have enough
     * Negative number is when the player does not more than enough
     *
     * @return the amount needed
     */
    public int getMissingFromPlayer()
    {
        return amountPlayer + amountAvailable - getAmount();
    }

    @Override
    public String toString()
    {
        final int itemId = Item.getIdFromItem(getItem());
        final int hashCode = getItemStack().hasTag() ? getItemStack().getTag().hashCode() : 0;
        return getName() + "(p:"
                + amountPlayer + " a:"
                + amountAvailable + " n:" + getAmount()
                + " id=" + itemId
                + " damage=" + getDamageValue() +  "-"
                + hashCode
                + ") => " + getAvailabilityStatus().name();
    }

    public String getName()
    {
        //It is the bet way ?
        return getItemStack().getDisplayName().getFormattedText();
    }

    public RessourceAvailability getAvailabilityStatus()
    {
        if (getAmount() > amountAvailable)
        {
            if (amountPlayer == 0)
            {
                return RessourceAvailability.DONT_HAVE;
            }
            if (amountPlayer < (getAmount() - amountAvailable))
            {
                return RessourceAvailability.NEED_MORE;
            }
            return RessourceAvailability.HAVE_ENOUGH;
        }
        return RessourceAvailability.NOT_NEEDED;
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

            return this.getAvailable() == that.getAvailable() && this.getPlayerAmount() == that.getPlayerAmount();
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
    public int getAvailable()
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

    /**
     * get how the player have in its inventory.
     *
     * @return the amount
     */
    public int getPlayerAmount()
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

    /**
     * Availability status of the resource.
     * according to the builder's chest, inventory and the player's inventory
     */
    public enum RessourceAvailability
    {
        NOT_NEEDED,
        DONT_HAVE,
        NEED_MORE,
        HAVE_ENOUGH
    }

    /**
     * Comparator class for BuildingBuilderResource.
     * <p>
     * This is use in the gui to order the list of resources needed.
     */
    public static class ResourceComparator implements Comparator<BuildingBuilderResource>, Serializable
    {
        private static final long serialVersionUID = 1;

        /**
         * Compare to resource together.
         * <p>
         * We want the item availalable in the player inventory first and the one not needed last
         * In alphabetical order otherwise
         */
        @Override
        public int compare(final BuildingBuilderResource resource1, final BuildingBuilderResource resource2)
        {
            if (resource1.getAvailabilityStatus() == resource2.getAvailabilityStatus())
            {
                return resource1.getName().compareTo(resource2.getName());
            }

            return resource2.getAvailabilityStatus().compareTo(resource1.getAvailabilityStatus());
        }
    }
}