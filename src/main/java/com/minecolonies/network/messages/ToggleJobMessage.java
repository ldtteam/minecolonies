package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.ColonyView;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Message class which manages the message to toggle automatic or manual job allocation.
 */
public class ToggleJobMessage extends AbstractMessage<ToggleJobMessage, IMessage>
{
    /**
     * The Colony ID;
     */
    private int     colonyId;
    /**
     * Toggle the job allocation to true or false.
     */
    private boolean toggle;

    /**
     * Empty public constructor.
     */
    public ToggleJobMessage()
    {
        super();
    }

    /**
     * Creates object for the player to turn manual allocation or or off.
     *
     * @param colony view of the colony to read data from
     * @param toggle toggle the job to manually or automatically
     */
    public ToggleJobMessage(@NotNull ColonyView colony, boolean toggle)
    {
        super();
        this.colonyId = colony.getID();
        this.toggle = toggle;
    }

    /**
     * Transformation from a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void fromBytes(@NotNull ByteBuf buf)
    {
        colonyId = buf.readInt();
        toggle = buf.readBoolean();
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void toBytes(@NotNull ByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeBoolean(toggle);
    }

    @Override
    public void messageOnServerThread(final ToggleJobMessage message, final EntityPlayerMP player)
    {
        Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {
            colony.setManualHiring(message.toggle);
        }
    }
}
