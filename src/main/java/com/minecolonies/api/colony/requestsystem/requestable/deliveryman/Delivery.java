package com.minecolonies.api.colony.requestsystem.requestable.deliveryman;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class used to represent deliveries inside the request system. This class can be used to request a getDelivery of a given ItemStack from a source to a target.
 */
public class Delivery extends AbstractDeliverymanRequestable
{
    /**
     * Set of type tokens belonging to this class.
     */
    private final static Set<TypeToken<?>>
      TYPE_TOKENS = ReflectionUtils.getSuperClasses(TypeToken.of(Delivery.class)).stream().filter(type -> !type.equals(TypeConstants.OBJECT)).collect(Collectors.toSet());

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_START  = "Start";
    private static final String NBT_TARGET = "Target";
    private static final String NBT_STACK  = "Stack";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    private final ILocation start;
    @NotNull
    private final ILocation target;
    @NotNull
    private final ItemStack stack;

    /**
     * Constructor for Delivery requests
     *
     * @param start    The location of the source inventory
     * @param target   The location of the target inventory
     * @param stack    The stack to be delivered
     * @param priority The priority of the request
     */
    public Delivery(@NotNull final ILocation start, @NotNull final ILocation target, @NotNull final ItemStack stack, final int priority)
    {
        super(priority);
        this.start = start;
        this.target = target;
        this.stack = stack;
    }

    @NotNull
    public static CompoundTag serialize(@NotNull final IFactoryController controller, final Delivery delivery)
    {
        final CompoundTag compound = new CompoundTag();

        compound.put(NBT_START, controller.serialize(delivery.getStart()));
        compound.put(NBT_TARGET, controller.serialize(delivery.getTarget()));
        compound.put(NBT_STACK, delivery.getStack().save(new CompoundTag()));
        compound.put(NBT_PRIORITY, controller.serialize(delivery.getPriority()));

        return compound;
    }

    @NotNull
    public static Delivery deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag compound)
    {
        final ILocation start = controller.deserialize(compound.getCompound(NBT_START));
        final ILocation target = controller.deserialize(compound.getCompound(NBT_TARGET));
        final ItemStack stack = ItemStackUtils.deserializeFromNBT(compound.getCompound(NBT_STACK));
        final int priority = controller.deserialize(compound.getCompound(NBT_PRIORITY));

        return new Delivery(start, target, stack, priority);
    }

    /**
     * Serialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the the buffer to write to.
     * @param input      the input to serialize.
     */
    public static void serialize(final IFactoryController controller, final FriendlyByteBuf buffer, final Delivery input)
    {
        controller.serialize(buffer, input.getStart());
        controller.serialize(buffer, input.getTarget());
        buffer.writeItem(input.getStack());
        buffer.writeInt(input.getPriority());
    }

    /**
     * Deserialize the deliverable.
     *
     * @param controller the controller.
     * @param buffer     the buffer to read.
     * @return the deliverable.
     */
    public static Delivery deserialize(final IFactoryController controller, final FriendlyByteBuf buffer)
    {
        final ILocation start = controller.deserialize(buffer);
        final ILocation target = controller.deserialize(buffer);
        final ItemStack stack = buffer.readItem();
        final int priority = buffer.readInt();

        return new Delivery(start, target, stack, priority);
    }

    @NotNull
    public ILocation getStart()
    {
        return start;
    }

    @NotNull
    public ILocation getTarget()
    {
        return target;
    }

    @NotNull
    public ItemStack getStack()
    {
        return stack;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (!super.equals(o))
        {
            return false;
        }
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Delivery))
        {
            return false;
        }

        final Delivery delivery = (Delivery) o;

        if (!getStart().equals(delivery.getStart()))
        {
            return false;
        }
        if (!getTarget().equals(delivery.getTarget()))
        {
            return false;
        }
        return ItemStackUtils.compareItemStacksIgnoreStackSize(getStack(), delivery.getStack());
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + getStart().hashCode();
        result = 31 * result + getTarget().hashCode();
        result = 31 * result + getStack().hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "Delivery{" +
                 "start=" + start +
                 ", target=" + target +
                 ", stack=" + stack +
                 ", priority=" + priority +
                 '}';
    }

    @Override
    public Set<TypeToken<?>> getSuperClasses()
    {
        return TYPE_TOKENS;
    }
}
