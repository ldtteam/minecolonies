package com.minecolonies.coremod.network.messages.client.colony;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.network.IMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Message for removing a view on the client, used for cleaning up after deletion
 */
public class ColonyViewRemoveMessage implements IMessage
{
    private int id;
    private RegistryKey<World> dimension;

    public ColonyViewRemoveMessage()
    {
        super();
    }

    public ColonyViewRemoveMessage(final int id, final RegistryKey<World> dimension)
    {
        this.id = id;
        this.dimension = dimension;
    }

    @Override
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeInt(id);
        buf.writeUtf(dimension.location().toString());
    }

    @Override
    public void fromBytes(final PacketBuffer buf)
    {
        id = buf.readInt();
        dimension = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(buf.readUtf(32767)));
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
        IColonyManager.getInstance().removeColonyView(id, dimension);
    }
}
