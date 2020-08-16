package com.minecolonies.coremod.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BANNER_PATTERNS;

/**
 * Message to update the colony flag once set in the {@link com.minecolonies.coremod.client.gui.WindowBannerPicker}.
 */
public class ColonyFlagChangeMessage extends AbstractColonyServerMessage
{
    /** The chosen list of patterns from the window */
    private final ListNBT patterns;

    /** Default constructor **/
    public ColonyFlagChangeMessage(final PacketBuffer buf)
    {
        super(buf);
        final CompoundNBT nbt = buf.readCompoundTag();
        this.patterns = nbt.getList(TAG_BANNER_PATTERNS, Constants.TAG_COMPOUND);
    }

    /**
     * Spawn a new change message
     * @param colony the colony the player changed the banner in
     * @param patternList the list of patterns they set in the banner picker
     */
    public ColonyFlagChangeMessage(final IColony colony, final ListNBT patternList)
    {
        super(colony);
        this.patterns = patternList;
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        colony.setColonyFlag(patterns);
    }

    @Override
    protected void toBytesOverride(final PacketBuffer buf)
    {
        final CompoundNBT nbt = new CompoundNBT();
        nbt.put(TAG_BANNER_PATTERNS, this.patterns);
        buf.writeCompoundTag(nbt);
    }
}
