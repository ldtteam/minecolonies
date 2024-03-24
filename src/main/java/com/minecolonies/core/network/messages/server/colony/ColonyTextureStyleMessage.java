package com.minecolonies.core.network.messages.server.colony;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

/**
 * Message to set the colony texture style.
 */
public class ColonyTextureStyleMessage extends AbstractColonyServerMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "colony_texture_style", ColonyTextureStyleMessage::new);

    /**
     * The chosen style.
     */
    private final String style;

    /**
     * Change the colony style from the client to the serverside.
     *
     * @param colony      the colony the player changed the style in.
     * @param style the list of patterns they set in the banner picker
     */
    public ColonyTextureStyleMessage(final IColony colony, final String style)
    {
        super(TYPE, colony);
        this.style = style;
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
    {
        colony.setTextureStyle(style);
    }

    @Override
    protected void toBytes(final FriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeUtf(style);
    }

    protected ColonyTextureStyleMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.style = buf.readUtf(32767);
    }
}
