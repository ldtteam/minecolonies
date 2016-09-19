package com.minecolonies.network.messages;

import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.AbstractBuilding;
import com.minecolonies.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message class which manages the messages hiring or firing of citizens.
 */
public class HireFireMessage implements IMessage, IMessageHandler<HireFireMessage, IMessage>
{
    /**
     * The Colony ID;
     */
    private int colonyId;

    /**
     * The buildings position.
     */
    private BlockPos buildingId;

    /**
     * If hiring (true) else firing.
     */
    private boolean hire;

    /**
     * The citizen to hire/fire
     */
    private int citizenID;

    /**
     * Empty public constructor.
     */
    public HireFireMessage()
    {
        //Required for netty.
    }

    /**
     * Creates object for the player to hire or fire a citizen.
     *
     * @param building  view of the building to read data from
     * @param hire      hire or fire the citizens
     * @param citizenID the id of the citizen to fill the job.
     */
    public HireFireMessage(@NotNull AbstractBuilding.View building, boolean hire, int citizenID)
    {
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.hire = hire;
        this.citizenID = citizenID;
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void fromBytes(@NotNull ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        hire = buf.readBoolean();
        citizenID = buf.readInt();
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void toBytes(@NotNull ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeBoolean(hire);
        buf.writeInt(citizenID);
    }

    /**
     * Called when a message has been received.
     *
     * @param message the message.
     * @param ctx     the context.
     * @return possible response, in this case -&gt; null.
     */
    @Nullable
    @Override
    public IMessage onMessage(@NotNull HireFireMessage message, @NotNull MessageContext ctx)
    {
        Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {
            //Verify player has permission to do edit permissions
            if (!colony.getPermissions().hasPermission(ctx.getServerHandler().playerEntity, Permissions.Action.ACCESS_HUTS))
            {
                return null;
            }

            if (message.hire)
            {
                CitizenData citizen = colony.getCitizen(message.citizenID);
                ((AbstractBuildingWorker) colony.getBuilding(message.buildingId)).setWorker(citizen);
            }
            else
            {
                ((AbstractBuildingWorker) colony.getBuilding(message.buildingId)).setWorker(null);
            }
        }
        return null;
    }
}
