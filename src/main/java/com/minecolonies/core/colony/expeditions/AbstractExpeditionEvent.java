package com.minecolonies.core.colony.expeditions;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.colony.expeditions.ExpeditionStatus;
import com.minecolonies.api.colony.expeditions.IExpedition;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.function.TriFunction;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_EVENT_ID;

/**
 * Abstract implementation for expedition related events.
 */
public abstract class AbstractExpeditionEvent implements IColonyEvent
{
    /**
     * NBT tags.
     */
    private static final String TAG_EXPEDITION = "expedition";

    /**
     * The expedition instance.
     */
    protected final IExpedition expedition;

    /**
     * The event ID.
     */
    private final int id;

    /**
     * The colony this event is for.
     */
    private final IColony colony;

    /**
     * Internal constructor.
     */
    protected AbstractExpeditionEvent(final int id, final IColony colony, final IExpedition expedition)
    {
        this.id = id;
        this.colony = colony;
        this.expedition = expedition;
    }

    /**
     * Construct an abstract colony from NBT data, meant to be called from implementations passing specific constructors along.
     *
     * @param colony      the target colony.
     * @param compound    the input compound data.
     * @param eventLoader a function for creating the event instance.
     * @param <T>         the generic type for the event class.
     * @return the created event instance.
     */
    public static <T extends AbstractExpeditionEvent> T loadFromNBT(
      final IColony colony,
      final CompoundTag compound,
      final TriFunction<Integer, IColony, IExpedition, T> eventLoader)
    {
        final int id = compound.getInt(TAG_EVENT_ID);
        final IExpedition expedition = Expedition.loadFromNBT(compound.getCompound(TAG_EXPEDITION));
        final T event = eventLoader.apply(id, colony, expedition);
        event.deserializeNBT(compound);
        return event;
    }

    @Override
    public final EventStatus getStatus()
    {
        return expedition.getStatus().getEventStatus();
    }

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

    @Override
    public void onStart()
    {
        expedition.setStatus(ExpeditionStatus.EMBARKED);
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

    /**
     * Get the expedition instance for this event.
     *
     * @return the expedition instance.
     */
    public IExpedition getExpedition()
    {
        return expedition;
    }
}
