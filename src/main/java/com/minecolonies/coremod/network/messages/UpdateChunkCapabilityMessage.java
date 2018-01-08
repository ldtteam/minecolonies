package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Update the ChunkCapability with a list of colonies.
 */
public class UpdateChunkCapabilityMessage implements IMessage, IMessageHandler<UpdateChunkCapabilityMessage, IMessage>
{
    /**
     * The colonies.
     */
    private List<Integer> colonyIds;

    /**
     * Position of the chunk.
     */
    private BlockPos      pos;

    /**
     * Empty constructor used when registering the message.
     */
    public UpdateChunkCapabilityMessage()
    {
        super();
    }

    /**
     * Creates a message to handle chunk compatabilities..
     * @param colonyIds the colonies.
     * @param pos the chunk pos.
     */
    public UpdateChunkCapabilityMessage(@NotNull final List<Integer> colonyIds, final BlockPos pos)
    {
        this.colonyIds = colonyIds;
        this.pos = pos;

    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyIds = new ArrayList<>();
        final int size = buf.readInt();
        for(int i = 0; i < size; i++)
        {
            colonyIds.add(buf.readInt());
        }
        pos = BlockPosUtil.readFromByteBuf(buf);
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyIds.size());
        for(final int colonyId: colonyIds)
        {
            buf.writeInt(colonyId);
        }
        BlockPosUtil.writeToByteBuf(buf, pos);
    }

    @Nullable
    @Override
    public IMessage onMessage(@NotNull final UpdateChunkCapabilityMessage message, final MessageContext ctx)
    {
        //todo add to chunk
        return null;
    }
}
