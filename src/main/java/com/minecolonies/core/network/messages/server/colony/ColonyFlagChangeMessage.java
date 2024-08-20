package com.minecolonies.core.network.messages.server.colony;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.event.ColonyInformationChangedEvent;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Message to update the colony flag once set in the {@link com.minecolonies.core.client.gui.WindowBannerPicker}.
 */
public class ColonyFlagChangeMessage extends AbstractColonyServerMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "colony_flag_change", ColonyFlagChangeMessage::new);

    /** The chosen list of patterns from the window */
    private final BannerPatternLayers patterns;

    /**
     * Spawn a new change message
     * @param colony the colony the player changed the banner in
     * @param patternList the list of patterns they set in the banner picker
     */
    public ColonyFlagChangeMessage(final IColony colony, final BannerPatternLayers patternList)
    {
        super(TYPE, colony);
        this.patterns = patternList;
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
    {
        colony.setColonyFlag(patterns);
        try
        {
            NeoForge.EVENT_BUS.post(new ColonyInformationChangedEvent(colony, ColonyInformationChangedEvent.Type.FLAG));
        }
        catch (final Exception e)
        {
            Log.getLogger().error("Error during ColonyInformationChangedEvent", e);
        }
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        super.toBytes(buf);
        Utils.serializeCodecMess(BannerPatternLayers.STREAM_CODEC, buf, this.patterns);
    }

    protected ColonyFlagChangeMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.patterns = Utils.deserializeCodecMess(BannerPatternLayers.STREAM_CODEC, buf);
    }
}
