package com.minecolonies.core.network.messages.server.colony.visitor.expeditionary;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.expeditions.IExpedition;
import com.minecolonies.core.colony.expeditions.Expedition;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionEvent;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

/**
 * Network message for triggering a colony expedition start.
 */
public class StartExpeditionMessage extends AbstractColonyServerMessage
{
    /**
     * The provided expedition instance.
     */
    private IExpedition expedition;

    /**
     * Default constructor.
     *
     * @param colony     the colony this expedition will occur in.
     * @param expedition the input expedition instance.
     */
    public StartExpeditionMessage(final IColony colony, final IExpedition expedition)
    {
        super(colony);
        this.expedition = expedition;
    }

    @Override
    protected void onExecute(final Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        colony.getEventManager().addEvent(new ColonyExpeditionEvent(colony, expedition));
    }

    @Override
    protected void toBytesOverride(final FriendlyByteBuf buf)
    {
        final CompoundTag compound = new CompoundTag();
        expedition.write(compound);
        buf.writeNbt(compound);
    }

    @Override
    protected void fromBytesOverride(final FriendlyByteBuf buf)
    {
        expedition = Expedition.loadFromNBT(buf.readNbt());
    }
}
