package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import org.jetbrains.annotations.NotNull;

/**
 * Message to execute the renaiming of the townHall.
 */
public class HutRenameMessage implements IMessage
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
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        name = buf.readString();
        dimension = buf.readInt();
        buildingId = buf.readBlockPos();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeString(name);
        buf.writeInt(dimension);
        buf.writeBlockPos(buildingId);
    }

    @Override
    public void messageOnServerThread(final HutRenameMessage message, final ServerPlayerEntity player)
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
