package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Creates a request from the postbox.
 */
public class PostBoxRequestMessage implements IMessage
{
    /**
     * The id of the building.
     */
    private BlockPos  buildingId;

    /**
     * The id of the colony.
     */
    private int       colonyId;

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
    public PostBoxRequestMessage()
    {
        super();
    }

    /**
     * Creates a Transfer Items request 
     *
     * @param building  AbstractBuilding of the request.
     * @param itemStack to be take from the player for the building
     * @param quantity  of item needed to be transfered
     */
    public PostBoxRequestMessage(@NotNull final AbstractBuildingView building, final ItemStack itemStack, final int quantity)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.itemStack = itemStack;
        this.itemStack.setCount(quantity);
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        buildingId = buf.readBlockPos();
        itemStack = buf.readItemStack();
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeBlockPos(buildingId);
        buf.writeItemStack(itemStack);
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
            Log.getLogger().warn("TransferItemsRequestMessage colony is null");
            return;
        }

        final IBuilding building = colony.getBuildingManager().getBuilding(buildingId);
        if (building == null)
        {
            Log.getLogger().warn("TransferItemsRequestMessage building is null");
            return;
        }

        building.createRequest(new Stack(itemStack), false);
    }
}
