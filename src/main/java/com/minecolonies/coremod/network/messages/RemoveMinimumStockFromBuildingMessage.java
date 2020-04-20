package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWareHouse;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Set a new block to the minimum stock list.
 */
public class RemoveMinimumStockFromBuildingMessage implements IMessage
{
    /**
     * The id of the colony.
     */
    private int colonyId;

    /**
     * The id of the building.
     */
    private BlockPos building;

    /**
     * How many item need to be transfer from the player inventory to the building chest.
     */
    private ItemStack itemStack;

    /**
     * The dimension of the
     */
    private int dimension;

    /**
     * Empty constructor used when registering the
     */
    public RemoveMinimumStockFromBuildingMessage()
    {
        super();
    }

    /**
     * Creates a Transfer Items request
     *
     * @param itemStack to be take from the player for the building
     * @param colonyId  the colony id
     * @param building  the building id.
     */
    public RemoveMinimumStockFromBuildingMessage(final ItemStack itemStack, final int colonyId, final BlockPos building)
    {
        super();
        this.colonyId = colonyId;
        this.itemStack = itemStack;
        this.dimension = Minecraft.getInstance().world.getDimension().getType().getId();
        this.building = building;
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        itemStack = buf.readItemStack();
        dimension = buf.readInt();
        building = buf.readBlockPos();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeItemStack(itemStack);
        buf.writeInt(dimension);
        buf.writeBlockPos(building);
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
            Log.getLogger().warn("RemoveMinimumStock Message colony is null");
            return;
        }

        final IBuilding theBuilding = colony.getBuildingManager().getBuilding(building);
        if (theBuilding == null)
        {
            Log.getLogger().warn("RemoveMinimumStock Message building is null");
            return;
        }

        theBuilding.removeMinimumStock(itemStack);
    }
}
