package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.network.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.chunk.Chunk;

import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

/**
 * Update the ChunkCapability with a colony.
 */
public class UpdateChunkCapabilityMessage implements IMessage
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
     * Empty constructor used when registering the 
     */
    public UpdateChunkCapabilityMessage()
    {
        super();
    }

    /**
     * Create a message to update the chunk cap on the client side.
     *
     * @param tagCapability the cap.
     * @param x             the x pos.
     * @param z             the z pos.
     */
    public UpdateChunkCapabilityMessage(@NotNull final IColonyTagCapability tagCapability, final int x, final int z)
    {
        this.x = x;
        this.z = z;
        this.owningColonyId = tagCapability.getOwningColony();
        this.closeColonies = tagCapability.getAllCloseColonies();
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        x = buf.readInt();
        z = buf.readInt();
        owningColonyId = buf.readInt();
        final int size = buf.readInt();
        closeColonies = new ArrayList<>();
        for (int i = 0; i < size; i++)
        {
            closeColonies.add(buf.readInt());
        }
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(x);
        buf.writeInt(z);
        buf.writeInt(owningColonyId);
        buf.writeInt(closeColonies.size());
        for (final int id : closeColonies)
        {
            buf.writeInt(id);
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
        final Chunk chunk = world.getChunk(x, z);
        final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);

        if (cap != null && cap.getOwningColony() != owningColonyId)
        {
            cap.reset();
            cap.setOwningColony(owningColonyId);
            for (final int id : closeColonies)
            {
                cap.addColony(id);
            }
        }
    }
}