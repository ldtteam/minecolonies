package com.minecolonies.core.network.messages.server.colony.visitor.expeditionary;

import com.minecolonies.api.colony.ICivilianData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpedition;
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
import static com.minecolonies.api.util.constant.ExpeditionConstants.EXPEDITION_START_MESSAGE;

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
     * @param colony         the colony this expedition will occur in.
     * @param expeditionType the expedition type.
     * @param expedition     the input expedition instance.
     */
    public StartExpeditionMessage(final IColony colony, final ColonyExpeditionType expeditionType, final ColonyExpedition expedition)
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

        // Attempt to create the expedition.
        final ColonyExpedition newExpedition = colony.getExpeditionManager().addExpedition(expedition);
        if (newExpedition == null)
        {
            return;
        }

        MessageUtils.format(EXPEDITION_START_MESSAGE, expedition.getLeader().getName());

        // Create the event related to this expedition.
        colony.getEventManager().addEvent(new ColonyExpeditionEvent(colony, expeditionType, newExpedition));

        // Add all members to the travelling manager and de-spawn them.
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
        expedition = ColonyExpedition.loadFromNBT(buf.readNbt());
    }
}
