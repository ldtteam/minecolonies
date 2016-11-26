package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.BuildingFarmer;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message to change the behavior of the guards..
 */
public class GuardTaskMessage extends AbstractMessage<GuardTaskMessage, IMessage>
{
    //todo assignment (range, knight)
    //todo patrol (manual, auto)
    //todo assign (manual, auto)
    //todo retrieve (off /10% / 20%)
    //todo task - patrol, follow, guard
    private int      colonyId;
    private BlockPos buildingId;
    private boolean  assignmentMode;

    /**
     * Empty standard constructor.
     */
    public GuardTaskMessage()
    {
        super();
    }

    /**
     * Creates object for the assignmentMode message.
     *
     * @param building       View of the building to read data from.
     * @param assignmentMode assignmentMode of the particular farmer.
     */
    public GuardTaskMessage(@NotNull BuildingFarmer.View building, boolean assignmentMode)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.assignmentMode = assignmentMode;
    }

    @Override
    public void fromBytes(@NotNull ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        assignmentMode = buf.readBoolean();
    }

    @Override
    public void toBytes(@NotNull ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeBoolean(assignmentMode);
    }

    @Override
    public void messageOnServerThread(final GuardTaskMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Permissions.Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final BuildingFarmer building = colony.getBuilding(message.buildingId, BuildingFarmer.class);
            if (building != null)
            {
                building.setAssignManually(message.assignmentMode);
            }
        }
    }
}
