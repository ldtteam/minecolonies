package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Transfer some items from the player inventory to the Workers's Inventory.
 */
public class TransferItemsToCitizenRequestMessage implements IMessage
{
    /**
     * The id of the building.
     */
    private int citizenId;

    /**
     * The id of the colony.
     */
    private int colonyId;

    /**
     * How many item need to be transfer from the player inventory to the building chest.
     */
    private ItemStack itemStack;

    /**
     * How many item need to be transfer from the player inventory to the building chest.
     */
    private int quantity;

    /**
     * The dimension of the message.
     */
    private int dimension;

    /**
     * Empty constructor used when registering the message.
     */
    public TransferItemsToCitizenRequestMessage()
    {
        super();
    }

    /**
     * Creates a Transfer Items request message.
     *
     * @param citizenDataView Citizen of the request.
     * @param itemStack       to be take from the player for the building
     * @param quantity        of item needed to be transfered
     * @param colonyId        the colony id
     */
    public TransferItemsToCitizenRequestMessage(@NotNull final ICitizenDataView citizenDataView, final ItemStack itemStack, final int quantity, final int colonyId)
    {
        super();
        this.colonyId = colonyId;
        this.citizenId = citizenDataView.getId();
        this.itemStack = itemStack;
        this.quantity = quantity;
        this.dimension = Minecraft.getInstance().world.world.getDimension().getType().getId();
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        citizenId = buf.readInt();
        itemStack = buf.readItemStack();
        quantity = buf.readInt();
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(citizenId);
        buf.writeItemStack(itemStack);
        buf.writeInt(quantity);
        buf.writeInt(dimension);
    }

    @Override
    public void messageOnServerThread(final TransferItemsToCitizenRequestMessage message, final ServerPlayerEntity player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony == null)
        {
            Log.getLogger().warn("TransferItemsRequestMessage colony is null");
            return;
        }

        final ICitizenData citizenData = colony.getCitizenManager().getCitizen(message.citizenId);
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

        final boolean isCreative = player.capabilities.isCreativeMode;
        if (message.quantity <= 0 && !isCreative)
        {
            Log.getLogger().warn("TransferItemsRequestMessage quantity below 0");
            return;
        }

        final Item item = message.itemStack.getItem();
        final int amountToTake;
        if (isCreative)
        {
            amountToTake = message.quantity;
        }
        else
        {
            amountToTake = Math.min(message.quantity, InventoryUtils.getItemCountInItemHandler(new InvWrapper(player.inventory), item, message.itemStack.getItemDamage()));
        }

        final ItemStack itemStackToTake = message.itemStack.copy();
        ItemStackUtils.setSize(itemStackToTake, message.quantity);
        final AbstractEntityCitizen citizen = optionalEntityCitizen.get();
        final ItemStack remainingItemStack = InventoryUtils.addItemStackToItemHandlerWithResult(new InvWrapper(citizen.getInventoryCitizen()), itemStackToTake);
        if (!isCreative)
        {
            int amountToRemoveFromPlayer = amountToTake - ItemStackUtils.getSize(remainingItemStack);
            while (amountToRemoveFromPlayer > 0)
            {
                final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(player.inventory), stack -> stack.isItemEqual(message.itemStack));
                final ItemStack itemsTaken = player.inventory.decrStackSize(slot, amountToRemoveFromPlayer);
                amountToRemoveFromPlayer -= ItemStackUtils.getSize(itemsTaken);
            }
        }
    }
}
