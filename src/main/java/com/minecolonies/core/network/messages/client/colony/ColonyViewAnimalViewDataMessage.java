package com.minecolonies.core.network.messages.client.colony;

import com.minecolonies.api.colony.IAnimalData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.Log;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Sends visitor data to the client
 */
public class ColonyViewAnimalViewDataMessage implements IMessage
{
    /**
     * The colony id
     */
    private int colonyId;

    /**
     * The dimension the citizen is in.
     */
    private ResourceKey<Level> dimension;

    /**
     * Visiting entity data
     */
    private Set<IAnimalData> animals;

    /**
     * Visitor buf to read on client side.
     */
    private FriendlyByteBuf animalBuf;

    /**
     * If a general refresh is necessary,
     */
    private boolean refresh;

    /**
     * Empty constructor used when registering the
     */
    public ColonyViewAnimalViewDataMessage()
    {
        super();
    }

    /**
     * Updates a {@link com.minecolonies.core.colony.CitizenDataView} of the citizens.
     *
     * @param colony Colony of the citizen
     */
    public ColonyViewAnimalViewDataMessage(@NotNull final IColony colony, @NotNull final Set<IAnimalData> animals, final boolean refresh)
    {
        super();
        this.colonyId = colony.getID();
        this.dimension = colony.getDimension();
        this.animals = animals;
        this.refresh = refresh;

        animalBuf = new FriendlyByteBuf(Unpooled.buffer());
        for (final IAnimalData data : animals)
        {
            animalBuf.writeInt(data.getId());
            data.serializeViewNetworkData(animalBuf);
        }
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        colonyId = buf.readInt();
        dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
        refresh = buf.readBoolean();
        this.animalBuf = new FriendlyByteBuf(buf.retain());
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        animalBuf.resetReaderIndex();
        buf.writeInt(colonyId);
        buf.writeUtf(dimension.location().toString());
        buf.writeBoolean(refresh);
        buf.writeInt(animals.size());
        buf.writeBytes(animalBuf);
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
        final IColonyView colony = IColonyManager.getInstance().getColonyView(colonyId, dimension);

        if (colony == null)
        {
            Log.getLogger().warn("Received animal data for nonexisting colony:" + colonyId + " dim:" + dimension);
        }
        else
        {
            colony.handleColonyViewAnimalMessage(animalBuf, refresh);
        }
        animalBuf.release();
    }
}
