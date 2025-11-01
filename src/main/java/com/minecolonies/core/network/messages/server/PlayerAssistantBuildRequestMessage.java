package com.minecolonies.core.network.messages.server;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.core.items.ItemAssistantHammer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Adds a entry to the builderRequired map.
 */
public class PlayerAssistantBuildRequestMessage extends AbstractColonyServerMessage
{
    private int      workorderID;
    private BlockPos interactPos;

    /**
     * Empty constructor used when registering the
     */
    public PlayerAssistantBuildRequestMessage()
    {
        super();
    }

    public PlayerAssistantBuildRequestMessage(final IColony colony, final int workorderID, final BlockPos interactPos)
    {
        super(colony);
        this.workorderID = workorderID;
        this.interactPos = interactPos;
    }

    @Override
    protected void toBytesOverride(final FriendlyByteBuf buf)
    {
        buf.writeInt(workorderID);
        buf.writeBlockPos(interactPos);
    }

    @Override
    protected void fromBytesOverride(final FriendlyByteBuf buf)
    {
        workorderID = buf.readInt();
        interactPos = buf.readBlockPos();
    }

    @Nullable
    public Action permissionNeeded()
    {
        return Action.PLACE_BLOCKS;
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        final Player player = ctxIn.getSender();

        final IWorkOrder workOrder = colony.getWorkManager().getWorkOrder(workorderID);
        if (workOrder == null)
        {
            player.sendSystemMessage(Component.literal("Could not find workorder with id: " + workorderID));
            return;
        }

        if (player.getMainHandItem().getItem() instanceof ItemAssistantHammer hammer)
        {
            hammer.placeBlock(player, colony, workOrder, interactPos);
        }
    }
}

