package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.buildings.BuildingMiner;
import com.minecolonies.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Message class which manages the message to toggle automatic or manual job allocation.
 */
public class ToggleJobMessage implements IMessage, IMessageHandler<ToggleJobMessage, IMessage>
{
    /**
     * The Colony ID;
     */
    private int              colonyId;
    /**
     * Toggle the job allocation to true or false.
     */
    private boolean          toggle;

    public ToggleJobMessage(){}

    /**
     * Creates object for the player to turn manual allocation or or off.
     *
     * @param building       View of the building to read data from
     * @param toggle         toggle the job to manually or automatically
     */
    public ToggleJobMessage(Building.View building, boolean toggle)
    {
        this.colonyId = building.getColony().getID();
        this.toggle   = toggle;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeBoolean(toggle);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = buf.readInt();
        toggle   = buf.readBoolean();
    }

    /**
     * Called when a message has been received.
     * @param message the message.
     * @param ctx the context.
     * @return possible response, in this case -> null.
     */
    @Override
    public IMessage onMessage(ToggleJobMessage message, MessageContext ctx)
    {
        Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {
            colony.setManuallyAllocateJobs(message.toggle);
        }
        return null;
    }
}
