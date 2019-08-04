package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingLumberjack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class used for setting whether saplings should be planted after lj chops a tree.
 */
public class LumberjackReplantSaplingToggleMessage implements IMessage
{

    /**
     * The colony id
     */
    private int colonyId;

    /**
     * The lumberjack's building id.
     */
    private BlockPos buildingId;

    /**
     * Whether the lumberjack should replant a sapling or not.
     */
    private boolean shouldReplant;

    /**
     * The dimension of the 
     */
    private int dimension;

    /**
     * Empty standard constructor.
     */
    public LumberjackReplantSaplingToggleMessage()
    {
        super();
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Creates a message which will be sent to set the replant setting in the lumberjack.
     *
     * @param building      the building view of the lumberjack
     * @param shouldReplant whether or not the sapling should be replanted.
     */
    public LumberjackReplantSaplingToggleMessage(final BuildingLumberjack.View building, final boolean shouldReplant)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.shouldReplant = shouldReplant;
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        buildingId = buf.readBlockPos();
        shouldReplant = buf.readBoolean();
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeBlockPos(buildingId);
        buf.writeBoolean(shouldReplant);
        buf.writeInt(dimension);
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
            //Verify player has permission to change this hut's settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final IBuildingWorker building = colony.getBuildingManager().getBuilding(buildingId, AbstractBuildingWorker.class);
            if (building instanceof BuildingLumberjack)
            {
                ((BuildingLumberjack) building).setShouldReplant(shouldReplant);
            }

        }
    }
}
