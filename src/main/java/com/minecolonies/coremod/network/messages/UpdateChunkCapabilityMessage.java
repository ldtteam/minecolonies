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

import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

/**
 * Update the ChunkCapability with a colony.
 */
public class UpdateChunkCapabilityMessage implements IMessage, IMessageHandler<UpdateChunkCapabilityMessage, IMessage>
{
    /**
     * The colony.
     */
    private int colonyId;

    /**
     * Position of the chunk.
     */
    private BlockPos pos;

    /**
     * Add it to the list or remove it.
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
     * Creates a message to handle chunk compatabilities..
     * @param colonyId the colony.
     * @param pos the chunk pos.
     */
    public UpdateChunkCapabilityMessage(@NotNull final int colonyId, final BlockPos pos, final boolean add)
    {
        this.colonyId = colonyId;
        this.pos = pos;
        this.add = add;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        pos = BlockPosUtil.readFromByteBuf(buf);
        add = buf.readBoolean();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, pos);
        buf.writeBoolean(add);
    }

    @Nullable
    @Override
    public IMessage onMessage(@NotNull final UpdateChunkCapabilityMessage message, final MessageContext ctx)
    {
        final Chunk chunk = ctx.getClientHandler().world.getChunkFromChunkCoords(message.pos.getX(), message.pos.getZ());
        final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null);

        if(message.add)
        {
            cap.setOwningColony(message.colonyId);
            cap.addColony(message.colonyId);
        }
        else
        {
            cap.removecolony(message.colonyId);
        }
        return null;
    }
}