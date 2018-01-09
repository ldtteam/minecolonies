package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

/**
 * Update the ChunkCapability with a list of colonies.
 */
public class UpdateChunkCapabilityMessage implements IMessage, IMessageHandler<UpdateChunkCapabilityMessage, IMessage>
{
    /**
     * The colony.
     */
    private int colonyId;

    /**
     * All owned chunks.
     */
    private List<BlockPos> ownedChunks;

    /**
     * All close chunks.
     */
    private List<BlockPos> closeChunks;

    /**
     * If the colony should be the owner.
     */
    private boolean add;

    /**
     * Empty constructor used when registering the message.
     */
    public UpdateChunkCapabilityMessage()
    {
        super();
    }

    /**
     * Creates a message to notify the client side chunks about the colony change.
     * @param colonyId the colony id.
     * @param ownedChunks the owned chunks.
     * @param closeChunks the closed chunks.
     * @param add if to add or to remove.
     */
    public UpdateChunkCapabilityMessage(final int colonyId, final List<BlockPos> ownedChunks, final List<BlockPos> closeChunks, final boolean add)
    {
        this.colonyId = colonyId;
        this.ownedChunks = new ArrayList<>(ownedChunks);
        this.closeChunks = new ArrayList<>(closeChunks);
        this.add = add;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        ownedChunks = new ArrayList<>();
        closeChunks = new ArrayList<>();
        colonyId = buf.readInt();
        final int sizeOwned = buf.readInt();
        final int sizeClose = buf.readInt();
        for(int i = 0; i < sizeOwned; i++)
        {
            ownedChunks.add(BlockPosUtil.readFromByteBuf(buf));
        }
        for(int i = 0; i < sizeClose; i++)
        {
            closeChunks.add(BlockPosUtil.readFromByteBuf(buf));
        }
        add = buf.readBoolean();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(ownedChunks.size());
        buf.writeInt(closeChunks.size());
        for(final BlockPos pos: ownedChunks)
        {
            BlockPosUtil.writeToByteBuf(buf, pos);
        }
        for(final BlockPos pos: closeChunks)
        {
            BlockPosUtil.writeToByteBuf(buf, pos);
        }
        buf.writeBoolean(add);
    }

    @Nullable
    @Override
    public IMessage onMessage(@NotNull final UpdateChunkCapabilityMessage message, final MessageContext ctx)
    {

        for(final BlockPos pos: ownedChunks)
        {
            final Chunk chunk = ctx.getClientHandler().world.getChunkFromChunkCoords(pos.getX(), pos.getZ());
            final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null);

            if(add)
            {
                cap.setOwningColony(message.colonyId);
                cap.addColony(message.colonyId);
            }
            else
            {
                cap.removecolony(message.colonyId);
            }
        }

        for(final BlockPos pos: closeChunks)
        {
            final Chunk chunk = ctx.getClientHandler().world.getChunkFromChunkCoords(pos.getX(), pos.getZ());
            final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null);

            if(add)
            {
                cap.addColony(message.colonyId);
            }
            else
            {
                cap.removecolony(message.colonyId);
            }
        }

        return null;
    }
}
