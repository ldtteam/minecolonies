package com.minecolonies.coremod.network.messages;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.util.BlockPosUtil;
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
    public BuildRequestMessage(@NotNull final AbstractBuilding.View building, final int mode)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.mode = mode;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        mode = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeInt(mode);
    }

    @Override
    public void messageOnServerThread(final BuildRequestMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony == null)
        {
            return;
        }

        final AbstractBuilding building = colony.getBuilding(message.buildingId);
        if (building == null)
        {
            return;
        }

        //Verify player has permission to change this huts settings
        if (!colony.getPermissions().hasPermission(player, Permissions.Action.MANAGE_HUTS))
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
                    building.requestUpgrade();
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
