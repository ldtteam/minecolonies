package com.minecolonies.core.network.messages.client;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.core.client.gui.townhall.WindowTownHallDeleteAbandonColony;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Message to open the colony founding covenant.
 */
public class OpenDeleteAbandonColonyMessage implements IMessage
{
    /**
     * Colony pos at which we are trying to place.
     */
    private BlockPos currentTownHallPos;

    /**
     * Colony pos we are deleting or abandoning.
     */
    private BlockPos oldColonyPos;

    /**
     * Old colony name.
     */
    private String oldColonyName;

    /**
     * Old colony id.
     */
    private int oldColonyId;

    /**
     * Default constructor
     **/
    public OpenDeleteAbandonColonyMessage()
    {
        super();
    }

    public OpenDeleteAbandonColonyMessage(final BlockPos currentTownHallPos, final String oldColonyName, final BlockPos oldColonyPos, final int oldColonyId)
    {
        super();
        this.currentTownHallPos = currentTownHallPos;
        this.oldColonyName = oldColonyName;
        this.oldColonyPos = oldColonyPos;
        this.oldColonyId = oldColonyId;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer)
    {
        new WindowTownHallDeleteAbandonColony(Minecraft.getInstance().player, currentTownHallPos, Minecraft.getInstance().level, oldColonyName, oldColonyPos, oldColonyId).open();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeBlockPos(currentTownHallPos);
        buf.writeUtf(oldColonyName);
        buf.writeBlockPos(oldColonyPos);
        buf.writeInt(oldColonyId);
    }

    @Override
    public void fromBytes(FriendlyByteBuf buf)
    {
        this.currentTownHallPos = buf.readBlockPos();
        this.oldColonyName = buf.readUtf(32767);
        this.oldColonyPos = buf.readBlockPos();
        this.oldColonyId = buf.readInt();
    }
}
