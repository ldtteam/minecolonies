package com.minecolonies.core.network.messages.client.colony;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IAnimalData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Sends visitor data to the client
 */
public class ColonyViewAnimalViewDataMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "colony_animal_view_data", ColonyViewAnimalViewDataMessage::new);

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
    private RegistryFriendlyByteBuf animalBuf;

    /**
     * If a general refresh is necessary,
     */
    private boolean refresh;

    /**
     * Updates a {@link com.minecolonies.core.colony.CitizenDataView} of the citizens.
     *
     * @param colony Colony of the citizen
     */
    public ColonyViewAnimalViewDataMessage(@NotNull final IColony colony, @NotNull final Set<IAnimalData> animals, final boolean refresh)
    {
        super(TYPE);
        this.colonyId = colony.getID();
        this.dimension = colony.getDimension();
        this.animals = animals;
        this.refresh = refresh;

        animalBuf = new RegistryFriendlyByteBuf(new FriendlyByteBuf(Unpooled.buffer()), colony.getWorld().registryAccess());
        animalBuf.writeInt(animals.size());
        for (final IAnimalData data : animals)
        {
            animalBuf.writeInt(data.getId());
            data.serializeViewNetworkData(animalBuf);
        }
    }

    /**
     * In this constructor you deserialize received network payload. Formerly known as <code>#fromBytes(RegistryFriendlyByteBuf)</code>
     *
     * @param buf received network payload
     * @param type message type
     * @apiNote you can keep this protected to reduce visibility
     */
    protected ColonyViewAnimalViewDataMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(type);
        colonyId = buf.readInt();
        dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(buf.readUtf(32767)));
        refresh = buf.readBoolean();
        this.animalBuf = new RegistryFriendlyByteBuf(new FriendlyByteBuf(Unpooled.wrappedBuffer(buf.readByteArray())), buf.registryAccess());
    }

    @Override
    public void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        animalBuf.resetReaderIndex();
        buf.writeInt(colonyId);
        buf.writeUtf(dimension.location().toString());
        buf.writeBoolean(refresh);
        buf.writeByteArray(animalBuf.array());
    }

    @Override
    public void onExecute(final IPayloadContext ctxIn, final Player player)
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
