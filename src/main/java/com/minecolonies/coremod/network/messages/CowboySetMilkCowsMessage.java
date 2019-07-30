package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCowboy;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CowboySetMilkCowsMessage implements IMessage
{
    private int      colonyId;
    private BlockPos buildingId;
    private boolean milkCows;

    /**
     * The dimension of the 
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
     * Creates object for the CowboySetMilk 
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
    public void fromBytes(final PacketBuffer byteBuf)
    {
        colonyId = byteBuf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(byteBuf);
        milkCows = byteBuf.readBoolean();
        dimension = byteBuf.readInt();
    }

    @Override
    public void toBytes(final PacketBuffer byteBuf)
    {
        byteBuf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(byteBuf, buildingId);
        byteBuf.writeBoolean(milkCows);
        byteBuf.writeInt(dimension);
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
        if (colony != null)
        {
            final PlayerEntity player = ctxIn.getSender();

            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final BuildingCowboy building = colony.getBuildingManager().getBuilding(buildingId, BuildingCowboy.class);
            if (building != null)
            {
                building.setMilkCows(milkCows);
            }
        }
    }
}
