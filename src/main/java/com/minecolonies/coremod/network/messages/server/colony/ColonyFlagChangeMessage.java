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
    private ListNBT patterns;

    /** Default constructor **/
    public ColonyFlagChangeMessage () { super(); }

    /**
     * Spawn a new change message
     * @param colony the colony the player changed the banner in
     * @param patternList the list of patterns they set in the banner picker
     */
    public ColonyFlagChangeMessage (IColony colony, ListNBT patternList)
    {
        super(colony);

        this.patterns = patternList;
    }

    @Override
    protected void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer, IColony colony)
    {
        colony.setColonyFlag(patterns);
    }

    @Override
    protected void toBytesOverride(PacketBuffer buf)
    {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put(TAG_BANNER_PATTERNS, this.patterns);
        buf.writeNbt(nbt);
    }

    @Override
    protected void fromBytesOverride(PacketBuffer buf)
    {
        CompoundNBT nbt = buf.readNbt();
        if (nbt != null)
            this.patterns = nbt.getList(TAG_BANNER_PATTERNS, Constants.TAG_COMPOUND);
    }
}
