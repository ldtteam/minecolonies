package com.minecolonies.core.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.*;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import net.minecraftforge.network.NetworkEvent;
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
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        itemStack = buf.readItem();
        quantity = buf.readInt();
        attemptResolve = buf.readBoolean();
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
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

        final Player player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

        final boolean isCreative = player.isCreative();
        // Inventory content before
        Map<ItemStorage, ItemStorage> previousContent = null;
        if (MineColonies.getConfig().getServer().debugInventories.get())
        {
            previousContent = InvDebugUtil.getAllItemsForProviders(building.getTileEntity(), new PlayerMainInvWrapper(player.getInventory()));
        }

        int movedItems = 0;
        int leftOver = 0;
        if (isCreative)
        {
            for (int i = 0; i < Math.ceil((double) this.quantity /itemStack.getMaxStackSize()); i++)
            {
                final int size = Math.min(this.quantity - movedItems, itemStack.getMaxStackSize());
                final ItemStack stack = itemStack.copyWithCount(size);
                final ItemStack leftOverStack = building.insert(stack);
                if (!ItemStackUtils.isEmpty(leftOverStack))
                {
                    leftOver = leftOverStack.getCount();
                    movedItems += size - leftOver;
                    break;
                }
                movedItems += size;
            }
        }
        else
        {
            final int toMove = Math.min(this.quantity, InventoryUtils.getItemCount(new PlayerMainInvWrapper(player.getInventory()), stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, itemStack, true, true)));
            if (toMove == 0)
            {
                return;
            }
            movedItems = building.insertFrom(new PlayerMainInvWrapper(player.getInventory()), itemStack, toMove);
            leftOver = toMove - movedItems;
        }
        if (leftOver > 0)
        {
            MessageUtils.format(Component.translatable("entity.builder.inventoryfull", itemStack.getDisplayName()).withStyle(ChatFormatting.RED)).sendTo(player);
        }

        if (movedItems > 0)
        {
            building.getTileEntity().setChanged();
            if (attemptResolve)
            {
                building.overruleNextOpenRequestWithStack(itemStack);
            }
        }
       
        if (!isCreative && previousContent != null && MineColonies.getConfig().getServer().debugInventories.get())
        {
            InvDebugUtil.doStorageSetsMatch(previousContent, InvDebugUtil.getAllItemsForProviders(building.getTileEntity(), new PlayerMainInvWrapper(player.getInventory())), true);
        }
    }
}
