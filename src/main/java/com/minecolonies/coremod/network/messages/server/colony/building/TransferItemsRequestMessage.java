package com.minecolonies.coremod.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Transfer some items from the player inventory to the Builder's chest or additional chests. Created: January 20, 2017
 *
 * @author xavierh
 */
public class TransferItemsRequestMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * How many item need to be transfer from the player inventory to the building chest.
     */
    private ItemStack itemStack;

    /**
     * How many item need to be transfer from the player inventory to the building chest.
     */
    private int quantity;

    /**
     * Attempt a resolve or not.
     */
    private boolean attemptResolve;

    /**
     * Empty constructor used when registering the
     */
    public TransferItemsRequestMessage()
    {
        super();
    }

    /**
     * Creates a Transfer Items request
     *
     * @param building       AbstractBuilding of the request.
     * @param itemStack      to be take from the player for the building
     * @param quantity       of item needed to be transfered
     * @param attemptResolve whether to attempt to resolve.
     */
    public TransferItemsRequestMessage(@NotNull final IBuildingView building, final ItemStack itemStack, final int quantity, final boolean attemptResolve)
    {
        super(building);
        this.itemStack = itemStack;
        this.quantity = quantity;
        this.attemptResolve = attemptResolve;
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {
        itemStack = buf.readItem();
        quantity = buf.readInt();
        attemptResolve = buf.readBoolean();
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {
        buf.writeItem(itemStack);
        buf.writeInt(quantity);
        buf.writeBoolean(attemptResolve);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        if (quantity <= 0)
        {
            Log.getLogger().warn("TransferItemsRequestMessage quantity below 0");
            return;
        }

        final PlayerEntity player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

        final boolean isCreative = player.isCreative();
        // Inventory content before
        Map<ItemStorage, ItemStorage> previousContent = null;
        final int amountToTake;
        if (isCreative)
        {
            amountToTake = quantity;
        }
        else
        {
            if (MineColonies.getConfig().getServer().debugInventories.get())
            {
                previousContent = InventoryUtils.getAllItemsForProviders(building.getTileEntity(), new InvWrapper(player.inventory));
            }

            amountToTake = Math.min(quantity, InventoryUtils.getItemCountInItemHandler(new InvWrapper(player.inventory),
              stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, itemStack, true, true)));
        }

        ItemStack remainingItemStack = ItemStack.EMPTY;
        int tempAmount = amountToTake;
        for (int i = 0; i < Math.max(1, Math.ceil((double) amountToTake/itemStack.getMaxStackSize())); i++)
        {
            final ItemStack itemStackToTake = itemStack.copy();
            int insertAmount = Math.min(itemStack.getMaxStackSize(), tempAmount);
            itemStackToTake.setCount(insertAmount);
            tempAmount -= insertAmount;

            remainingItemStack = InventoryUtils.addItemStackToProviderWithResult(building.getTileEntity(), itemStackToTake);
            if (!remainingItemStack.isEmpty())
            {
                tempAmount += remainingItemStack.getCount();
                break;
            }
        }

        if (ItemStackUtils.isEmpty(remainingItemStack) || ItemStackUtils.getSize(remainingItemStack) != amountToTake)
        {
            //Only doing this at the moment as the additional chest do not detect new content
            building.getTileEntity().setChanged();
        }

        if (ItemStackUtils.isEmpty(remainingItemStack) || ItemStackUtils.getSize(remainingItemStack) != amountToTake)
        {
            if (!isCreative)
            {
                int amountToRemoveFromPlayer = amountToTake - tempAmount;
                while (amountToRemoveFromPlayer > 0)
                {
                    final int slot =
                      InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(player.inventory),
                        stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, itemStack, true, true));
                    final ItemStack itemsTaken = player.inventory.removeItem(slot, amountToRemoveFromPlayer);
                    amountToRemoveFromPlayer -= ItemStackUtils.getSize(itemsTaken);
                }
            }

            if (attemptResolve)
            {
                building.overruleNextOpenRequestWithStack(itemStack);
            }
        }

        if (!isCreative && previousContent != null && MineColonies.getConfig().getServer().debugInventories.get())
        {
            InventoryUtils.doStorageSetsMatch(previousContent, InventoryUtils.getAllItemsForProviders(building.getTileEntity(), new InvWrapper(player.inventory)), true);
        }
    }
}
