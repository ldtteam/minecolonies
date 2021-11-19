package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.client.render.worldevent.PathfindingDebugRenderer;
import com.minecolonies.coremod.entity.pathfinding.MNode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * Asks the client to play a specific music
 */
public class SyncPathMessage implements IMessage
{
    public Set<MNode> lastDebugNodesVisited = new HashSet<>();
    public Set<MNode> lastDebugNodesNotVisited  = new HashSet<>();
    public Set<MNode> lastDebugNodesPath  = new HashSet<>();

    /**
     * Default constructor.
     */
    public SyncPathMessage()
    {
        super();
    }

    /**
     * Create a play music message with a specific sound event.
     *
     */
    public SyncPathMessage(final Set<MNode> lastDebugNodesVisited, final Set<MNode> lastDebugNodesNotVisited, final Set<MNode>  lastDebugNodesPath)
    {
        super();
        this.lastDebugNodesVisited = lastDebugNodesVisited;
        this.lastDebugNodesNotVisited = lastDebugNodesNotVisited;
        this.lastDebugNodesPath = lastDebugNodesPath;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
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
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
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
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        PathfindingDebugRenderer.lastDebugNodesVisited = lastDebugNodesVisited;
        PathfindingDebugRenderer.lastDebugNodesNotVisited = lastDebugNodesNotVisited;
        PathfindingDebugRenderer.lastDebugNodesPath = lastDebugNodesPath;
    }
}
