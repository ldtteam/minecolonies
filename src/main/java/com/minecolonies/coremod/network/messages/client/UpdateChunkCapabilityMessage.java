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
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.colony.IColony.CLOSE_COLONY_CAP;

/**
 * Update the ChunkCapability with a colony.
 */
public class UpdateChunkCapabilityMessage implements IMessage
{
    /**
     * The chunk cap data
     */
    private ChunkCapData chunkCapData;

    /**
     * Empty constructor used when registering the
     */
    public UpdateChunkCapabilityMessage()
    {
        super();
    }

    /**
     * Create a message to update the chunk cap on the client side.
     *
     * @param chunkCapData the data.
     */
    public UpdateChunkCapabilityMessage(@NotNull final ChunkCapData chunkCapData)
    {
        this.chunkCapData = chunkCapData;
    }

    public UpdateChunkCapabilityMessage(@NotNull final IColonyTagCapability tagCapability, final int x, final int z)
    {
        this.chunkCapData = new ChunkCapData(x, z, tagCapability.getOwningColony(), tagCapability.getStaticClaimColonies(), tagCapability.getAllClaimingBuildings());
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        chunkCapData = ChunkCapData.fromBytes(buf);
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        chunkCapData.toBytes(buf);
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

        if (!WorldUtil.isChunkLoaded(world, new ChunkPos(chunkCapData.x, chunkCapData.z)))
        {
            ChunkClientDataHelper.addCapData(chunkCapData);
            return;
        }

        final LevelChunk chunk = world.getChunk(chunkCapData.x, chunkCapData.z);
        final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);

        if (cap != null && cap.getOwningColony() != chunkCapData.getOwningColony())
        {
            ChunkClientDataHelper.applyCap(chunkCapData, chunk);
        }
    }
}