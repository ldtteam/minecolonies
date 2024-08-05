package com.minecolonies.core.network.messages.server.colony.building;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Message to set the style of a building.
 */
public class BuildingSetStyleMessage extends AbstractBuildingServerMessage<IBuilding>
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "building_set_style", BuildingSetStyleMessage::new);

    /**
     * The style to set.
     */
    private final String structurePack;

    /**
     * Creates object for the style of a building.
     *
     * @param building View of the building to read data from.
     * @param structurePack    style of the building.
     */
    public BuildingSetStyleMessage(@NotNull final IBuildingView building, final String structurePack)
    {
        super(TYPE, building);
        this.structurePack = structurePack;
    }

    protected BuildingSetStyleMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        structurePack = buf.readUtf(32767);
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeUtf(structurePack);
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final IBuilding building)
    {
        if (building.getBuildingLevel() > 0 && !building.isDeconstructed())
        {
            return;
        }

        building.setStructurePack(structurePack);
    }
}
