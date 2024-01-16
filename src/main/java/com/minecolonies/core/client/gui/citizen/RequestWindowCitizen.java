package com.minecolonies.core.client.gui.citizen;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.network.messages.server.colony.UpdateRequestStateMessage;
import com.minecolonies.core.network.messages.server.colony.citizen.TransferItemsToCitizenRequestMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_CANT_TAKE_EQUIPPED;
import static com.minecolonies.api.util.constant.WindowConstants.CITIZEN_REQ_RESOURCE_SUFFIX;

/**
 * BOWindow for the citizen.
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
    private final Inventory inventory = this.mc.player.getInventory();

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
        if (building == null)
        {
            return ImmutableList.of();
        }

        final List<IRequest<?>> requests = new ArrayList<>();
        for (final IToken<?> req : building.getOpenRequestsByCitizen().getOrDefault(citizen.getId(), Collections.emptyList()))
        {
            if (req != null)
            {
                final IRequest<?> request = colony.getRequestManager().getRequestForToken(req);
                if (request != null)
                {
                    requests.add(request);
                }
            }
        }

        for (final IToken<?> req : building.getOpenRequestsByCitizen().getOrDefault(-1, Collections.emptyList()))
        {
            if (req != null)
            {
                final IRequest<?> request = colony.getRequestManager().getRequestForToken(req);
                if (request != null)
                {
                    requests.add(request);
                }
            }
        }

        return ImmutableList.copyOf(requests);
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
            final int invSize = inventory.getContainerSize() - 5; // 4 armour slots + 1 shield slot
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
                MessageUtils.format("<%s> ")
                  .with(ChatFormatting.BOLD, ChatFormatting.WHITE)
                  .append(COM_MINECOLONIES_CANT_TAKE_EQUIPPED, citizen.getName())
                  .sendTo(Minecraft.getInstance().player);

                return; // We don't have one that isn't in our armour slot
            }
            itemStack = inventory.getItem(slot);
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
