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

/**
 * Message to sync the reached positions over to the client for rendering.
 */
public class SyncPathReachedMessage implements IMessage
{
    /**
     * Reached position.
     */
    public BlockPos reached = null;

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
    public SyncPathReachedMessage(final BlockPos reached)
    {
        super();
        this.reached = reached;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(reached);
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {
        reached = (buf.readBlockPos());
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
            if (reached.getX() == node.x && reached.getY() == node.y && reached.getZ() == node.z)
            {
                node.setReachedByWorker(true);
            }
        }
    }
}
