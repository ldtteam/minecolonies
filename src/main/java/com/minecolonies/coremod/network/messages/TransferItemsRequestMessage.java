package com.minecolonies.coremod.network.messages;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.util.BlockPosUtil;
import com.minecolonies.coremod.util.InventoryUtils;
import com.minecolonies.coremod.util.Log;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;


/**
 * Transfer some items from the player inventory to the Builder's chest or additional chests.
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
     * How many item need to be transfer from the player inventory to the building chest.
     */
    private ItemStack      itemStack;
    /**
     * How many item need to be transfer from the player inventory to the building chest.
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
     * Creates a Transfer Items request message.
     *
     * @param building AbstractBuilding of the request.
     * @param itemStack to be take from the player for the building
     * @param quantity of item needed to be transfered
     */
    public TransferItemsRequestMessage(@NotNull final AbstractBuilding.View building, final ItemStack itemStack, final int quantity)
    {
        super();
        this.colonyId   = building.getColony().getID();
        this.buildingId = building.getID();
        this.itemStack  = itemStack;
        this.quantity   = quantity;

    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId   = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        itemStack  = ByteBufUtils.readItemStack(buf);
        quantity   = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        ByteBufUtils.writeItemStack(buf,itemStack);
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

        final Item item = message.itemStack.getItem();
        final int amountToTake = Math.min(message.quantity, InventoryUtils.getItemCountInInventory(player.inventory, item, message.itemStack.getItemDamage()));
        final ItemStack itemStackToTake = new ItemStack(item, amountToTake, message.itemStack.getItemDamage());

        ItemStack remainingItemStack = InventoryUtils.setOverSizedStack(building.getTileEntity(), itemStackToTake);

        //If we still have some to drop, let's try the additional chests now
        if (remainingItemStack.stackSize > 0)
        {
            final World world = colony.getWorld();
            for(final BlockPos pos : building.getAdditionalCountainers())
            {
                final TileEntity entity = world.getTileEntity(pos);
                if(entity instanceof TileEntityChest)
                {
                    remainingItemStack = InventoryUtils.setOverSizedStack((TileEntityChest)entity, remainingItemStack);
                    if (remainingItemStack.stackSize==0)
                    {
                        break;
                    }
                }
            }
        }
        if (remainingItemStack.stackSize != itemStackToTake.stackSize)
        {
            //Only doing this at the moment as the additional chest do not detect new content
            building.getTileEntity().markDirty();
        }

        int amountToRemoveFromPlayer = amountToTake - remainingItemStack.stackSize;

        while (amountToRemoveFromPlayer > 0)
        {
            final int slot = InventoryUtils.findFirstSlotInInventoryWith(player.inventory, item, message.itemStack.getItemDamage());
            final ItemStack itemsTaken = player.inventory.decrStackSize(slot, amountToRemoveFromPlayer);
            amountToRemoveFromPlayer-=itemsTaken.stackSize;
        }

    }
}
