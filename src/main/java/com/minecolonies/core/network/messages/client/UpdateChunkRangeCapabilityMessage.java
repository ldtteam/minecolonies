package com.minecolonies.core.network.messages.client;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.capability.IColonyTagCapability;
import com.minecolonies.api.util.ChunkCapData;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.util.ChunkClientDataHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.colony.IColony.CLOSE_COLONY_CAP;

/**
 * Update the ChunkCapability with a colony.
 */
public class UpdateChunkRangeCapabilityMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "update_chunk_range_capability", UpdateChunkRangeCapabilityMessage::new);

    /**
     * The colonies tags to send over.
     */
    private final List<ChunkCapData> caps;

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
        super(TYPE);
        caps = new ArrayList<>();
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

    protected UpdateChunkRangeCapabilityMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        caps = buf.readCollection(ArrayList::new, ChunkCapData::fromBytes);
    }

    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeCollection(caps, (b, c) -> c.toBytes(b));
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final Player player)
    {
        for (final ChunkCapData data : caps)
        {
            if (!WorldUtil.isChunkLoaded(player.level(), new ChunkPos(data.x, data.z)))
            {
                ChunkClientDataHelper.addCapData(data);
                continue;
            }

            final LevelChunk chunk = player.level().getChunk(data.x, data.z);
            ChunkClientDataHelper.applyCap(data, chunk);
        }
    }
}