package com.minecolonies.core.network.messages.server.colony;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.event.ColonyInformationChangedEvent;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BANNER_PATTERNS;

/**
 * Message to update the colony flag once set in the {@link com.minecolonies.core.client.gui.WindowBannerPicker}.
 */
public class ColonyFlagChangeMessage extends AbstractColonyServerMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "colony_flag_change", ColonyFlagChangeMessage::new);

    /** The chosen list of patterns from the window */
    private final ListTag patterns;

    /**
     * Spawn a new change message
     * @param colony the colony the player changed the banner in
     * @param patternList the list of patterns they set in the banner picker
     */
    public ColonyFlagChangeMessage(final IColony colony, final ListTag patternList)
    {
        super(TYPE, colony);

        this.patterns = patternList;
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
    {
        colony.setColonyFlag(patterns);

        NeoForge.EVENT_BUS.post(new ColonyInformationChangedEvent(colony, ColonyInformationChangedEvent.Type.FLAG));
    }

    @Override
    protected void toBytes(final FriendlyByteBuf buf)
    {
        super.toBytes(buf);
        final CompoundTag nbt = new CompoundTag();
        nbt.put(TAG_BANNER_PATTERNS, this.patterns);
        buf.writeNbt(nbt);
    }

    protected ColonyFlagChangeMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.patterns = buf.readNbt().getList(TAG_BANNER_PATTERNS, Constants.TAG_COMPOUND);
    }
}
