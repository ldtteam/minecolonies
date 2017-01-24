package com.minecolonies.coremod.network.messages;

import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.util.BlockPosUtil;
import com.minecolonies.coremod.util.InventoryUtils;
import com.minecolonies.coremod.util.Log;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Transfer some items from the player inventory to the Builder's chest
 * Created: January 20, 2017
 *
 * @author xavierh
 */
public class TransferItemsRequestMessage  extends AbstractMessage<TransferItemsRequestMessage, IMessage>
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
     * How many item need to be transfer from the player inventory to the building chest
     */
    private int      itemId;
    /**
     * How many item need to be transfer from the player inventory to the building chest
     */
    private int      quantity;

    /**
     * Empty constructor used when registering the message.
     */
    public TransferItemsRequestMessage()
    {
        super();
    }

    /**
     * Creates a build request message.
     *
     * @param building AbstractBuilding of the request.
     * @param mode     Mode of the request, 1 is repair, 0 is build.
     */
    public TransferItemsRequestMessage(@NotNull final AbstractBuilding.View building, final int itemId, final int quantity)
    {
        super();
        this.colonyId   = building.getColony().getID();
        this.buildingId = building.getID();
        this.itemId     = itemId;
        this.quantity   = quantity;

    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        itemId = buf.readInt();
        quantity = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeInt(itemId);
        buf.writeInt(quantity);
    }

    @Override
    public void messageOnServerThread(final TransferItemsRequestMessage message, final EntityPlayerMP player)
    {

        final Colony colony = ColonyManager.getColony(message.colonyId);
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

        if (message.quantity <= 0)
        {
            Log.getLogger().warn("TransferItemsRequestMessage quantity below 0");
            return;
        }

        final Item item = Item.getItemById(message.itemId);
        final ItemStack itemStack = new ItemStack(item, message.quantity);
        final int amountInPlayer = InventoryUtils.getItemCountInInventory(player.inventory, item, -1);
        final int amountToTake = Math.min(message.quantity, InventoryUtils.getItemCountInInventory(player.inventory, item, -1));


        final ItemStack itemStackToTake = new ItemStack(item, amountToTake);
        final ItemStack remainingItemStack = InventoryUtils.setOverSizedStack(building.getTileEntity(), itemStackToTake);
        if (remainingItemStack.getCount() != remainingItemStack.getCount())
        {
            building.getTileEntity().markDirty();
        }

        int amountToRemoveFromPlayer = amountToTake - remainingItemStack.getCount();

        while (amountToRemoveFromPlayer > 0)
        {
            final int slot = InventoryUtils.findFirstSlotInInventoryWith(player.inventory, item, -1);
            final ItemStack itemsTaken = player.inventory.decrStackSize(slot, amountToRemoveFromPlayer);
            amountToRemoveFromPlayer-=itemsTaken.getCount();
        }

    }
}
