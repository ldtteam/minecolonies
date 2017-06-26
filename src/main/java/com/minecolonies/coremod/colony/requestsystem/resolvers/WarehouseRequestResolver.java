package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.collect.Lists;
import com.minecolonies.api.colony.requestsystem.IRequestManager;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.Delivery;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.tileentities.TileEntityWareHouse;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * ----------------------- Not Documented Object ---------------------
 */
public class WarehouseRequestResolver extends AbstractRequestResolver<ItemStack>
{

    public WarehouseRequestResolver(
                                     @NotNull final ILocation location,
                                     @NotNull final IToken token)
    {
        super(location, token);
    }

    @Override
    public Class<? extends ItemStack> getRequestType()
    {
        return ItemStack.class;
    }

    @Override
    public boolean canResolve(@NotNull final IRequestManager manager, final IRequest<ItemStack> requestToCheck)
    {
        if (ItemStackUtils.isEmpty(requestToCheck.getRequest()))
        {
            return false;
        }

        final TileEntity tileEntity = manager.getColony().getWorld().getTileEntity(getLocation().getInDimensionLocation());

        if (tileEntity instanceof TileEntityWareHouse)
        {
            final TileEntityWareHouse wareHouse = (TileEntityWareHouse) tileEntity;

            return wareHouse.isInHut(requestToCheck.getRequest());
        }

        return false;
    }

    @Nullable
    @Override
    public List<IToken> attemptResolve(
                                          @NotNull final IRequestManager manager, @NotNull final IRequest<ItemStack> request)
    {
        final TileEntity tileEntity = manager.getColony().getWorld().getTileEntity(getLocation().getInDimensionLocation());

        if (tileEntity instanceof TileEntityWareHouse)
        {
            final TileEntityWareHouse wareHouse = (TileEntityWareHouse) tileEntity;
            final BlockPos pos = wareHouse.getPositionOfChestWithItemStack(request.getRequest());

            request.setDelivery(request.getRequest().copy());
            return Lists.newArrayList(manager.createRequest(new WarehouseChestDeliveryRequester(this, manager.getFactoryController().getNewInstance(UUID.randomUUID()), manager.getFactoryController().getNewInstance(pos), request.getToken()), new Delivery(manager.getFactoryController().getNewInstance(pos), request.getRequester().getDeliveryLocation(), request.getRequest())));
        }

        return Lists.newArrayList();
    }

    @Nullable
    @Override
    public void resolve(@NotNull final IRequestManager manager, @NotNull final IRequest<ItemStack> request) throws RuntimeException
    {
        //Noop delivery has been completed
    }

    @Nullable
    @Override
    public IRequest getFollowupRequestForCompletion(
                                                     @NotNull final IRequestManager manager, @NotNull final IRequest<ItemStack> completedRequest)
    {
        //No followup needed.
        return null;
    }

    @Nullable
    @Override
    public IRequest onParentCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<ItemStack> request) throws IllegalArgumentException
    {
        //TODO Release the stack from the warehouse.
        return null;
    }

    @Nullable
    @Override
    public void onResolvingOverruled(@NotNull final IRequestManager manager, @NotNull final IRequest<ItemStack> request) throws IllegalArgumentException
    {
        //TODO Release the stack from the warehouse.
    }

    @NotNull
    @Override
    public void onRequestComplete(@NotNull final IToken token)
    {
        //TODO Release the DMan that did the job.
    }

    private final class WarehouseChestDeliveryRequester implements IRequester {

        private final WarehouseRequestResolver warehouseRequestResolver;
        private final IToken id;
        private final ILocation location;

        private final IToken itemStackRequestToken;

        private WarehouseChestDeliveryRequester(
                                                 final WarehouseRequestResolver warehouseRequestResolver,
                                                 final IToken id,
                                                 final ILocation location,
                                                 final IToken itemStackRequestToken) {
            this.warehouseRequestResolver = warehouseRequestResolver;
            this.id = id;
            this.location = location;
            this.itemStackRequestToken = itemStackRequestToken;
        }

        @Override
        public IToken getID()
        {
            return id;
        }

        @NotNull
        @Override
        public ILocation getLocation()
        {
            return location;
        }

        @NotNull
        @Override
        public void onRequestComplete(@NotNull final IToken token)
        {
            warehouseRequestResolver.onRequestComplete(itemStackRequestToken);
        }
    }
}
