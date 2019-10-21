package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingLumberjack;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class used for setting whether saplings should be planted after lj chops a tree.
 */
public class LumberjackRestrictionToggleMessage extends AbstractMessage<LumberjackRestrictionToggleMessage, IMessage>
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
     * Whether the lumberjack shouldbe restricted.
     */
    private boolean shouldRestrict;
    /**
     * The dimension of the message.
     */
    private int dimension;

    /**
     * Empty standard constructor.
     */
    public LumberjackRestrictionToggleMessage()
    {
        super();
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Creates a message which will be sent to set the restrict setting in the lumberjack.
     *
     * @param building      the building view of the lumberjack
     * @param shouldRestrict whether or not the lumberjack should be restricted.
     */
    public LumberjackRestrictionToggleMessage(final BuildingLumberjack.View building, final boolean shouldRestrict)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.shouldRestrict = shouldRestrict;
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        shouldRestrict = buf.readBoolean();
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeBoolean(shouldRestrict);
        buf.writeInt(dimension);
    }

    @Override
    public void messageOnServerThread(final LumberjackRestrictionToggleMessage message, final EntityPlayerMP player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null)
        {
            //Verify player has permission to change this hut's settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final AbstractBuildingWorker building = colony.getBuildingManager().getBuilding(message.buildingId, AbstractBuildingWorker.class);
            if (building instanceof BuildingLumberjack)
            {
                ((BuildingLumberjack) building).setShouldRestrict(message.shouldRestrict);
            }

        }
    }
}
