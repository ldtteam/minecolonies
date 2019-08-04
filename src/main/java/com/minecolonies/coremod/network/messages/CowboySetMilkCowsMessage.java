package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCowboy;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CowboySetMilkCowsMessage implements IMessage
{
    private int      colonyId;
    private BlockPos buildingId;
    private boolean milkCows;

    /**
     * The dimension of the message.
     */
    private int dimension;

    /**
     * Empty standard constructor.
     */
    public CowboySetMilkCowsMessage()
    {
        super();
    }

    /**
     * Creates object for the CowboySetMilk message.
     *
     * @param building       View of the building to read data from.
     * @param milkCows       Whether Cowboy should milk cows.
     */
    public CowboySetMilkCowsMessage(@NotNull final BuildingCowboy.View building, final boolean milkCows)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.milkCows = milkCows;
        this.dimension = building.getColony().getDimension();
    }


    @Override
    public void fromBytes(final ByteBuf byteBuf)
    {
        colonyId = byteBuf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(byteBuf);
        milkCows = byteBuf.readBoolean();
        dimension = byteBuf.readInt();
    }

    @Override
    public void toBytes(final ByteBuf byteBuf)
    {
        byteBuf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(byteBuf, buildingId);
        byteBuf.writeBoolean(milkCows);
        byteBuf.writeInt(dimension);
    }

    @Override
    public void messageOnServerThread(final CowboySetMilkCowsMessage message, final ServerPlayerEntity player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final BuildingCowboy building = colony.getBuildingManager().getBuilding(message.buildingId, BuildingCowboy.class);
            if (building != null)
            {
                building.setMilkCows(message.milkCows);
            }
        }
    }
}
