package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.BuildingGuardTower;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message to set the position of the guard task.
 */
public class GuardTargetMessage extends AbstractMessage<GuardTargetMessage, IMessage>
{
    /**
     * The id of the colony.
     */
    private int colonyId;


    /**
     * The position of the guard tower.
     */
    private BlockPos guardTower;

    /**
     * The position of the task.
     */
    private BlockPos position;

    /**
     * Defines if this is the first position set in the session.
     * If true then delete the old list, else add to the old list.
     */
    private boolean first;

    /**
     * Empty standard constructor.
     */
    public GuardTargetMessage()
    {
        super();
    }

    /**
     * Creates an object for the GuardTargetMessage.
     * @param colonyId the colony id.
     * @param guardTower the position of the guardTower.
     * @param position the position to add.
     * @param first is this the first position being set in the current session.
     */
    public GuardTargetMessage(int colonyId, @NotNull BlockPos guardTower, @NotNull BlockPos position, boolean first)
    {
        super();
        this.colonyId = colonyId;
        this.guardTower = guardTower;
        this.position = position;
        this.first = first;
    }

    @Override
    public void fromBytes(@NotNull ByteBuf buf)
    {
        colonyId = buf.readInt();
        guardTower = BlockPosUtil.readFromByteBuf(buf);
        position = BlockPosUtil.readFromByteBuf(buf);
        first = buf.readBoolean();
    }

    @Override
    public void toBytes(@NotNull ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, guardTower);
        BlockPosUtil.writeToByteBuf(buf, position);
        buf.writeBoolean(first);
    }

    @Override
    public void messageOnServerThread(final GuardTargetMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Permissions.Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final BuildingGuardTower building = colony.getBuilding(message.guardTower, BuildingGuardTower.class);
            if (building != null)
            {
                if(building.getTask().equals(BuildingGuardTower.Task.GUARD))
                {
                    building.setGuardTarget(message.position);
                }
                else
                {
                    if(message.first)
                    {
                        building.resetPatrolTargets();
                    }
                    building.addPatrolTargets(message.position);
                }
            }
        }
    }
}
