package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.entity.pathfinding.Node;
import com.minecolonies.coremod.entity.pathfinding.Pathfinding;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * Message to sync some path over to the client.
 */
public class SyncPathMessage implements IMessage
{
    /**
     * Set of visited nodes.
     */
    public Set<Node> lastDebugNodesVisited = new HashSet<>();

    /**
     * Set of not visited nodes.
     */
    public Set<Node> lastDebugNodesNotVisited  = new HashSet<>();

    /**
     * Set of chosen nodes for the path.
     */
    public Set<Node> lastDebugNodesPath  = new HashSet<>();

    /**
     * Default constructor.
     */
    public SyncPathMessage()
    {
        super();
    }

    /**
     * Create a new path message with the filled pathpoints.
     */
    public SyncPathMessage(final Set<Node> lastDebugNodesVisited, final Set<Node> lastDebugNodesNotVisited, final Set<Node>  lastDebugNodesPath)
    {
        super();
        this.lastDebugNodesVisited = lastDebugNodesVisited;
        this.lastDebugNodesNotVisited = lastDebugNodesNotVisited;
        this.lastDebugNodesPath = lastDebugNodesPath;
    }

    @Override
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeInt(lastDebugNodesVisited.size());
        for (final Node node : lastDebugNodesVisited)
        {
            node.serializeToBuf(buf);
        }

        buf.writeInt(lastDebugNodesNotVisited.size());
        for (final Node node : lastDebugNodesNotVisited)
        {
            node.serializeToBuf(buf);
        }

        buf.writeInt(lastDebugNodesPath.size());
        for (final Node node : lastDebugNodesPath)
        {
            node.serializeToBuf(buf);
        }
    }

    @Override
    public void fromBytes(final PacketBuffer buf)
    {
        int size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            lastDebugNodesVisited.add(new Node(buf));
        }

        size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            lastDebugNodesNotVisited.add(new Node(buf));
        }

        size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            lastDebugNodesPath.add(new Node(buf));
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
        Pathfinding.lastDebugNodesVisited = lastDebugNodesVisited;
        Pathfinding.lastDebugNodesNotVisited = lastDebugNodesNotVisited;
        Pathfinding.lastDebugNodesPath = lastDebugNodesPath;
    }
}
