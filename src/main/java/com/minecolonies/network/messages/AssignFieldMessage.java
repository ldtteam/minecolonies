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
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message which handles the assignment of fields to farmers.
 */
public class AssignFieldMessage extends AbstractMessage<AssignFieldMessage, IMessage>
{

    private int      colonyId;
    private BlockPos buildingId;
    private boolean  assign;
    private BlockPos field;

    /**
     * Empty standard constructor.
     */
    public AssignFieldMessage()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Creates the message to assign a field.
     *
     * @param building the farmer to assign to or release from.
     * @param assign   assign if true, free if false.
     * @param field    the field to assign or release.
     */
    public AssignFieldMessage(@NotNull BuildingFarmer.View building, boolean assign, BlockPos field)
    {
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.assign = assign;
        this.field = field;
    }

    @Override
    public void fromBytes(@NotNull ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        assign = buf.readBoolean();
        field = BlockPosUtil.readFromByteBuf(buf);
    }

    @Override
    public void toBytes(@NotNull ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeBoolean(assign);
        BlockPosUtil.writeToByteBuf(buf, field);
    }

    @Override
    public void messageOnServerThread(final AssignFieldMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {
            //Verify player has permission to do edit permissions
            if (!colony.getPermissions().hasPermission(player, Permissions.Action.ACCESS_HUTS))
            {
                return;
            }

            @Nullable final BuildingFarmer building = colony.getBuilding(message.buildingId, BuildingFarmer.class);
            if (building != null)
            {
                if (message.assign)
                {
                    building.assignField(message.field);
                }
                else
                {
                    building.freeField(message.field);
                }
            }
        }
    }
}

