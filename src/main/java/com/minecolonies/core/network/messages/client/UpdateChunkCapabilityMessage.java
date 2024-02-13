package com.minecolonies.core.network.messages.client;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.capability.IColonyTagCapability;
import com.minecolonies.api.util.ChunkCapData;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.util.ChunkClientDataHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Update the ChunkCapability with a colony.
 */
public class UpdateChunkCapabilityMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "update_chunk_capability", UpdateChunkCapabilityMessage::new);

    /**
     * The chunk cap data
     */
    private final ChunkCapData chunkCapData;

    /**
     * Create a message to update the chunk cap on the client side.
     *
     * @param chunkCapData the data.
     */
    public UpdateChunkCapabilityMessage(@NotNull final ChunkCapData chunkCapData)
    {
        super(TYPE);
        this.chunkCapData = chunkCapData;
    }

    public UpdateChunkCapabilityMessage(@NotNull final IColonyTagCapability tagCapability, final int x, final int z)
    {
        this(new ChunkCapData(x, z, tagCapability.getOwningColony(), tagCapability.getStaticClaimColonies(), tagCapability.getAllClaimingBuildings()));
    }

    protected UpdateChunkCapabilityMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        chunkCapData = ChunkCapData.fromBytes(buf);
    }

    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        chunkCapData.toBytes(buf);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final Player player)
    {
        final ClientLevel world = Minecraft.getInstance().level;

        if (!WorldUtil.isChunkLoaded(world, new ChunkPos(chunkCapData.x, chunkCapData.z)))
        {
            ChunkClientDataHelper.addCapData(chunkCapData);
            return;
        }

        final LevelChunk chunk = world.getChunk(chunkCapData.x, chunkCapData.z);
        final IColonyTagCapability cap = IColonyTagCapability.getCapability(chunk);

        if (cap != null && cap.getOwningColony() != chunkCapData.getOwningColony())
        {
            ChunkClientDataHelper.applyCap(chunkCapData, chunk);
        }
    }
}