package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Message to trigger a response handler on the server side.
 */
public class TriggerServerResponseHandlerMessage extends AbstractMessage<TriggerServerResponseHandlerMessage, IMessage>
{
    /**
     * The Colony ID.
     */
    private int colonyId;

    /**
     * Id of the citizen.
     */
    private int citizenId;

    /**
     * The key of the handler to trigger.
     */
    private ITextComponent key;

    /**
     * The chosen response.
     */
    private ITextComponent response;

    /**
     * The dimension of the
     */
    private int dimension;

    /**
     * Empty public constructor.
     */
    public TriggerServerResponseHandlerMessage()
    {
        super();
    }

    /**
     * Trigger the server response handler.
     * @param colonyId the colony id.
     * @param citizenId the citizen id.
     * @param dimension the dimension the colony and citizen are in.
     * @param key the key of the handler.
     * @param response the response to trigger.
     */
    public TriggerServerResponseHandlerMessage(final int colonyId, final int citizenId, final int dimension, @NotNull final ITextComponent key, @NotNull final ITextComponent response)
    {
        super();
        this.colonyId = colonyId;
        this.citizenId = citizenId;
        this.dimension = dimension;
        this.key = key;
        this.response = response;
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        this.colonyId = buf.readInt();
        this.citizenId = buf.readInt();
        this.dimension = buf.readInt();

        this.key = ITextComponent.Serializer.jsonToComponent(ByteBufUtils.readUTF8String(buf));
        this.response = ITextComponent.Serializer.jsonToComponent(ByteBufUtils.readUTF8String(buf));
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(this.colonyId);
        buf.writeInt(this.citizenId);
        buf.writeInt(this.dimension);

        ByteBufUtils.writeUTF8String(buf, ITextComponent.Serializer.componentToJson(key));
        ByteBufUtils.writeUTF8String(buf, ITextComponent.Serializer.componentToJson(key));
    }

    @Override
    public void messageOnServerThread(final TriggerServerResponseHandlerMessage message, final EntityPlayerMP player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null && colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            final ICitizenData citizenData = colony.getCitizenManager().getCitizen(message.citizenId);
            if (citizenData != null)
            {
                citizenData.onResponseTriggered(message.key, message.response, player.getServerWorld());
            }
        }
    }
}


