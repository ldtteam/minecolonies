package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

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
     * The dimension of the message.
     */
    private int dimension;

    /**
     * The id of the building.
     */
    private BlockPos builder;


    /**
     * Empty constructor used when registering the message.
     */
    public BuildRequestMessage()
    {
        super();
    }

    /**
     * Creates a build request message.
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
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        mode = buf.readInt();
        dimension = buf.readInt();
        builder = BlockPosUtil.readFromByteBuf(buf);
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeInt(mode);
        buf.writeInt(dimension);
        BlockPosUtil.writeToByteBuf(buf, builder);
    }

    @Override
    public void messageOnServerThread(final BuildRequestMessage message, final ServerPlayerEntity player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony == null)
        {
            return;
        }

        final IBuilding building = colony.getBuildingManager().getBuilding(message.buildingId);
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
            switch (message.mode)
            {
                case BUILD:
                    building.requestUpgrade(player, message.builder);
                    break;
                case REPAIR:
                    building.requestRepair(message.builder);
                    break;
                default:
                    break;
            }
        }
    }
}
