package com.minecolonies.core.network.messages.server.colony.visitor.expeditionary;

import com.minecolonies.api.colony.ICivilianData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.colony.expeditions.IExpedition;
import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.core.colony.expeditions.Expedition;
import com.minecolonies.core.colony.expeditions.ExpeditionVisitorMember;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionEvent;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionType;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionTypeManager;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraftforge.network.NetworkEvent.Context;

import static com.minecolonies.api.util.constant.Constants.TICKS_HOUR;

/**
 * Network message for triggering a colony expedition start.
 */
public class StartExpeditionMessage extends AbstractColonyServerMessage
{
    /**
     * The provided expedition type id.
     */
    private ResourceLocation expeditionTypeId;

    /**
     * The provided expedition instance.
     */
    private IExpedition expedition;

    /**
     * Deserialization constructor.
     */
    public StartExpeditionMessage()
    {
    }

    /**
     * Default constructor.
     *
     * @param colony         the colony this expedition will occur in.
     * @param expeditionType the expedition type.
     * @param expedition     the input expedition instance.
     */
    public StartExpeditionMessage(final IColony colony, final ColonyExpeditionType expeditionType, final IExpedition expedition)
    {
        super(colony);
        this.expeditionTypeId = expeditionType.getId();
        this.expedition = expedition;
    }

    @Override
    protected void onExecute(final Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        /// Check if the expedition type can be found.
        final ColonyExpeditionType expeditionType = ColonyExpeditionTypeManager.getInstance().getExpeditionType(expeditionTypeId);
        if (expeditionType == null)
        {
            return;
        }

        final IExpeditionMember expeditionLeader = expedition.getMembers().stream().filter(ExpeditionVisitorMember.class::isInstance).findFirst().orElseThrow();
        for (final IColonyEvent event : colony.getEventManager().getEvents().values())
        {
            if (event instanceof ColonyExpeditionEvent expeditionEvent)
            {
                for (final IExpeditionMember member : expeditionEvent.getExpedition().getMembers())
                {
                    // If the leader of the input expedition is already present as part of another expedition, we likely managed to double-click the start button
                    // or re-open the interface before the interaction managed to go away. We do not want to allow duplications to enter the events.
                    if (expeditionLeader.equals(member))
                    {
                        return;
                    }
                }
            }
        }

        // Add all guards to the travelling manager and de-spawn them.
        final BlockPos townHallReturnPosition = BlockPosUtil.findSpawnPosAround(colony.getWorld(), colony.getBuildingManager().getTownHall().getPosition());
        for (final IExpeditionMember member : expedition.getMembers())
        {
            colony.getTravelingManager().startTravellingTo(member.getId(), townHallReturnPosition, TICKS_HOUR, false);

            final ICivilianData memberData = member.resolveCivilianData(colony);
            if (memberData != null)
            {
                memberData.getEntity().ifPresent(entity -> entity.remove(RemovalReason.DISCARDED));
            }
        }

        colony.getEventManager().addEvent(new ColonyExpeditionEvent(colony, expeditionType, expedition));
    }

    @Override
    protected void toBytesOverride(final FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(expeditionTypeId);
        final CompoundTag compound = new CompoundTag();
        expedition.write(compound);
        buf.writeNbt(compound);
    }

    @Override
    protected void fromBytesOverride(final FriendlyByteBuf buf)
    {
        expeditionTypeId = buf.readResourceLocation();
        expedition = Expedition.loadFromNBT(buf.readNbt());
    }
}
