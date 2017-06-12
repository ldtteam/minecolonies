package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Send a message to the server to mark the building as dirty.
 * Created: January 20, 2017
 *
 * @author xavierh
 */
public class MarkBuildingDirtyMessage  extends AbstractMessage<MarkBuildingDirtyMessage, IMessage>
{
    /**
     * The id of the building.
     */
    private BlockPos buildingId;
    /**
     * The id of the colony.
     */
    private int      colonyId;

    /**
     * Empty constructor used when registering the message.
     */
    public MarkBuildingDirtyMessage()
    {
        super();
    }

    /**
     * Creates a mark building dirty request message.
     *
     * @param building AbstractBuilding of the request.
     */
    public MarkBuildingDirtyMessage(@NotNull final AbstractBuilding.View building)
    {
        super();
        this.colonyId   = building.getColony().getID();
        this.buildingId = building.getID();

    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId   = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
    }

    @Override
    public void messageOnServerThread(final MarkBuildingDirtyMessage message, final EntityPlayerMP player)
    {

        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony == null)
        {
            Log.getLogger().warn("TransferItemsRequestMessage colony is null");
            return;
        }

        final AbstractBuilding building = colony.getBuilding(message.buildingId);
        if (building == null)
        {
            Log.getLogger().warn("TransferItemsRequestMessage building is null");
            return;
        }

        building.getTileEntity().markDirty();
    }
}
