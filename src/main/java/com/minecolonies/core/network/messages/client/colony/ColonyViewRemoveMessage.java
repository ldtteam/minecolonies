package com.minecolonies.core.network.messages.client.colony;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Message for removing a view on the client, used for cleaning up after deletion
 */
public class ColonyViewRemoveMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "colony_view_remove", ColonyViewRemoveMessage::new);

    private final int id;
    private final ResourceKey<Level> dimension;

    public ColonyViewRemoveMessage(final int id, final ResourceKey<Level> dimension)
    {
        super(TYPE);
        this.id = id;
        this.dimension = dimension;
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        buf.writeInt(id);
        buf.writeUtf(dimension.location().toString());
    }

    protected ColonyViewRemoveMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        id = buf.readInt();
        dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(buf.readUtf(32767)));
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final Player player)
    {
        IColonyManager.getInstance().removeColonyView(id, dimension);
    }
}
