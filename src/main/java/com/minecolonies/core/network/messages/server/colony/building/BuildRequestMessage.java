package com.minecolonies.core.network.messages.server.colony.building;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Adds a entry to the builderRequired map. Created: May 26, 2014
 *
 * @author Colton
 */
public class BuildRequestMessage extends AbstractBuildingServerMessage<IBuilding>
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "build_request", BuildRequestMessage::new);

    /**
     * The request mode.
     */
    public enum Mode
    {
        BUILD,
        REPAIR,
        REMOVE
    }

    /**
     * The mode id.
     */
    private final Mode mode;

    /**
     * The id of the building.
     */
    private final BlockPos builder;

    /**
     * Creates a build request
     *
     * @param building the building we're executing on.
     * @param mode     Mode of the request, 1 is repair, 0 is build.
     * @param builder  the builder we're assinging the request to
     */
    public BuildRequestMessage(@NotNull final IBuildingView building, final Mode mode, final BlockPos builder)
    {
        super(TYPE, building);
        this.mode = mode;
        this.builder = builder;
    }

    protected BuildRequestMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        mode = Mode.values()[buf.readInt()];
        builder = buf.readBlockPos();
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeInt(mode.ordinal());
        buf.writeBlockPos(builder);
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final IBuilding building)
    {
        if (building.hasWorkOrder())
        {
            building.removeWorkOrder();
        }
        else
        {
            switch (mode)
            {
                case BUILD:
                    building.requestUpgrade(player, builder);
                    break;
                case REPAIR:
                    building.requestRepair(builder);
                    break;
                case REMOVE:
                    building.requestRemoval(player, builder);
                    for (final BlockPos childPos : building.getChildren())
                    {
                        final IBuilding childBuilding = colony.getBuildingManager().getBuilding(childPos);
                        if (childBuilding != null)
                        {
                            childBuilding.requestRemoval(player, builder);
                        }
                    }
                default:
                    break;
            }
        }
    }
}
