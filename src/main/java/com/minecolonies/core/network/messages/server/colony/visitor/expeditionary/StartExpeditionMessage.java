package com.minecolonies.core.network.messages.server.colony.visitor.expeditionary;

import com.minecolonies.api.colony.ICivilianData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import com.minecolonies.api.colony.interactionhandling.ModInteractionResponseHandlers;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.MessageUtils.MessagePriority;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpedition;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionEvent;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraftforge.network.NetworkEvent.Context;

import static com.minecolonies.api.util.constant.Constants.TICKS_HOUR;
import static com.minecolonies.api.util.constant.ExpeditionConstants.EXPEDITION_START_MESSAGE;

/**
 * Network message for triggering a colony expedition start.
 */
public class StartExpeditionMessage extends AbstractColonyServerMessage
{
    /**
     * The provided expedition instance.
     */
    private ColonyExpedition expedition;

    /**
     * Deserialization constructor.
     */
    public StartExpeditionMessage()
    {
    }

    /**
     * Default constructor.
     *
     * @param colony     the colony this expedition will occur in.
     * @param expedition the input expedition instance.
     */
    public StartExpeditionMessage(final IColony colony, final ColonyExpedition expedition)
    {
        super(colony);
        this.expedition = expedition;
    }

    @Override
    protected void onExecute(final Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        // Attempt to create the expedition.
        final boolean isAdded = colony.getExpeditionManager().addExpedition(expedition);
        if (!isAdded)
        {
            return;
        }

        final IVisitorData leaderData = expedition.getLeader().resolveCivilianData(colony);
        leaderData.removeInteractions(ModInteractionResponseHandlers.EXPEDITION.getPath());

        MessageUtils.format(EXPEDITION_START_MESSAGE, expedition.getLeader().getName())
          .withPriority(MessagePriority.IMPORTANT)
          .sendTo(colony)
          .forManagers();

        // Create the event related to this expedition.
        colony.getEventManager().addEvent(new ColonyExpeditionEvent(colony, expedition));

        // Add all members to the travelling manager and de-spawn them.
        final BlockPos townHallReturnPosition = BlockPosUtil.findSpawnPosAround(colony.getWorld(), colony.getBuildingManager().getTownHall().getPosition());
        for (final IExpeditionMember<?> member : expedition.getMembers())
        {
            colony.getTravelingManager().startTravellingTo(member.getId(), townHallReturnPosition, TICKS_HOUR, false);

            final ICivilianData memberData = member.resolveCivilianData(colony);
            if (memberData != null)
            {
                memberData.getEntity().ifPresent(entity -> entity.remove(RemovalReason.DISCARDED));
            }
        }
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
        expedition = ColonyExpedition.loadFromNBT(buf.readNbt());
    }
}
