package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingEnchanter;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message to set the daily drainage for the enchanter.
 */
public class EnchanterQtySetMessage extends AbstractMessage<EnchanterQtySetMessage, IMessage>
{
    /**
     * The colony id.
     */
    private int colonyId;

    /**
     * The building id of the enchanter.
     */
    private BlockPos buildingId;

    /**
     * The dimension of the building.
     */
    private int dimension;

    /**
     * The worker to add/remove.
     */
    private int qty;

    /**
     * Empty constructor used when registering the message.
     */
    public EnchanterQtySetMessage()
    {
        super();
    }

    /**
     * Create the enchanter daily drainage message.
     * @param building the building of the enchanter.
     * @param qty the worker to add/remove.
     */
    public EnchanterQtySetMessage(@NotNull final BuildingEnchanter.View building, final int qty)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.qty = qty;
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        dimension = buf.readInt();
        qty = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeInt(dimension);
        buf.writeInt(qty);
    }

    @Override
    public void messageOnServerThread(final EnchanterQtySetMessage message, final EntityPlayerMP player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null)
        {

            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final BuildingEnchanter building = colony.getBuildingManager().getBuilding(message.buildingId, BuildingEnchanter.class);
            if (building != null)
            {
                building.setDailyDrainage(message.qty);
            }
        }
    }
}
