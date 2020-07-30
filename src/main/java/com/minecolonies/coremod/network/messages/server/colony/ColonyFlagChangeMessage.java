package com.minecolonies.coremod.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;

public class ColonyFlagChangeMessage extends AbstractColonyServerMessage
{
    private ListNBT patterns;

    public ColonyFlagChangeMessage () { super(); }

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
        nbt.put("Patterns", this.patterns);
        buf.writeCompoundTag(nbt);
    }

    @Override
    protected void fromBytesOverride(PacketBuffer buf)
    {
        CompoundNBT nbt = buf.readCompoundTag();
        if (nbt != null)
            this.patterns = nbt.getList("Patterns", Constants.TAG_COMPOUND);

    }
}
