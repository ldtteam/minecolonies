package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColonyTagCapability;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.BLOCKS_PER_CHUNK;
import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

/**
 * Update the ChunkCapability with a colony.
 */
public class UpdateChunkRangeCapabilityMessage extends AbstractMessage<UpdateChunkRangeCapabilityMessage, IMessage>
{
    /**
     * The colonies tags to send over.
     */
    private final List<Tuple<Tuple<Integer, Integer>, Integer>> colonies = new ArrayList<>();

    /**
     * Empty constructor used when registering the message.
     */
    public UpdateChunkRangeCapabilityMessage()
    {
        super();
    }

    /**
     * Create a message to update the chunk cap on the client side.
     * @param world the world.
     * @param xC the x pos.
     * @param zC the z pos.
     * @param range the range.
     */
    public UpdateChunkRangeCapabilityMessage(@NotNull final World world, final int xC, final int zC, final int range)
    {
        for (int x = -range; x <= range; x++ )
        {
            for (int z = -range; z <= range; z++ )
            {
                final int chunkX = xC + x;
                final int chunkZ = zC + z;
                if (world.isBlockLoaded(new BlockPos(chunkX * BLOCKS_PER_CHUNK, 0, chunkZ * BLOCKS_PER_CHUNK)))
                {
                    final Chunk chunk = world.getChunk(chunkX, chunkZ);
                    final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null);
                    if (cap != null)
                    {
                        colonies.add(new Tuple<>(new Tuple<>(chunkX, chunkZ), cap.getOwningColony()));
                    }
                }
            }
        }
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        final int size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            colonies.add(new Tuple<>(new Tuple<>(buf.readInt(), buf.readInt()), buf.readInt()));
        }
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonies.size());
        for (final Tuple<Tuple<Integer, Integer>, Integer> c : colonies)
        {
            buf.writeInt(c.getFirst().getFirst());
            buf.writeInt(c.getFirst().getSecond());
            buf.writeInt(c.getSecond());
        }
    }

    @Override
    protected void messageOnClientThread(final UpdateChunkRangeCapabilityMessage message, final MessageContext ctx)
    {
        if(ctx.getClientHandler().world != null)
        {
            for (final Tuple<Tuple<Integer, Integer>, Integer> c : message.colonies)
            {
                final Chunk chunk = ctx.getClientHandler().world.getChunk(c.getFirst().getFirst(), c.getFirst().getSecond());
                final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null);
                if (cap != null)
                {
                    cap.setOwningColony(c.getSecond(), chunk);
                    cap.addColony(c.getSecond(), chunk);
                }
            }
        }
    }
}