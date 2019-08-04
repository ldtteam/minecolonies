package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.workerbuildings.IWareHouse;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWareHouse;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Issues the upgrade of the warehouse pos level 5.
 */
public class UpgradeWarehouseMessage implements IMessage
{
    /**
     * The id of the building.
     */
    private BlockPos buildingId;

    /**
     * The id of the colony.
     */
    private int colonyId;

    /**
     * The dimension of the 
     */
    private int dimension;

    /**
     * Empty constructor used when registering the 
     */
    public UpgradeWarehouseMessage()
    {
        super();
    }

    /**
     * Creates a Upgrade Warehouse 
     *
     * @param building AbstractBuilding of the request.
     */
    public UpgradeWarehouseMessage(@NotNull final AbstractBuildingView building)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        buildingId = buf.readBlockPos();
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
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
        if (colony == null)
        {
            Log.getLogger().warn("UpgradeWarehouseMessage colony is null");
            return;
        }

        final PlayerEntity player = ctxIn.getSender();
        if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
        {
            return;
        }

        final IBuilding building = colony.getBuildingManager().getBuilding(buildingId);
        if (!(building instanceof BuildingWareHouse))
        {
            Log.getLogger().warn("UpgradeWarehouseMessage building is not a Warehouse");
            return;
        }

        ((IWareHouse) building).upgradeContainers(player.world);

        final boolean isCreative = player.isCreative();
        if (!isCreative)
        {
            final int slot = InventoryUtils.
                                             findFirstSlotInItemHandlerWith(new InvWrapper(player.inventory),
                                               itemStack -> itemStack.isItemEqual(new ItemStack(Blocks.EMERALD_BLOCK)));
            player.inventory.decrStackSize(slot, 1);
        }
    }
}
