package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColonyTagCapability;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

/**
 * Update the ChunkCapability with a colony.
 */
public class UpdateChunkCapabilityMessage extends AbstractMessage<UpdateChunkCapabilityMessage, IMessage>
{
    /**
     * The colony.
     */
    private int owningColonyId;

    /**
     * X Position of the chunk.
     */
    private int x;

    /**
     * Z Position of the chunk.
     */
    private int z;

    /**
     * The list of the close colonies.
     */
    private List<Integer> closeColonies;

    /**
     * Empty constructor used when registering the message.
     */
    public UpdateChunkCapabilityMessage()
    {
        super();
    }

    /**
     * Create a message to update the chunk cap on the client side.
     * @param tagCapability the cap.
     * @param x the x pos.
     * @param z the z pos.
     */
    public UpdateChunkCapabilityMessage(@NotNull final IColonyTagCapability tagCapability, final int x, final int z)
    {
        this.x = x;
        this.z = z;
        this.owningColonyId = tagCapability.getOwningColony();
        this.closeColonies = tagCapability.getAllCloseColonies();
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        x = buf.readInt();
        z = buf.readInt();
        owningColonyId = buf.readInt();
        final int size = buf.readInt();
        closeColonies = new ArrayList<>();
        for(int i = 0; i < size; i++)
        {
            closeColonies.add(buf.readInt());
        }
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(x);
        buf.writeInt(z);
        buf.writeInt(owningColonyId);
        buf.writeInt(closeColonies.size());
        for(final int id: closeColonies)
        {
            buf.writeInt(id);
        }
    }

    @Override
    protected void messageOnClientThread(final UpdateChunkCapabilityMessage message, final MessageContext ctx)
    {
        if(ctx.getClientHandler().world != null)
        {
            final Chunk chunk = ctx.getClientHandler().world.getChunk(message.x, message.z);
            final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null);

            if (cap.getOwningColony() != message.owningColonyId)
            {
                cap.reset(chunk);
                cap.setOwningColony(message.owningColonyId, chunk);
                for (final int id : message.closeColonies)
                {
                    cap.addColony(id, chunk);
                }
            }
        }
    }
}