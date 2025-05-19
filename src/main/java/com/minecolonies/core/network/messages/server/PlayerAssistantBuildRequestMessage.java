package com.minecolonies.core.network.messages.server;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.workorders.IWorkOrder;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.items.ItemAssistantHammer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

/**
 * Adds a entry to the builderRequired map.
 */
public class PlayerAssistantBuildRequestMessage extends AbstractColonyServerMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "assistanthammerrequest", PlayerAssistantBuildRequestMessage::new);

    private int      workorderID;
    private BlockPos interactPos;

    public PlayerAssistantBuildRequestMessage(final IColony colony, final int workorderID, final BlockPos interactPos)
    {
        super(TYPE, colony);
        this.workorderID = workorderID;
        this.interactPos = interactPos;
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeInt(workorderID);
        buf.writeBlockPos(interactPos);
    }

    protected PlayerAssistantBuildRequestMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, TYPE);
        workorderID = buf.readInt();
        interactPos = buf.readBlockPos();
    }

    @Nullable
    public Action permissionNeeded()
    {
        return Action.PLACE_BLOCKS;
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
    {
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

