package com.minecolonies.core.network.messages.client;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.core.client.render.worldevent.PathfindingDebugRenderer;
import com.minecolonies.core.entity.pathfinding.MNode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * Message to sync the reached positions over to the client for rendering.
 */
public class SyncPathReachedMessage implements IMessage
{
    /**
     * Set of reached positions.
     */
    public Set<BlockPos> reached = new HashSet<>();

    /**
     * Default constructor.
     */
    public SyncPathReachedMessage()
    {
        super();
    }

    /**
     * Create the message to send a set of positions over to the client side.
     *
     */
    public SyncPathReachedMessage(final Set<BlockPos> reached)
    {
        super();
        this.reached = reached;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeInt(reached.size());
        for (final BlockPos node : reached)
        {
            buf.writeBlockPos(node);
        }
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {
        int size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            reached.add(buf.readBlockPos());
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
        for (final MNode node : PathfindingDebugRenderer.lastDebugNodesPath)
        {
            for (final BlockPos reachedPos : reached)
            {
                if (reachedPos.getX() == node.x && reachedPos.getY() == node.y && reachedPos.getZ() == node.z)
                {
                    node.setReachedByWorker(true);
                }
            }
        }
    }
}
