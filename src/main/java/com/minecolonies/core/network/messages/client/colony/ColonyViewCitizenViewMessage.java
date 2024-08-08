package com.minecolonies.core.network.messages.client.colony;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.Colony;
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

/**
 * Add or Update a ColonyView on the client.
 */
public class ColonyViewCitizenViewMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "colony_view_citizen_view", ColonyViewCitizenViewMessage::new);

    private final int          colonyId;
    private final int          citizenId;
    private final RegistryFriendlyByteBuf citizenBuffer;

    /**
     * The dimension the citizen is in.
     */
    private final ResourceKey<Level> dimension;

    /**
     * Updates a {@link com.minecolonies.core.colony.CitizenDataView} of the citizens.
     *
     * @param colony  Colony of the citizen
     * @param citizen Citizen data of the citizen to update view
     */
    public ColonyViewCitizenViewMessage(@NotNull final Colony colony, @NotNull final ICitizenData citizen)
    {
        super(TYPE);
        this.colonyId = colony.getID();
        this.citizenId = citizen.getId();
        this.citizenBuffer = new RegistryFriendlyByteBuf(new FriendlyByteBuf(Unpooled.buffer()), colony.getWorld().registryAccess());
        this.dimension = citizen.getColony().getDimension();
        citizen.serializeViewNetworkData(citizenBuffer);
    }

    protected ColonyViewCitizenViewMessage(@NotNull final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        colonyId = buf.readInt();
        citizenId = buf.readInt();
        dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(buf.readUtf(32767)));
        this.citizenBuffer = new RegistryFriendlyByteBuf(new FriendlyByteBuf(Unpooled.wrappedBuffer(buf.readByteArray())), buf.registryAccess());
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        citizenBuffer.resetReaderIndex();
        buf.writeInt(colonyId);
        buf.writeInt(citizenId);
        buf.writeUtf(dimension.location().toString());
        buf.writeByteArray(citizenBuffer.array());
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final Player player)
    {
        IColonyManager.getInstance().handleColonyViewCitizensMessage(colonyId, citizenId, citizenBuffer, dimension);
    }
}
