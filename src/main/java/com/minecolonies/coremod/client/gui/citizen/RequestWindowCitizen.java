package com.minecolonies.coremod.client.gui.citizen;

import com.google.common.collect.ImmutableList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.server.colony.UpdateRequestStateMessage;
import com.minecolonies.coremod.network.messages.server.colony.citizen.TransferItemsToCitizenRequestMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_CANT_TAKE_EQUIPPED;
import static com.minecolonies.api.util.constant.WindowConstants.CITIZEN_REQ_RESOURCE_SUFFIX;

/**
 * Window for the citizen.
 */
public class RequestWindowCitizen extends AbstractWindowCitizen
{
    /**
     * The citizenData.View object.
     */
    private final ICitizenDataView citizen;

    /**
     * Inventory of the player.
     */
    private final PlayerInventory inventory = this.mc.player.inventory;

    /**
     * Is the player in creative or not.
     */
    private final boolean isCreative = this.mc.player.isCreative();

    /**
     * Constructor to initiate the citizen windows.
     *
     * @param citizen citizen to bind the window to.
     */
    public RequestWindowCitizen(final ICitizenDataView citizen)
    {
        super(citizen, Constants.MOD_ID + CITIZEN_REQ_RESOURCE_SUFFIX);
        this.citizen = citizen;
    }

    public ICitizenDataView getCitizen()
    {
        return citizen;
    }

    @Override
    public boolean canFulFill()
    {
        return true;
    }

    @Override
    public ImmutableList<IRequest<?>> getOpenRequestsFromBuilding(final IBuildingView building)
    {
        return building.getOpenRequests(citizen);
    }

    @Override
    public void fulfill(@NotNull final IRequest<?> tRequest)
    {
        if (!(tRequest.getRequest() instanceof IDeliverable))
        {
            return;
        }

        @NotNull final IRequest<? extends IDeliverable> request = (IRequest<? extends IDeliverable>) tRequest;

        final Predicate<ItemStack> requestPredicate = stack -> request.getRequest().matches(stack);
        final int amount = request.getRequest().getCount();

        final int count = InventoryUtils.getItemCountInItemHandler(new InvWrapper(inventory), requestPredicate);

        if (!isCreative && count <= 0)
        {
            return;
        }

        // The itemStack size should not be greater than itemStack.getMaxStackSize, We send 1 instead
        // and use quantity for the size
        @NotNull final ItemStack itemStack;
        if (isCreative)
        {
            itemStack = request.getDisplayStacks().stream().findFirst().orElse(ItemStack.EMPTY);
        }
        else
        {
            final List<Integer> slots = InventoryUtils.findAllSlotsInItemHandlerWith(new InvWrapper(inventory), requestPredicate);
            final int invSize = inventory.getSizeInventory() - 5; // 4 armour slots + 1 shield slot
            int slot = -1;
            for (final Integer possibleSlot : slots)
            {
                if (possibleSlot < invSize)
                {
                    slot = possibleSlot;
                    break;
                }
            }

            if (slot == -1)
            {
                final ITextComponent chatMessage = new StringTextComponent("<" + citizen.getName() + "> " +
                                                                             LanguageHandler.format(COM_MINECOLONIES_CANT_TAKE_EQUIPPED, citizen.getName()))
                                                     .setStyle(Style.EMPTY.setBold(false).setFormatting(TextFormatting.WHITE)
                                                     );
                Minecraft.getInstance().player.sendMessage(chatMessage, Minecraft.getInstance().player.getUniqueID());

                return; // We don't have one that isn't in our armour slot
            }
            itemStack = inventory.getStackInSlot(slot);
        }


        if (citizen.getWorkBuilding() != null)
        {
            colony.getBuilding(citizen.getWorkBuilding()).onRequestedRequestComplete(colony.getRequestManager(), tRequest);
        }
        Network.getNetwork().sendToServer(
          new TransferItemsToCitizenRequestMessage(colony, citizen, itemStack, isCreative ? amount : Math.min(amount, count)));

        final ItemStack copy = itemStack.copy();
        copy.setCount(isCreative ? amount : Math.min(amount, count));
        Network.getNetwork().sendToServer(new UpdateRequestStateMessage(colony, request.getId(), RequestState.OVERRULED, copy));
    }
}
