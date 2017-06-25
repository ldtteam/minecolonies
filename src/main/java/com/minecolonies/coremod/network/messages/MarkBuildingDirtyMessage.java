package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.IAPI;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Send a message to the server to mark the building as dirty.
 * Created: January 20, 2017
 *
 * @author xavierh
 */
public class MarkBuildingDirtyMessage extends AbstractMessage<MarkBuildingDirtyMessage, IMessage>
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
     * The dimension ID of the world
     */
    private int      dimensionId;

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
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getLocation().getInDimensionLocation();
        this.dimensionId = Minecraft.getMinecraft().player.dimension;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        dimensionId = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeInt(dimensionId);
    }

    @Override
    public void messageOnServerThread(final MarkBuildingDirtyMessage message, final EntityPlayerMP player)
    {
        final World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(dimensionId);
        final Colony colony = IAPI.Holder.getApi().getColonyManager().getControllerForWorld(world).getColony()
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
