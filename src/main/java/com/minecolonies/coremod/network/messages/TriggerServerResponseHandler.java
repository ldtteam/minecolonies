package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.network.IMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message to trigger a response handler on the server side.
 */
public class TriggerServerResponseHandler implements IMessage
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
    public TriggerServerResponseHandler()
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
    public TriggerServerResponseHandler(final int colonyId, final int citizenId, final int dimension, @NotNull final ITextComponent key, @NotNull final ITextComponent response)
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
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        this.colonyId = buf.readInt();
        this.citizenId = buf.readInt();
        this.dimension = buf.readInt();

        this.key =  buf.readTextComponent();
        this.response = buf.readTextComponent();
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(this.colonyId);
        buf.writeInt(this.citizenId);
        buf.writeInt(this.dimension);

        buf.writeTextComponent(key);
        buf.writeTextComponent(response);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final PlayerEntity player = ctxIn.getSender();
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, dimension);
        if (colony != null && colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            final ICitizenData citizenData = colony.getCitizenManager().getCitizen(citizenId);
            if (citizenData != null)
            {
                citizenData.onResponseTriggered(key, response, ctxIn.getSender().world);
            }
        }
    }
}


