package com.minecolonies.core.network.messages.server.colony.building;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Message to execute the renaming of the townHall.
 */
public class HutRenameMessage extends AbstractBuildingServerMessage<IBuilding>
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "hut_rename", HutRenameMessage::new);

    /**
     * The custom name to set.
     */
    private final String name;

    /**
     * Object creation for the town hall rename
     *
     * @param name     New name of the town hall.
     * @param building the building we're executing on.
     */
    public HutRenameMessage(@NotNull final IBuildingView building, final String name)
    {
        super(TYPE, building);
        this.name = name;
    }

    protected HutRenameMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);

        name = buf.readUtf(32767);
    }

    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        super.toBytes(buf);

        buf.writeUtf(name);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final IBuilding building)
    {
        building.setCustomBuildingName(name);
    }
}
