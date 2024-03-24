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
    public Set<MNode> lastDebugNodesNotVisited = new HashSet<>();

    /**
     * Set of chosen nodes for the path.
     */
    public Set<MNode> lastDebugNodesPath = new HashSet<>();
    public Set<MNode> debugNodesVisitedLater = new HashSet<>();
    public Set<MNode> debugNodesOrgPath = new HashSet<>();
    public Set<MNode> debugNodesExtra = new HashSet<>();

    /**
     * Create a new path message with the filled pathpoints.
     */
    public SyncPathMessage(
      final Set<MNode> lastDebugNodesVisited,
      final Set<MNode> lastDebugNodesNotVisited,
      final Set<MNode> lastDebugNodesPath,
      final Set<MNode> debugNodesVisitedLater,
      final Set<MNode> debugNodesOrgPath,
      final Set<MNode> debugNodesExtra)
    {
        super(TYPE);
        this.lastDebugNodesVisited = lastDebugNodesVisited;
        this.lastDebugNodesNotVisited = lastDebugNodesNotVisited;
        this.lastDebugNodesPath = lastDebugNodesPath;
        this.debugNodesVisitedLater = debugNodesVisitedLater;
        this.debugNodesOrgPath = debugNodesOrgPath;
        this.debugNodesExtra = debugNodesExtra;
    }

    @Override
    protected void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeInt(lastDebugNodesVisited.size());
        for (final MNode node : lastDebugNodesVisited)
        {
            node.serializeToBuf(buf);
        }

        buf.writeInt(lastDebugNodesNotVisited.size());
        for (final MNode node : lastDebugNodesNotVisited)
        {
            node.serializeToBuf(buf);
        }

        buf.writeInt(lastDebugNodesPath.size());
        for (final MNode node : lastDebugNodesPath)
        {
            node.serializeToBuf(buf);
        }

        buf.writeInt(debugNodesVisitedLater.size());
        for (final MNode node : debugNodesVisitedLater)
        {
            node.serializeToBuf(buf);
        }

        buf.writeInt(debugNodesOrgPath.size());
        for (final MNode node : debugNodesOrgPath)
        {
            node.serializeToBuf(buf);
        }

        buf.writeInt(debugNodesExtra.size());
        for (final MNode node : debugNodesExtra)
        {
            node.serializeToBuf(buf);
        }
    }

    protected SyncPathMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        int size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            lastDebugNodesVisited.add(new MNode(buf));
        }

        size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            lastDebugNodesNotVisited.add(new MNode(buf));
        }

        size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            lastDebugNodesPath.add(new MNode(buf));
        }

        size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            debugNodesVisitedLater.add(new MNode(buf));
        }

        size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            debugNodesOrgPath.add(new MNode(buf));
        }

        size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            debugNodesExtra.add(new MNode(buf));
        }
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final Player player)
    {
        PathfindingDebugRenderer.lastDebugNodesVisited = lastDebugNodesVisited;
        PathfindingDebugRenderer.lastDebugNodesNotVisited = lastDebugNodesNotVisited;
        PathfindingDebugRenderer.lastDebugNodesPath = lastDebugNodesPath;
        PathfindingDebugRenderer.lastDebugNodesVisitedLater = debugNodesVisitedLater;
        PathfindingDebugRenderer.debugNodesOrgPath = debugNodesOrgPath;
        PathfindingDebugRenderer.debugNodesExtra = debugNodesExtra;
    }
}
