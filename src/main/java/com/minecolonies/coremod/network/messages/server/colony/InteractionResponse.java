package com.minecolonies.coremod.network.messages.server.colony;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to trigger a response handler on the server side.
 */
public class InteractionResponse extends AbstractColonyServerMessage
{
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
     * Empty public constructor.
     */
    public InteractionResponse()
    {
        super();
    }

    /**
     * Trigger the server response handler.
     *
     * @param colonyId  the colony id.
     * @param citizenId the citizen id.
     * @param dimension the dimension the colony and citizen are in.
     * @param key       the key of the handler.
     * @param response  the response to trigger.
     */
    public InteractionResponse(
      final int colonyId,
      final int citizenId,
      final RegistryKey<World> dimension,
      @NotNull final ITextComponent key,
      @NotNull final ITextComponent response)
    {
        super(dimension, colonyId);
        this.citizenId = citizenId;
        this.key = key;
        this.response = response;
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {
        this.citizenId = buf.readInt();
        this.key = buf.readComponent();
        this.response = buf.readComponent();
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(this.citizenId);
        buf.writeComponent(key);
        buf.writeComponent(response);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        ICitizenData citizenData = colony.getCitizenManager().getCivilian(citizenId);
        if (citizenData == null)
        {
            citizenData = colony.getVisitorManager().getVisitor(citizenId);
        }

        if (citizenData != null && ctxIn.getSender() != null)
        {
            citizenData.onResponseTriggered(key, response, ctxIn.getSender());
        }
    }
}


