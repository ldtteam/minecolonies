package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.IBuildingWorker;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingLumberjack;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntityMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class used for setting whether saplings should be planted after lj chops a tree.
 */
public class LumberjackReplantSaplingToggleMessage extends AbstractMessage<LumberjackReplantSaplingToggleMessage, IMessage>
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
     * The dimension of the message.
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
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        shouldReplant = buf.readBoolean();
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeBoolean(shouldReplant);
        buf.writeInt(dimension);
    }

    @Override
    public void messageOnServerThread(final LumberjackReplantSaplingToggleMessage message, final PlayerEntityMP player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null)
        {
            //Verify player has permission to change this hut's settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final IBuildingWorker building = colony.getBuildingManager().getBuilding(message.buildingId, AbstractBuildingWorker.class);
            if (building instanceof BuildingLumberjack)
            {
                ((BuildingLumberjack) building).setShouldReplant(message.shouldReplant);
            }

        }
    }
}
