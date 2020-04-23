package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * The dimension of the 
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
     * Object creation for the town hall rename 
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
        name = buf.readString(32767);
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

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, dimension);
        final PlayerEntity player = ctxIn.getSender();
        if (colony != null && colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
        {
            final IBuilding b = colony.getBuildingManager().getBuildings().get(buildingId);

            if (b != null)
            {
                b.setCustomBuildingName(name);
            }
        }
    }
}
