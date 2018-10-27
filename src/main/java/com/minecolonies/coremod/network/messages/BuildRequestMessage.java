package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Adds a entry to the builderRequired map.
 * Created: May 26, 2014
 *
 * @author Colton
 */
public class BuildRequestMessage extends AbstractMessage<BuildRequestMessage, IMessage>
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
    public BuildRequestMessage(@NotNull final AbstractBuildingView building, final int mode)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.mode = mode;
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        mode = buf.readInt();
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeInt(mode);
        buf.writeInt(dimension);
    }

    @Override
    public void messageOnServerThread(final BuildRequestMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColonyByDimension(message.colonyId, message.dimension);
        if (colony == null)
        {
            return;
        }

        final AbstractBuilding building = colony.getBuildingManager().getBuilding(message.buildingId);
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
                    building.requestUpgrade(player);
                    break;
                case REPAIR:
                    building.requestRepair();
                    break;
                default:
                    break;
            }
        }
    }
}
