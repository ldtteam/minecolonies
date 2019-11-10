package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.network.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.BLOCKS_PER_CHUNK;
import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

/**
 * Update the ChunkCapability with a colony.
 */
public class UpdateChunkRangeCapabilityMessage implements IMessage
{
    /**
     * The colonies tags to send over.
     */
    private final List<Tuple<Tuple<Integer, Integer>, Integer>> colonies = new ArrayList<>();

    /**
     * Empty constructor used when registering the
     */
    public UpdateChunkRangeCapabilityMessage()
    {
        super();
    }

    /**
     * Create a message to update the chunk cap on the client side.
     *
     * @param world the world.
     * @param xC    the x pos.
     * @param zC    the z pos.
     * @param range the range.
     */
    public UpdateChunkRangeCapabilityMessage(@NotNull final World world, final int xC, final int zC, final int range)
    {
        for (int x = -range; x <= range; x++)
        {
            for (int z = -range; z <= range; z++)
            {
                final int chunkX = xC + x;
                final int chunkZ = zC + z;
                if (world.isBlockLoaded(new BlockPos(chunkX * BLOCKS_PER_CHUNK, 0, chunkZ * BLOCKS_PER_CHUNK)))
                {
                    final Chunk chunk = world.getChunk(chunkX, chunkZ);
                    final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);
                    if (cap != null)
                    {
                        colonies.add(new Tuple<>(new Tuple<>(chunkX, chunkZ), cap.getOwningColony()));
                    }
                }
            }
        }
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        final int size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            colonies.add(new Tuple<>(new Tuple<>(buf.readInt(), buf.readInt()), buf.readInt()));
        }
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonies.size());
        for (final Tuple<Tuple<Integer, Integer>, Integer> c : colonies)
        {
            buf.writeInt(c.getA().getA());
            buf.writeInt(c.getA().getB());
            buf.writeInt(c.getB());
        }
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final ClientWorld world = Minecraft.getInstance().world;
        for (final Tuple<Tuple<Integer, Integer>, Integer> c : colonies)
        {
            final Chunk chunk = world.getChunk(c.getA().getA(), c.getA().getB());
            final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);
            if (cap != null)
            {
                cap.setOwningColony(c.getB(), chunk);
                cap.addColony(c.getB(), chunk);
            }
        }
    }
}