package com.minecolonies.core.network.messages.server.colony;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.event.ColonyInformationChangedEvent;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Message to execute the renaiming of the townHall.
 */
public class TownHallRenameMessage extends AbstractColonyServerMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "town_hall_rename", TownHallRenameMessage::new);

    private static final int    MAX_NAME_LENGTH  = 25;
    private static final int    SUBSTRING_LENGTH = MAX_NAME_LENGTH - 1;
    private final              String name;

    /**
     * Object creation for the town hall rename
     *
     * @param colony Colony the rename is going to occur in.
     * @param name   New name of the town hall.
     */
    public TownHallRenameMessage(@NotNull final IColonyView colony, final String name)
    {
        super(TYPE, colony);
        this.name = (name.length() <= MAX_NAME_LENGTH) ? name : name.substring(0, SUBSTRING_LENGTH);
    }

    protected TownHallRenameMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        final String nameLong = buf.readUtf(32767);
        name = (nameLong.length() <= MAX_NAME_LENGTH) ? nameLong : nameLong.substring(0, SUBSTRING_LENGTH);
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeUtf(name);
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
    {
        colony.setName(name);
        try
        {
            NeoForge.EVENT_BUS.post(new ColonyInformationChangedEvent(colony, ColonyInformationChangedEvent.Type.NAME));
        }
        catch (final Exception e)
        {
            Log.getLogger().error("Error during ColonyInformationChangedEvent", e);
        }
    }
}
