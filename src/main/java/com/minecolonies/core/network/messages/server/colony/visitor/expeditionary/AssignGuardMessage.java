package com.minecolonies.core.network.messages.server.colony.visitor.expeditionary;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.core.colony.expeditions.ExpeditionCitizenMember;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

import static com.minecolonies.core.entity.visitor.ExpeditionaryVisitorType.EXTRA_DATA_EXPEDITION;

/**
 * Message for assigning a guard to an expedition.
 */
public class AssignGuardMessage extends AbstractColonyServerMessage
{
    /**
     * The id of the guard citizen.
     */
    private int id;

    /**
     * Whether to assign or unassign the guard.
     */
    private boolean assign;

    /**
     * Deserialization constructor.
     */
    public AssignGuardMessage()
    {
    }

    /**
     * Create a new guard assignment message for the given guard.
     *
     * @param guard  the guard to (un)assign.
     * @param assign whether to assign or unassign.
     */
    public AssignGuardMessage(final ICitizenDataView guard, final boolean assign)
    {
        super(guard.getColony());
        this.id = guard.getId();
        this.assign = assign;
    }

    @Override
    protected void onExecute(final Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        if (!isLogicalServer)
        {
            return;
        }

        final IVisitorData activeExpeditionary = colony.getVisitorManager().getActiveExpeditionary();
        if (activeExpeditionary == null)
        {
            return;
        }

        final ICitizenData guardCitizen = colony.getCitizenManager().getCivilian(id);
        if (guardCitizen.getJob().isGuard() && guardCitizen.getJob().isCombatGuard())
        {
            if (assign)
            {
                activeExpeditionary.getExtraDataValue(EXTRA_DATA_EXPEDITION).addMember(new ExpeditionCitizenMember(guardCitizen));
            }
            else
            {
                activeExpeditionary.getExtraDataValue(EXTRA_DATA_EXPEDITION).removeMember(new ExpeditionCitizenMember(guardCitizen));
            }
        }
    }

    @Override
    protected void toBytesOverride(final FriendlyByteBuf buf)
    {
        buf.writeInt(id);
        buf.writeBoolean(assign);
    }

    @Override
    protected void fromBytesOverride(final FriendlyByteBuf buf)
    {
        id = buf.readInt();
        assign = buf.readBoolean();
    }
}
