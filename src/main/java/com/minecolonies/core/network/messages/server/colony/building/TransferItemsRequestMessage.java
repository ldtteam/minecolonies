package com.minecolonies.core.network.messages.server.colony.building;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Transfer some items from the player inventory to the Builder's chest or additional chests. Created: January 20, 2017
 *
 * @author xavierh
 */
public class TransferItemsRequestMessage extends AbstractBuildingServerMessage<IBuilding>
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "transfer_items_request", TransferItemsRequestMessage::new);

    /**
     * How many item need to be transfer from the player inventory to the building chest.
     */
    private final ItemStack itemStack;

    /**
     * How many item need to be transfer from the player inventory to the building chest.
     */
    private final int quantity;

    /**
     * Attempt a resolve or not.
     */
    private final boolean attemptResolve;

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
        super(TYPE, building);
        this.itemStack = itemStack;
        this.quantity = quantity;
        this.attemptResolve = attemptResolve;
    }

    protected TransferItemsRequestMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        itemStack = Utils.deserializeCodecMess(buf);
        quantity = buf.readInt();
        attemptResolve = buf.readBoolean();
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        super.toBytes(buf);
        Utils.serializeCodecMess(buf, itemStack);
        buf.writeInt(quantity);
        buf.writeBoolean(attemptResolve);
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final IBuilding building)
    {
        if (quantity <= 0)
        {
            Log.getLogger().warn("TransferItemsRequestMessage quantity below 0");
            return;
        }

        final boolean isCreative = player.isCreative();
        // Inventory content before
        Map<ItemStorage, ItemStorage> previousContent = null;
        if (MineColonies.getConfig().getServer().debugInventories.get())
        {
            previousContent = InventoryUtils.getAllItemsForProviders(building.getTileEntity(), new InvWrapper(player.getInventory()));
        }

        final int toMove;
        final int leftOver;
        if (isCreative)
        {
            toMove = this.quantity;
            leftOver = InventoryUtils.transferItemStackToBuilding(building, itemStack, this.quantity);
        }
        else
        {
            toMove = Math.min(this.quantity, InventoryUtils.getItemCountInItemHandler(new InvWrapper(player.getInventory()), stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, itemStack, true, true)));
            if (toMove == 0)
            {
                return;
            }
            leftOver = InventoryUtils.transferItemStackFromItemHandlerToBuilding(new InvWrapper(player.getInventory()), building, itemStack, toMove);
        }
        if (leftOver > 0)
        {
            MessageUtils.format(Component.translatable("entity.builder.inventoryfull", itemStack.getDisplayName()).withStyle(ChatFormatting.RED)).sendTo(player);
        }
        final boolean success = leftOver < toMove;

        if (success)
        {
            building.getTileEntity().setChanged();
            if (attemptResolve)
            {
                building.overruleNextOpenRequestWithStack(itemStack);
            }
        }
       
        if (!isCreative && previousContent != null && MineColonies.getConfig().getServer().debugInventories.get())
        {
            InventoryUtils.doStorageSetsMatch(previousContent, InventoryUtils.getAllItemsForProviders(building.getTileEntity(), new InvWrapper(player.getInventory())), true);
        }
    }
}
