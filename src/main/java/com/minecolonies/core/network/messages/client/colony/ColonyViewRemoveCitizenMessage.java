package com.minecolonies.core.network.messages.client.colony;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.core.colony.Colony;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Add or Update a ColonyView on the client.
 */
public class ColonyViewRemoveCitizenMessage implements IMessage
{
    private int colonyId;
    private int citizenId;

    /**
     * Empty constructor used when registering the
     */
    public ColonyViewRemoveCitizenMessage()
    {
        super();
    }

    /**
     * Creates an object for the remove message for citizen.
     *
     * @param colony  Colony the citizen is in.
     * @param citizen Citizen ID.
     */
    public ColonyViewRemoveCitizenMessage(@NotNull final Colony colony, final int citizen)
    {
        this.colonyId = colony.getID();
        this.citizenId = citizen;
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        colonyId = buf.readInt();
        citizenId = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(citizenId);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        if (Minecraft.getInstance().level != null)
        {
            IColonyManager.getInstance().handleColonyViewRemoveCitizenMessage(colonyId, citizenId, Minecraft.getInstance().level.dimension());
        }
    }
}
