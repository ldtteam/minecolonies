package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.Nullable;

public class GuardRecalculateMessage extends AbstractMessage<GuardRecalculateMessage, IMessage>
{
    /**
     * The position of the building.
     */
    private BlockPos buildingId;

    /**
     * The colony the building is within.
     */
    private int colonyId;

    /**
     * Empty standard constructor.
     */
    public GuardRecalculateMessage()
    {
        super();
    }

    /**
     * Creates a new message of this type to set the guard scepter in the player inventory.
     *
     * @param buildingId the position of the building.
     */
    public GuardRecalculateMessage(final int colonyId, final BlockPos buildingId)
    {
        super();
        this.colonyId = colonyId;
        this.buildingId = buildingId;
    }

    @Override
    public void fromBytes(final ByteBuf byteBuf)
    {
        this.colonyId = byteBuf.readInt();
        this.buildingId = BlockPosUtil.readFromByteBuf(byteBuf);
    }

    @Override
    public void toBytes(final ByteBuf byteBuf)
    {
        byteBuf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(byteBuf, buildingId);
    }

    @Override
    public void messageOnServerThread(final GuardRecalculateMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final AbstractBuildingGuards building = colony.getBuildingManager().getBuilding(message.buildingId, AbstractBuildingGuards.class);
            if (building != null)
            {
                building.setMobsToAttack(building.calculateMobs());
            }
        }
    }
}
