package com.minecolonies.core.network.messages.client;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.core.client.gui.townhall.WindowTownHallColonyManage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Message to open the colony founding covenant.
 */
public class OpenColonyFoundingCovenantMessage implements IMessage
{
    /**
     * Info on the closest colony.
     */
    private String closestName;
    private int      closestDistance;
    private BlockPos townHallPos;

    /**
     * Default constructor
     **/
    public OpenColonyFoundingCovenantMessage()
    {
        super();
    }

    public OpenColonyFoundingCovenantMessage(final String closestName, final int closestDistance, final BlockPos townHallPos)
    {
        super();
        this.closestName = closestName;
        this.closestDistance = closestDistance;
        this.townHallPos = townHallPos;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer)
    {
        new WindowTownHallColonyManage(Minecraft.getInstance().player, townHallPos, Minecraft.getInstance().level, closestName, closestDistance, "", false).open();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeUtf(closestName);
        buf.writeInt(closestDistance);
        buf.writeBlockPos(townHallPos);
    }

    @Override
    public void fromBytes(FriendlyByteBuf buf)
    {
        this.closestName = buf.readUtf(32767);
        this.closestDistance = buf.readInt();
        this.townHallPos = buf.readBlockPos();
    }
}
