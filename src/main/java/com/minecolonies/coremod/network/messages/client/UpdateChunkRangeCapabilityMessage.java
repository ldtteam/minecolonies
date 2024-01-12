package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.ChunkCapData;
import com.minecolonies.coremod.util.ChunkClientDataHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.colony.IColony.CLOSE_COLONY_CAP;

/**
 * Update the ChunkCapability with a colony.
 */
public class UpdateChunkRangeCapabilityMessage implements IMessage
{
    /**
     * The colonies tags to send over.
     */
    private final List<ChunkCapData> caps = new ArrayList<>();

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
     * @param world       the world.
     * @param xC          the x pos.
     * @param zC          the z pos.
     * @param range       the range.
     * @param checkLoaded are we checking for loaded?
     */
    public UpdateChunkRangeCapabilityMessage(@NotNull final Level world, final int xC, final int zC, final int range, boolean checkLoaded)
    {
        for (int x = -range; x <= range; x++)
        {
            for (int z = -range; z <= range; z++)
            {
                final int chunkX = xC + x;
                final int chunkZ = zC + z;
                if (!checkLoaded || WorldUtil.isEntityChunkLoaded(world, chunkX, chunkZ))
                {
                    final LevelChunk chunk = world.getChunk(chunkX, chunkZ);
                    final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);
                    if (cap != null)
                    {
                        caps.add(new ChunkCapData(chunkX, chunkZ, cap.getOwningColony(), cap.getStaticClaimColonies(), cap.getAllClaimingBuildings()));
                    }
                }
            }
        }
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        final int size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            caps.add(ChunkCapData.fromBytes(buf));
        }
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(caps.size());
        for (final ChunkCapData c : caps)
        {
            c.toBytes(buf);
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
        final ClientLevel world = Minecraft.getInstance().level;
        for (final ChunkCapData data : caps)
        {
            if (!WorldUtil.isChunkLoaded(world, new ChunkPos(data.x, data.z)))
            {
                ChunkClientDataHelper.addCapData(data);
                continue;
            }

            final LevelChunk chunk = world.getChunk(data.x, data.z);
            ChunkClientDataHelper.applyCap(data, chunk);
        }
    }
}