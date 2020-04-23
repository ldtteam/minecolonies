package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingShepherd;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
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
     * Creates object for the CowboySetMilk 
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
    public void fromBytes(final PacketBuffer byteBuf)
    {
        colonyId = byteBuf.readInt();
        buildingId = byteBuf.readBlockPos();
        dyeSheeps = byteBuf.readBoolean();
        dimension = byteBuf.readInt();
    }

    @Override
    public void toBytes(final PacketBuffer byteBuf)
    {
        byteBuf.writeInt(colonyId);
        byteBuf.writeBlockPos(buildingId);
        byteBuf.writeBoolean(dyeSheeps);
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

            @Nullable final BuildingShepherd building = colony.getBuildingManager().getBuilding(buildingId, BuildingShepherd.class);
            if (building != null)
            {
                building.setDyeSheeps(dyeSheeps);
            }
        }
    }
}
