package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Message to change priorities of recipes.
 */
public class ChangeRecipePriorityMessage extends AbstractMessage<ChangeRecipePriorityMessage, IMessage>
{
    /**
     * The Colony ID.
     */
    private int colonyId;

    /**
     * Id of the building.
     */
    private BlockPos buildingId;

    /**
     * The workOrder to remove or change priority.
     */
    private int recipeLocation;

    /**
     * If up true, if down false.
     */
    private boolean up;

    /**
     * Empty public constructor.
     */
    public ChangeRecipePriorityMessage()
    {
        super();
    }

    /**
     *  Creates message for player to change the priority of the recipes.
     *
     * @param building        view of the building to read data from
     * @param location     the recipeLocation.
     * @param up            up or down?
     */
    public ChangeRecipePriorityMessage(@NotNull final AbstractBuildingView building, final int location, final boolean up)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getLocation();
        this.recipeLocation = location;
        this.up = up;
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        this.colonyId = buf.readInt();
        this.buildingId = BlockPosUtil.readFromByteBuf(buf);
        this.recipeLocation = buf.readInt();
        this.up = buf.readBoolean();
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(this.colonyId);
        BlockPosUtil.writeToByteBuf(buf, this.buildingId);
        buf.writeInt(this.recipeLocation);
        buf.writeBoolean(this.up);
    }

    @Override
    public void messageOnServerThread(final ChangeRecipePriorityMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null && colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }
            final AbstractBuilding building = colony.getBuildingManager().getBuilding(message.buildingId);

            if(building instanceof AbstractBuildingWorker)
            {
                if (message.up)
                {
                    ((AbstractBuildingWorker) building).switchIndex(message.recipeLocation, message.recipeLocation + 1);
                }
                else
                {
                    ((AbstractBuildingWorker) building).switchIndex(message.recipeLocation, message.recipeLocation - 1);
                }
            }
        }
    }
}


