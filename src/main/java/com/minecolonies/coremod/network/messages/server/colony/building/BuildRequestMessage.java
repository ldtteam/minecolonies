package com.minecolonies.coremod.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Adds a entry to the builderRequired map. Created: May 26, 2014
 *
 * @author Colton
 */
public class BuildRequestMessage extends AbstractBuildingServerMessage<IBuilding>
{
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
    private Mode mode;

    /**
     * The id of the building.
     */
    private BlockPos builder;

    /**
     * Empty constructor used when registering the
     */
    public BuildRequestMessage()
    {
        super();
    }

    /**
     * Creates a build request
     *
     * @param building the building we're executing on.
     * @param mode     Mode of the request, 1 is repair, 0 is build.
     * @param builder  the builder we're assinging the request to
     */
    public BuildRequestMessage(@NotNull final IBuildingView building, final Mode mode, final BlockPos builder)
    {
        super(building);
        this.mode = mode;
        this.builder = builder;
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {
        mode = Mode.values()[buf.readInt()];
        builder = buf.readBlockPos();
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(mode.ordinal());
        buf.writeBlockPos(builder);
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        final PlayerEntity player = ctxIn.getSender();
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
                default:
                    break;
            }
        }
    }
}
