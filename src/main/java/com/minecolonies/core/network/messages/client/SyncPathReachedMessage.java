package com.minecolonies.core.network.messages.client;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.render.worldevent.PathfindingDebugRenderer;
import com.minecolonies.core.entity.pathfinding.MNode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Message to sync the reached positions over to the client for rendering.
 */
public class SyncPathReachedMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "sync_path_reached", SyncPathReachedMessage::new);

    /**
     * Reached position.
     */
    public BlockPos reached = null;

    /**
     * Create the message to send a set of positions over to the client side.
     *
     */
    public SyncPathReachedMessage(final BlockPos reached)
    {
        super(TYPE);
        this.reached = reached;
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        buf.writeBlockPos(reached);
    }

    protected SyncPathReachedMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        reached = (buf.readBlockPos());
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final Player player)
    {
        for (final MNode node : PathfindingDebugRenderer.lastDebugNodesPath)
        {
            if (reached.getX() == node.x && reached.getY() == node.y && reached.getZ() == node.z)
            {
                node.setReachedByWorker(true);
            }
        }
    }
}
