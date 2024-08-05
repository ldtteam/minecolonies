package com.minecolonies.core.network.messages.server.colony.building;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.ItemListModule;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Message which handles the reset of items to filterable item lists.
 */
public class ResetFilterableItemMessage extends AbstractBuildingServerMessage<AbstractBuilding>
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "reset_filterable_item", ResetFilterableItemMessage::new);

    /**
     * The id of the list.
     */
    private final int id;

    /**
     * Creates the message to reset a list..
     *
     * @param id       the id of the list of filterables.
     * @param building the building we're executing on.
     */
    public ResetFilterableItemMessage(final IBuildingView building, final int id)
    {
        super(TYPE, building);
        this.id = id;
    }

    protected ResetFilterableItemMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.id = buf.readInt();
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeInt(this.id);
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final AbstractBuilding building)
    {
        if (building.getModule(id) instanceof final ItemListModule module)
        {
            module.resetToDefaults();
        }
    }
}
