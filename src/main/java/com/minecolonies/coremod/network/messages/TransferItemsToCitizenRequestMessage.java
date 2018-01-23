package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.entity.EntityCitizen;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

/**
 * Transfer some items from the player inventory to the Workers's Inventory.
 */
public class TransferItemsToCitizenRequestMessage extends AbstractMessage<TransferItemsToCitizenRequestMessage, IMessage>
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
    public TransferItemsToCitizenRequestMessage(@NotNull final CitizenDataView citizenDataView, final ItemStack itemStack, final int quantity, final int colonyId)
    {
        super();
        this.colonyId = colonyId;
        this.citizenId = citizenDataView.getId();
        this.itemStack = itemStack;
        this.quantity = quantity;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        citizenId = buf.readInt();
        itemStack = ByteBufUtils.readItemStack(buf);
        quantity = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(citizenId);
        ByteBufUtils.writeItemStack(buf, itemStack);
        buf.writeInt(quantity);
    }

    @Override
    public void messageOnServerThread(final TransferItemsToCitizenRequestMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony == null)
        {
            Log.getLogger().warn("TransferItemsRequestMessage colony is null");
            return;
        }

        final CitizenData citizenData = colony.getCitizenManager().getCitizen(message.citizenId);
        if (citizenData == null)
        {
            Log.getLogger().warn("TransferItemsRequestMessage citizenData is null");
            return;
        }

        final EntityCitizen citizen = citizenData.getCitizenEntity();
        if (citizen == null)
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

        final ItemStack remainingItemStack = InventoryUtils.addItemStackToItemHandlerWithResult(new InvWrapper(citizen.getInventoryCitizen()), itemStackToTake);
        if (!isCreative)
        {
            int amountToRemoveFromPlayer = amountToTake - ItemStackUtils.getSize(remainingItemStack);
            while (amountToRemoveFromPlayer > 0)
            {
                final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(player.inventory), item, message.itemStack.getItemDamage());
                final ItemStack itemsTaken = player.inventory.decrStackSize(slot, amountToRemoveFromPlayer);
                amountToRemoveFromPlayer -= ItemStackUtils.getSize(itemsTaken);
            }
        }
    }
}
