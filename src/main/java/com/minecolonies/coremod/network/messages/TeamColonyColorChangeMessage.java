package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Message class which manages changing the team color of the colony.
 */
public class TeamColonyColorChangeMessage extends AbstractMessage<TeamColonyColorChangeMessage, IMessage>
{
    /**
     * The Colony ID.
     */
    private int colonyId;

    /**
     * The color to set.
     */
    private int colorOrdinal;

    /**
     * The dimension of the message.
     */
    private int dimension;

    /**
     * Empty public constructor.
     */
    public TeamColonyColorChangeMessage()
    {
        super();
    }

    /**
     * Creates object for the player to handle the color message.
     *
     * @param colorOrdinal the color to set.
     * @param building  view of the building to read data from
     */
    public TeamColonyColorChangeMessage(final int colorOrdinal, @NotNull final AbstractBuildingView building)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.colorOrdinal = colorOrdinal;
        this.dimension = building.getColony().getDimension();
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        colorOrdinal = buf.readInt();
        dimension = buf.readInt();
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(colorOrdinal);
        buf.writeInt(dimension);
    }

    @Override
    public void messageOnServerThread(final TeamColonyColorChangeMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }
            colony.setColonyColor(TextFormatting.values()[message.colorOrdinal]);
        }
    }
}
