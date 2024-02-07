package com.minecolonies.core.network.messages.client;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.render.worldevent.PathfindingDebugRenderer;
import com.minecolonies.core.entity.pathfinding.MNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.HashSet;
import java.util.Set;

/**
 * Message to sync some path over to the client.
 */
public class SyncPathMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "sync_path", SyncPathMessage::new);

    /**
     * Set of visited nodes.
     */
    private final Set<MNode> lastDebugNodesVisited;

    /**
     * Set of not visited nodes.
     */
    private final Set<MNode> lastDebugNodesNotVisited;

    /**
     * Set of chosen nodes for the path.
     */
    private final Set<MNode> lastDebugNodesPath;

    /**
     * Create a new path message with the filled pathpoints.
     */
    public SyncPathMessage(final Set<MNode> lastDebugNodesVisited, final Set<MNode> lastDebugNodesNotVisited, final Set<MNode>  lastDebugNodesPath)
    {
        super(TYPE);
        this.lastDebugNodesVisited = lastDebugNodesVisited;
        this.lastDebugNodesNotVisited = lastDebugNodesNotVisited;
        this.lastDebugNodesPath = lastDebugNodesPath;
    }

    @Override
    protected void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeCollection(lastDebugNodesVisited, (b, n) -> n.serializeToBuf(buf));
        buf.writeCollection(lastDebugNodesNotVisited, (b, n) -> n.serializeToBuf(buf));
        buf.writeCollection(lastDebugNodesPath, (b, n) -> n.serializeToBuf(buf));
    }

    protected SyncPathMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        lastDebugNodesVisited = buf.readCollection(HashSet::new, MNode::new);
        lastDebugNodesNotVisited = buf.readCollection(HashSet::new, MNode::new);
        lastDebugNodesPath = buf.readCollection(HashSet::new, MNode::new);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final Player player)
    {
        PathfindingDebugRenderer.lastDebugNodesVisited = lastDebugNodesVisited;
        PathfindingDebugRenderer.lastDebugNodesNotVisited = lastDebugNodesNotVisited;
        PathfindingDebugRenderer.lastDebugNodesPath = lastDebugNodesPath;
    }
}
