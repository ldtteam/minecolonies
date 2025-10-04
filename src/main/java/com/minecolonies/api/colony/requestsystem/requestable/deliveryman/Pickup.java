package com.minecolonies.api.colony.requestsystem.requestable.deliveryman;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class used to represent pickups inside the request system. This class can be used to request a pickup of
 */
public class Pickup extends AbstractDeliverymanRequestable
{
    /**
     * Set of type tokens belonging to this class.
     */
    private final static Set<TypeToken<?>>
      TYPE_TOKENS = ReflectionUtils.getSuperClasses(TypeToken.of(Pickup.class)).stream().filter(type -> !type.equals(TypeConstants.OBJECT)).collect(Collectors.toSet());

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
    public static CompoundTag serialize(@NotNull final IFactoryController controller, final Pickup pickup)
    {
        final CompoundTag compound = new CompoundTag();
        compound.put(NBT_PRIORITY, controller.serialize(pickup.getPriority()));
        return compound;
    }

    @NotNull
    public static Pickup deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag compound)
    {
        final int priority = controller.deserialize(compound.getCompound(NBT_PRIORITY));
        return new Pickup(priority);
    }

    /**
     * Serialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the the buffer to write to.
     * @param input      the input to serialize.
     */
    public static void serialize(final IFactoryController controller, final FriendlyByteBuf buffer, final Pickup input)
    {
        buffer.writeInt(input.getPriority());
    }

    /**
     * Deserialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the buffer to read.
     * @return the deliverable.
     */
    public static Pickup deserialize(final IFactoryController controller, final FriendlyByteBuf buffer)
    {
        final int priority = buffer.readInt();

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

    @Override
    public Set<TypeToken<?>> getSuperClasses()
    {
        return TYPE_TOKENS;
    }
}
