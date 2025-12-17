package com.minecolonies.core.client.gui.citizen;

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
import com.minecolonies.core.client.gui.AbstractWindowSkeleton;
import com.minecolonies.core.client.gui.modules.RequestTreeWindowModule;
import com.minecolonies.core.network.messages.server.colony.UpdateRequestStateMessage;
import com.minecolonies.core.network.messages.server.colony.citizen.TransferItemsToCitizenRequestMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_CANT_TAKE_EQUIPPED;

/**
 * BOWindow for the citizen.
 */
public class RequestWindowCitizen extends AbstractWindowCitizen
{
    public static final ResourceLocation WINDOW_ID = new ResourceLocation(Constants.MOD_ID, "gui/citizen/requests.xml");

    /**
     * The request that should be opened.
     */
    @Nullable
    private final IRequest<?> autoOpenRequest;

    /**
     * The window module instance for handling requests.
     */
    private final RequestTreeWindowModule requestTreeModule;

    /**
     * Constructor to initiate the citizen windows.
     *
     * @param citizen citizen to bind the window to.
     */
    public RequestWindowCitizen(final ICitizenDataView citizen)
    {
        this(citizen, null);
    }

    /**
     * Constructor to initialize the citizen request window. Automatically opens the detail page for the given request.
     *
     * @param citizen         citizen to bind the window to.
     * @param autoOpenRequest the request to open.
     */
    public RequestWindowCitizen(final ICitizenDataView citizen, @Nullable final IRequest<?> autoOpenRequest)
    {
        super(citizen, WINDOW_ID);
        this.autoOpenRequest = autoOpenRequest;
        this.requestTreeModule = registerLayoutModule(CitizenRequestTreeWindowModule::new, citizen, 33, 29);
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        if (autoOpenRequest != null)
        {
            requestTreeModule.openDetails(autoOpenRequest);
        }
    }

    private static class CitizenRequestTreeWindowModule extends RequestTreeWindowModule implements RequestTreeWindowModule.IRequestTreeSupportsFulfill
    {
        private final ICitizenDataView citizenDataView;

        private final IBuildingView buildingView;

        private final boolean isCreative;

        private final Inventory inventory;

        /**
         * Constructor to initiate the window request tree windows.
         *
         * @param parent the parenting window
         */
        private CitizenRequestTreeWindowModule(
            final AbstractWindowSkeleton parent,
            final ICitizenDataView citizenDataView)
        {
            super(parent, citizenDataView.getColony());
            this.citizenDataView = citizenDataView;
            this.buildingView = citizenDataView.getColony().getClientBuildingManager().getBuilding(citizenDataView.getWorkBuilding());
            this.isCreative = Minecraft.getInstance().player.isCreative();
            this.inventory = Minecraft.getInstance().player.getInventory();
        }

        @Override
        protected Collection<IRequest<?>> getOpenRequests()
        {
            if (buildingView == null)
            {
                return List.of();
            }

            final List<IRequest<?>> requests = new ArrayList<>();
            for (final IToken<?> token : buildingView.getOpenRequestsByCitizen().getOrDefault(citizenDataView.getId(), Collections.emptyList()))
            {
                if (token != null)
                {
                    final IRequest<?> request = colony.getRequestManager().getRequestForToken(token);
                    if (request != null)
                    {
                        requests.add(request);
                    }
                }
            }

            for (final IToken<?> token : buildingView.getOpenRequestsByCitizen().getOrDefault(-1, Collections.emptyList()))
            {
                if (token != null)
                {
                    final IRequest<?> request = colony.getRequestManager().getRequestForToken(token);
                    if (request != null)
                    {
                        requests.add(request);
                    }
                }
            }
            return requests;
        }

        @Override
        public void onFulfill(final @NotNull IRequest<?> request)
        {
            if (!(request.getRequest() instanceof IDeliverable deliverable))
            {
                return;
            }

            final Predicate<ItemStack> requestPredicate = deliverable::matches;
            final int amount = deliverable.getCount();

            final int count = InventoryUtils.getItemCountInItemHandler(new InvWrapper(inventory), deliverable::matches);

            if (!isCreative && count <= 0)
            {
                return;
            }

            // The itemStack size should not be greater than itemStack.getMaxStackSize, We send 1 instead
            // and use quantity for the size
            final ItemStack itemStack;
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
                        .append(COM_MINECOLONIES_CANT_TAKE_EQUIPPED, citizenDataView.getName())
                        .withPriority(MessageUtils.MessagePriority.IMPORTANT)
                        .sendTo(Minecraft.getInstance().player);

                    return;
                }
                itemStack = inventory.getItem(slot);
            }


            if (citizenDataView.getWorkBuilding() != null)
            {
                colony.getClientBuildingManager().getBuilding(citizenDataView.getWorkBuilding()).onRequestedRequestComplete(colony.getRequestManager(), request);
            }

            final int quantity = isCreative ? amount : Math.min(amount, count);
            Network.getNetwork().sendToServer(new TransferItemsToCitizenRequestMessage(colony, citizenDataView, itemStack, quantity));

            final ItemStack copy = itemStack.copy();
            copy.setCount(quantity);
            Network.getNetwork().sendToServer(new UpdateRequestStateMessage(colony, request.getId(), RequestState.OVERRULED, copy));
        }
    }
}
