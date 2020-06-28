package com.minecolonies.api.colony.requestsystem.requestable.deliveryman;

import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.NotNull;

/**
 * Class used to represent pickups inside the request system.
 * This class can be used to request a pickup of
 */
public class Pickup extends AbstractDeliverymanRequestable
{

    /**
     * Constructor for Delivery requests
     *
     * @param priority The priority of the request.
     */
    public Pickup(final int priority)
    {
        super(priority);
    }

    @NotNull
    public static CompoundNBT serialize(@NotNull final IFactoryController controller, final Pickup pickup)
    {
        final CompoundNBT compound = new CompoundNBT();
        compound.put(NBT_PRIORITY, controller.serialize(pickup.getPriority()));
        return compound;
    }

    @NotNull
    public static Pickup deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT compound)
    {
        final int priority = controller.deserialize(compound.getCompound(NBT_PRIORITY));
        return new Pickup(priority);
    }

    @Override
    public boolean equals(final Object o)
    {
        // Note that the super class will compare the priority.
        if (!super.equals(o))
        {
            return false;
        }
        if (this == o)
        {
            return true;
        }
        return o instanceof Pickup;
    }

    @Override
    public String toString()
    {
        return "Pickup{" +
                 "priority=" + priority +
                 '}';
    }
}
