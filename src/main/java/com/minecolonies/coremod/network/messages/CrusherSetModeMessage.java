package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCrusher;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message to set the crusher mode from the GUI.
 */
public class CrusherSetModeMessage extends AbstractMessage<CrusherSetModeMessage, IMessage>
{
    /**
     * The colony id.
     */
    private int colonyId;

    /**
     * The building id of the crusher.
     */
    private BlockPos buildingId;

    /**
     * The dimension of the building.
     */
    private int dimension;

    /**
     * The quantity to produce.
     */
    private int quantity;

    /**
     * The crusher mode.
     */
    private int crusherMode;

    /**
     * Empty constructor used when registering the message.
     */
    public CrusherSetModeMessage()
    {
        super();
    }

    /**
     * Set the mode of the crusher.
     *
     * @param building      the building to set it for.
     * @param dailyQuantity the quantity to produce.
     * @param crusherMode   the mode to set.
     */
    public CrusherSetModeMessage(@NotNull final BuildingCrusher.View building, final int crusherMode, final int dailyQuantity)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.quantity = dailyQuantity;
        this.crusherMode = crusherMode;
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        dimension = buf.readInt();
        quantity = buf.readInt();
        crusherMode = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeInt(dimension);
        buf.writeInt(quantity);
        buf.writeInt(crusherMode);
    }

    @Override
    public void messageOnServerThread(final CrusherSetModeMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null)
        {

            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final BuildingCrusher building = colony.getBuildingManager().getBuilding(message.buildingId, BuildingCrusher.class);
            if (building != null)
            {
                building.setCrusherMode(BuildingCrusher.CrusherMode.values()[message.crusherMode], message.quantity);
            }
        }
    }
}
