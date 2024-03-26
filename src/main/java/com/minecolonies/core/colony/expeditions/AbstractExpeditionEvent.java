package com.minecolonies.core.colony.expeditions;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.colony.expeditions.IExpedition;
import net.minecraft.nbt.CompoundTag;

import java.util.function.Function;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_EVENT_ID;

/**
 * Abstract implementation for expedition related events.
 */
public abstract class AbstractExpeditionEvent implements IColonyEvent
{
    /**
     * The colony this event is for.
     */
    protected final IColony colony;

    /**
     * The event id.
     */
    protected int id;

    /**
     * Internal constructor.
     */
    protected AbstractExpeditionEvent(final IColony colony)
    {
        this.colony = colony;
    }

    /**
     * Construct an abstract colony from NBT data, meant to be called from implementations passing specific constructors along.
     *
     * @param colony      the target colony.
     * @param compound    the input compound data.
     * @param eventLoader a function for creating the event instance.
     * @return the created event instance.
     */
    public static <T extends AbstractExpeditionEvent> T loadFromNBT(
      final IColony colony,
      final CompoundTag compound,
      final Function<IColony, T> eventLoader)
    {
        final T event = eventLoader.apply(colony);
        event.deserializeNBT(compound);
        return event;
    }

    /**
     * Get the expedition instance for this event.
     *
     * @return the expedition instance.
     */
    public abstract IExpedition getExpedition();

    @Override
    public final void setStatus(final EventStatus status)
    {
        // No-op, expedition status uses a different enumeration to control active status, which can only be modified directly within this event.
    }

    @Override
    public final int getID()
    {
        return id;
    }

    /**
     * Get the colony for this event.
     *
     * @return the colony instance.
     */
    public final IColony getColony()
    {
        return colony;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = new CompoundTag();
        compound.putInt(TAG_EVENT_ID, id);
        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundTag compoundTag)
    {
        id = compoundTag.getInt(TAG_EVENT_ID);
    }
}
