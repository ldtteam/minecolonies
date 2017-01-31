package com.minecolonies.coremod.colony.buildings;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Information about a resource.
 * - How many are needed to finish the build
 * - How many are available to the builder
 * - How many are in the player's inventory (client side only)
 */
public class BuildingBuilderResource
{
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

    private       int       amountAvailable;
    private       int       amountPlayer;
    private final String    name;
    private final ItemStack itemStack;
    private final int       amountNeeded;

    /**
     * Constructor for BuildingBuilderResource.
     *
     * @param name of the resource.
     * @param itemStack stack for that resource.
     * @param amountAvailable amount of resource available  in the builder's chest + inventory.
     * @param amountNeeded amount required for the builder to finish the build.
     */
    public BuildingBuilderResource(final String name, final ItemStack itemStack, final int amountAvailable, final int amountNeeded)
    {
        this.name = name;
        this.itemStack = itemStack;
        this.amountAvailable = amountAvailable;
        this.amountNeeded = amountNeeded;
        this.amountPlayer = 0;
    }

    public String getName()
    {
        return name;
    }

    public ItemStack getItemStack()
    {
        return itemStack;
    }

    /**
     * get the amount available for this resource.
     * i.e. amount in the chest + amount in the builder's inventory
     */
    public int getAvailable()
    {
        return amountAvailable;
    }

    public void setAvailable(final int amount)
    {
        amountAvailable = amount;
    }

    public int getNeeded()
    {
        return amountNeeded;
    }

    public void setPlayerAmount(final int amount)
    {
        amountPlayer = amount;
    }

    public int getPlayerAmount()
    {
        return amountPlayer;
    }

    public RessourceAvailability getAvailabilityStatus()
    {
        if (amountNeeded > amountAvailable)
        {
            if (amountPlayer == 0)
            {
                return RessourceAvailability.DONT_HAVE;
            }
            if (amountPlayer < (amountNeeded-amountAvailable))
            {
                return RessourceAvailability.NEED_MORE;
            }
            return RessourceAvailability.HAVE_ENOUGH;
        }
        return RessourceAvailability.NOT_NEEDED;
    }

    @Override
    public String toString()
    {
        final int itemId= Item.getIdFromItem(itemStack.getItem());
        final int damage=itemStack.getItemDamage();
        return name + "(p:"+amountPlayer+" a:" +amountAvailable+" n:"+amountNeeded+" id="+itemId+" damage="+damage+") => "+getAvailabilityStatus().name();
    }


    /**
     * Comparator class for BuildingBuilderResource.
     *
     * This is use in the gui to order the list of resources needed.
     */
    public static class ResourceComparator implements Comparator<BuildingBuilderResource>, Serializable
    {
        private static final long serialVersionUID = 1;

        /**
         * Compare to resource together.
         *
         * We want the item availalable in the player inventory first and the one not needed last
         * In alphabetical order otherwise
         */
        @Override
        public int compare(BuildingBuilderResource resource1, BuildingBuilderResource resource2)
        {
            if  (resource1.getAvailabilityStatus()==resource2.getAvailabilityStatus())
            {
                return resource1.getName().compareTo(resource2.getName());
            }

            return resource2.getAvailabilityStatus().compareTo(resource1.getAvailabilityStatus());
        }
    }

}
