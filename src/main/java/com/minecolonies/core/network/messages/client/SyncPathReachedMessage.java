package com.minecolonies.core.network.messages.client;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.render.worldevent.PathfindingDebugRenderer;
import com.minecolonies.core.entity.pathfinding.MNode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.HashSet;
import java.util.Set;

/**
 * Message to sync the reached positions over to the client for rendering.
 */
public class SyncPathReachedMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "sync_path_reached", SyncPathReachedMessage::new);

    /**
     * Set of reached positions.
     */
    public final Set<BlockPos> reached;

    /**
     * Create the message to send a set of positions over to the client side.
     *
     */
    public SyncPathReachedMessage(final Set<BlockPos> reached)
    {
        super(TYPE);
        this.reached = reached;
    }

    @Override
    protected void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeCollection(reached, FriendlyByteBuf::writeBlockPos);
    }

    protected SyncPathReachedMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        reached = buf.readCollection(HashSet::new, FriendlyByteBuf::readBlockPos);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final Player player)
    {
        for (final MNode node : PathfindingDebugRenderer.lastDebugNodesPath)
        {
            if (reached.contains(node.pos))
            {
                node.setReachedByWorker(true);
            }
        }
    }
}
