package com.minecolonies.coremod.network.messages;

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
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        shouldReplant = buf.readBoolean();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeBoolean(shouldReplant);
    }

    @Override
    public void messageOnServerThread(final LumberjackReplantSaplingToggleMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColony(message.colonyId);
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
                ((BuildingLumberjack) building).setShouldReplant(message.shouldReplant);
            }

        }
    }
}
