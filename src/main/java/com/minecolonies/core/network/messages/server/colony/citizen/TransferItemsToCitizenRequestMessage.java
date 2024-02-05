package com.minecolonies.core.network.messages.server.colony.citizen;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Transfer some items from the player inventory to the Workers's Inventory.
 */
public class TransferItemsToCitizenRequestMessage extends AbstractColonyServerMessage
{
    /**
     * The id of the building.
     */
    private int citizenId;

    /**
     * How many item need to be transfer from the player inventory to the building chest.
     */
    private ItemStack itemStack;

    /**
     * How many item need to be transfer from the player inventory to the building chest.
     */
    private int quantity;

    /**
     * Empty constructor used when registering the
     */
    public TransferItemsToCitizenRequestMessage()
    {
        super();
    }

    /**
     * Creates a Transfer Items request
     *
     * @param citizenDataView Citizen of the request.
     * @param itemStack       to be take from the player for the building
     * @param quantity        of item needed to be transfered
     * @param colony          the colony of the network message
     */
    public TransferItemsToCitizenRequestMessage(final IColony colony, @NotNull final ICitizenDataView citizenDataView, final ItemStack itemStack, final int quantity)
    {
        super(colony);
        this.citizenId = citizenDataView.getId();
        this.itemStack = itemStack;
        this.quantity = quantity;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        citizenId = buf.readInt();
        itemStack = buf.readItem();
        quantity = buf.readInt();
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(citizenId);
        buf.writeItem(itemStack);
        buf.writeInt(quantity);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        final ICitizenData citizenData = colony.getCitizenManager().getCivilian(citizenId);
        if (citizenData == null)
        {
            Log.getLogger().warn("TransferItemsRequestMessage citizenData is null");
            return;
        }

        final Optional<AbstractEntityCitizen> optionalEntityCitizen = citizenData.getEntity();
        if (!optionalEntityCitizen.isPresent())
        {
            Log.getLogger().warn("TransferItemsRequestMessage entity citizen is null");
            return;
        }

        final Player player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

        final boolean isCreative = player.isCreative();
        if (quantity <= 0 && !isCreative)
        {
            Log.getLogger().warn("TransferItemsRequestMessage quantity below 0");
            return;
        }

        // Inventory content before
        Map<ItemStorage, ItemStorage> previousContent = null;
        final int amountToTake;
        if (isCreative)
        {
            amountToTake = quantity;
        }
        else
        {
            amountToTake = Math.min(quantity,
              InventoryUtils.getItemCountInItemHandler(new InvWrapper(player.getInventory()), stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, itemStack)));
        }

        final List<ItemStack> itemsToPut = new ArrayList<>();
        int tempAmount = amountToTake;

        while (tempAmount > 0)
        {
            int count = Math.min(itemStack.getMaxStackSize(), tempAmount);
            ItemStack stack = itemStack.copy();
            stack.setCount(count);
            itemsToPut.add(stack);
            tempAmount -= count;
        }

        final AbstractEntityCitizen citizen = optionalEntityCitizen.get();

        if (!isCreative && MineColonies.getConfig().getServer().debugInventories.get())
        {
            previousContent = InventoryUtils.getAllItemsForProviders(citizen.getInventoryCitizen(), new InvWrapper(player.getInventory()));
        }

        tempAmount = 0;
        for (final ItemStack insertStack : itemsToPut)
        {
            final ItemStack remainingItemStack = InventoryUtils.addItemStackToItemHandlerWithResult(citizen.getInventoryCitizen(), insertStack);
            if (!ItemStackUtils.isEmpty(remainingItemStack))
            {
                tempAmount += (insertStack.getCount() - remainingItemStack.getCount());
                break;
            }
            tempAmount += insertStack.getCount();
        }

        if (!isCreative)
        {
            int amountToRemoveFromPlayer = tempAmount;
            while (amountToRemoveFromPlayer > 0)
            {
                final int slot =
                  InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(player.getInventory()), stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, itemStack));
                final ItemStack itemsTaken = player.getInventory().removeItem(slot, amountToRemoveFromPlayer);
                amountToRemoveFromPlayer -= ItemStackUtils.getSize(itemsTaken);
            }
        }

        if (!isCreative && previousContent != null && MineColonies.getConfig().getServer().debugInventories.get())
        {
            InventoryUtils.doStorageSetsMatch(previousContent, InventoryUtils.getAllItemsForProviders(citizen.getInventoryCitizen(), new InvWrapper(player.getInventory())), true);
        }
    }
}
