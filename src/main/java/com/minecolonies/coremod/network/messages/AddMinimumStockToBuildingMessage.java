package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.Log;
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
public class AddMinimumStockToBuildingMessage implements IMessage
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
     * How many item need to be transfer from the player inventory to the building chest.
     */
    private int quantity;

    /**
     * The dimension of the
     */
    private int dimension;

    /**
     * Empty constructor used when registering the
     */
    public AddMinimumStockToBuildingMessage()
    {
        super();
    }

    /**
     * Creates a Transfer Items request
     *
     * @param itemStack to be take from the player for the building
     * @param quantity  of item needed to be transfered
     * @param colonyId  the colony id
     * @param building  the building id.
     */
    public AddMinimumStockToBuildingMessage(final ItemStack itemStack, final int quantity, final int colonyId, final BlockPos building)
    {
        super();
        this.colonyId = colonyId;
        this.itemStack = itemStack;
        this.quantity = quantity;
        this.dimension = Minecraft.getInstance().world.getDimension().getType().getId();
        this.building = building;
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        itemStack = buf.readItemStack();
        quantity = buf.readInt();
        dimension = buf.readInt();
        building = buf.readBlockPos();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeItemStack(itemStack);
        buf.writeInt(quantity);
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
            Log.getLogger().warn("AddMinimumStock Message colony is null");
            return;
        }

        final IBuilding theBuilding = colony.getBuildingManager().getBuilding(building);
        if (theBuilding == null)
        {
            Log.getLogger().warn("AddMinimumStock Message building is null");
            return;
        }

        theBuilding.addMinimumStock(itemStack, quantity);
    }
}
