package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.colony.buildings.IBuilding;
import com.minecolonies.coremod.colony.buildings.views.IBuildingView;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Adds a entry to the builderRequired map.
 * Created: May 26, 2014
 *
 * @author Colton
 */
public class BuildRequestMessage implements IMessage
{
    /**
     * The int mode for a build job.
     */
    public static final int BUILD  = 0;

    /**
     * The int mode for a repair job.
     */
    public static final int REPAIR = 1;

    /**
     * The id of the building.
     */
    private BlockPos buildingId;

    /**
     * The id of the colony.
     */
    private int      colonyId;

    /**
     * The mode id.
     */
    private int      mode;

    /**
     * The dimension of the 
     */
    private int dimension;

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
     * @param building AbstractBuilding of the request.
     * @param mode     Mode of the request, 1 is repair, 0 is build.
     */
    public BuildRequestMessage(@NotNull final IBuildingView building, final int mode, final BlockPos builder)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.mode = mode;
        this.dimension = building.getColony().getDimension();
        this.builder = builder;
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        buildingId = buf.readBlockPos();
        mode = buf.readInt();
        dimension = buf.readInt();
        builder = buf.readBlockPos();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeBlockPos(buildingId);
        buf.writeInt(mode);
        buf.writeInt(dimension);
        buf.writeBlockPos(builder);
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
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, dimension);
        if (colony == null)
        {
            return;
        }
        final PlayerEntity player = ctxIn.getSender();
        final IBuilding building = colony.getBuildingManager().getBuilding(buildingId);
        if (building == null)
        {
            return;
        }

        //Verify player has permission to change this huts settings
        if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
        {
            return;
        }

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
                default:
                    break;
            }
        }
    }
}
