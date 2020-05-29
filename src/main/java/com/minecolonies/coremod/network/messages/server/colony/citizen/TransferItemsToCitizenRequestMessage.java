package com.minecolonies.coremod.network.messages.server.colony.citizen;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

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
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {
        citizenId = buf.readInt();
        itemStack = buf.readItemStack();
        quantity = buf.readInt();
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(citizenId);
        buf.writeItemStack(itemStack);
        buf.writeInt(quantity);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        final ICitizenData citizenData = colony.getCitizenManager().getCitizen(citizenId);
        if (citizenData == null)
        {
            Log.getLogger().warn("TransferItemsRequestMessage citizenData is null");
            return;
        }

        final Optional<AbstractEntityCitizen> optionalEntityCitizen = citizenData.getCitizenEntity();
        if (!optionalEntityCitizen.isPresent())
        {
            Log.getLogger().warn("TransferItemsRequestMessage entity citizen is null");
            return;
        }

        final PlayerEntity player = ctxIn.getSender();
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

        final int amountToTake;
        if (isCreative)
        {
            amountToTake = quantity;
        }
        else
        {
            amountToTake = Math.min(quantity, InventoryUtils.getItemCountInItemHandler(new InvWrapper(player.inventory), stack -> stack.isItemEqual(itemStack)));
        }

        final ItemStack itemStackToTake = itemStack.copy();
        ItemStackUtils.setSize(itemStackToTake, quantity);
        final AbstractEntityCitizen citizen = optionalEntityCitizen.get();
        final ItemStack remainingItemStack = InventoryUtils.addItemStackToItemHandlerWithResult(citizen.getInventoryCitizen(), itemStackToTake);
        if (!isCreative)
        {
            int amountToRemoveFromPlayer = amountToTake - ItemStackUtils.getSize(remainingItemStack);
            while (amountToRemoveFromPlayer > 0)
            {
                final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(player.inventory), stack -> stack.isItemEqual(itemStack));
                final ItemStack itemsTaken = player.inventory.decrStackSize(slot, amountToRemoveFromPlayer);
                amountToRemoveFromPlayer -= ItemStackUtils.getSize(itemsTaken);
            }
        }
    }
}
