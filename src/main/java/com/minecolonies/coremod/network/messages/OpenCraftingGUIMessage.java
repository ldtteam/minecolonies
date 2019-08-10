package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message sent to open an inventory.
 */
public class OpenCraftingGUIMessage implements IMessage
{
    /**
     * The position of the inventory block/entity.
     */
    private BlockPos buildingId;

    /**
     * The colony id the field or building etc is in.
     */
    private int colonyId;

    /**
     * Size of the crafting grid.
     */
    private int gridSize;

    /**
     * The dimension of the 
     */
    private int dimension;

    /**
     * Empty public constructor.
     */
    public OpenCraftingGUIMessage()
    {
        super();
    }

    /**
     * Creates an open inventory message for a building.
     *
     * @param building {@link AbstractBuildingView}
     */
    public OpenCraftingGUIMessage(@NotNull final AbstractBuildingView building, final int gridSize)
    {
        super();
        this.buildingId = building.getPosition();
        this.gridSize = gridSize;
        this.colonyId = building.getColony().getID();
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        this.gridSize = buf.readInt();
        this.colonyId = buf.readInt();
        this.buildingId = buf.readBlockPos();
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(this.gridSize);
        buf.writeInt(this.colonyId);
        buf.writeBlockPos(buildingId);
        buf.writeInt(dimension);
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
        final ServerPlayerEntity player = ctxIn.getSender();
        if (colony != null && checkPermissions(colony, player))
        {
            final BlockPos pos = buildingId;
            //todo, which is our inventory?
            player.openContainer((INamedContainerProvider) player.world.getTileEntity(pos));
        }
    }

    private static boolean checkPermissions(final IColony colony, final ServerPlayerEntity player)
    {
        //Verify player has permission to change this huts settings
        return colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS);
    }
}
