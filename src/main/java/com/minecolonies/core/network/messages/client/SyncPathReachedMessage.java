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
        this.reached = new HashSet<>(reached);
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        buf.writeCollection(reached, RegistryFriendlyByteBuf::writeBlockPos);
    }

    protected SyncPathReachedMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        reached = buf.readCollection(HashSet::new, RegistryFriendlyByteBuf::readBlockPos);
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final Player player)
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
