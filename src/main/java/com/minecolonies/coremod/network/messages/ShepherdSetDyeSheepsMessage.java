package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingShepherd;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Transfer the current state of automatical sheep dyeing (true = enabled)
 */
public class ShepherdSetDyeSheepsMessage implements IMessage
{
    private int      colonyId;
    private BlockPos buildingId;
    private boolean  dyeSheeps;
    private int      dimension;

    /**
     * Empty standard constructor.
     */
    public ShepherdSetDyeSheepsMessage()
    {
        super();
    }

    /**
     * Creates object for the CowboySetMilk message.
     *
     * @param building View of the building to read data from.
     */
    public ShepherdSetDyeSheepsMessage(@NotNull final BuildingShepherd.View building)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.dyeSheeps = building.isDyeSheeps();
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void fromBytes(final ByteBuf byteBuf)
    {
        colonyId = byteBuf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(byteBuf);
        dyeSheeps = byteBuf.readBoolean();
        dimension = byteBuf.readInt();
    }

    @Override
    public void toBytes(final ByteBuf byteBuf)
    {
        byteBuf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(byteBuf, buildingId);
        byteBuf.writeBoolean(dyeSheeps);
        byteBuf.writeInt(dimension);
    }

    @Override
    public void messageOnServerThread(final ShepherdSetDyeSheepsMessage message, final ServerPlayerEntity player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final BuildingShepherd building = colony.getBuildingManager().getBuilding(message.buildingId, BuildingShepherd.class);
            if (building != null)
            {
                building.setDyeSheeps(message.dyeSheeps);
            }
        }
    }
}
