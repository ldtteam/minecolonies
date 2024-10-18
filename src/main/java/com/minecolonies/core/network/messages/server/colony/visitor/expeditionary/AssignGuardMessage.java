package com.minecolonies.core.network.messages.server.colony.visitor.expeditionary;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.core.items.ItemExpeditionSheet.ExpeditionSheetContainerManager;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;

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
     * In which hand the expedition sheet is.
     */
    private InteractionHand hand;

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
     * @param hand   in which hand the expedition sheet is.
     */
    public AssignGuardMessage(final ICitizenDataView guard, final boolean assign, final InteractionHand hand)
    {
        super(guard.getColony());
        this.id = guard.getId();
        this.assign = assign;
        this.hand = hand;
    }

    @Override
    protected void onExecute(final Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        if (!isLogicalServer)
        {
            return;
        }

        final ICitizenData guard = colony.getCitizenManager().getCivilian(id);
        if (guard == null)
        {
            colony.getCitizenManager().markDirty();
            return;
        }


        if (!guard.getJob().isGuard() || !guard.getJob().isCombatGuard())
        {
            return;
        }

        final ItemStack itemInHand = ctxIn.getSender().getItemInHand(hand);
        final ExpeditionSheetContainerManager expeditionSheetContainerManager = new ExpeditionSheetContainerManager(itemInHand);
        expeditionSheetContainerManager.toggleMember(guard.getId(), assign);
    }

    @Override
    protected void toBytesOverride(final FriendlyByteBuf buf)
    {
        buf.writeInt(id);
        buf.writeBoolean(assign);
        buf.writeEnum(hand);
    }

    @Override
    protected void fromBytesOverride(final FriendlyByteBuf buf)
    {
        id = buf.readInt();
        assign = buf.readBoolean();
        hand = buf.readEnum(InteractionHand.class);
    }
}
