package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Message to execute the renaiming of the townHall.
 */
public class HutRenameMessage extends AbstractMessage<HutRenameMessage, IMessage>
{
    /**
     * The colony id.
     */
    private int colonyId;

    /**
     * The custom name to set.
     */
    private String name;

    /**
     * The building id.
     */
    private BlockPos buildingId;

    /**
     * The dimension of the message.
     */
    private int dimension;

    /**
     * Empty public constructor.
     */
    public HutRenameMessage()
    {
        super();
    }

    /**
     * Object creation for the town hall rename message.
     *
     * @param colony Colony the rename is going to occur in.
     * @param name   New name of the town hall.
     */
    public HutRenameMessage(@NotNull final IColony colony, final String name, final AbstractBuildingView b)
    {
        super();
        this.colonyId = colony.getID();
        this.name = name;
        this.dimension = colony.getDimension();
        this.buildingId = b.getID();
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        name = ByteBufUtils.readUTF8String(buf);
        dimension = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        ByteBufUtils.writeUTF8String(buf, name);
        buf.writeInt(dimension);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
    }

    @Override
    public void messageOnServerThread(final HutRenameMessage message, final EntityPlayerMP player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null && colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
        {
            final IBuilding b = colony.getBuildingManager().getBuildings().get(message.buildingId);

            if (b != null)
            {
                b.setCustomBuildingName(message.name);
            }
        }
    }
}
