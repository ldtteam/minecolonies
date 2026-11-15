package com.minecolonies.core.network.messages.server.colony.citizen;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.network.NetworkEvent;
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
     * The requestId
     */
    private IToken<?> token;

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
    public TransferItemsToCitizenRequestMessage(
        final IColony colony,
        @NotNull final ICitizenDataView citizenDataView,
        final ItemStack itemStack,
        final int quantity,
        final IToken<?> requestId)
    {
        super(colony);
        this.citizenId = citizenDataView.getId();
        this.itemStack = itemStack;
        this.quantity = quantity;
        this.token = requestId;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        citizenId = buf.readInt();
        itemStack = buf.readItem();
        quantity = buf.readInt();
        token = StandardFactoryController.getInstance().deserialize(buf.readNbt());
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(citizenId);
        buf.writeItem(itemStack);
        buf.writeInt(quantity);
        buf.writeNbt(StandardFactoryController.getInstance().serialize(token));
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

        itemStack.setCount(quantity);

        final AbstractEntityCitizen citizen = optionalEntityCitizen.get();
        if (!player.isCreative())
        {
            final int remainder = InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoNextFreeSlotInItemHandlerWithResult(new InvWrapper(player.getInventory()),
                stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, itemStack),
                quantity,
                citizen.getInventoryCitizen());
            if (remainder > 0)
            {
                player.displayClientMessage(Component.translatable("com.minecolonies.coremod.citizeninvfull"), true);
            }
        }
        else
        {
            InventoryUtils.addItemStackToItemHandler(citizen.getInventoryCitizen(), itemStack);
        }

        if (token != null)
        {
            colony.getRequestManager().overruleRequest(token, itemStack);
        }
    }
}
