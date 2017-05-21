package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.collect.Lists;
import com.minecolonies.api.colony.requestsystem.IRequestManager;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.coremod.colony.requestsystem.requestable.Delivery;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.tileentities.TileEntityWareHouse;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
        TileEntity tileEntity = manager.getColony().getWorld().getTileEntity(getLocation().getInDimensionLocation());

        if (tileEntity instanceof TileEntityWareHouse)
        {
            TileEntityWareHouse wareHouse = (TileEntityWareHouse) tileEntity;

            //TODO: Add check to warehouse TE

            //TODO: Mark selected ItemStack as required for delivery so that next time that stack is skipped.

            return true;
        }

        return false;
    }

    @Nullable
    @Override
    public List<IRequest> attemptResolve(
                                          @NotNull final IRequestManager manager, @NotNull final IRequest<ItemStack> request)
    {
        return Lists.newArrayList();
    }

    @Nullable
    @Override
    public void resolve(@NotNull final IRequestManager manager, @NotNull final IRequest<ItemStack> request) throws RuntimeException
    {
        //NOOP The stack is already marked in the check method.
    }

    @NotNull
    @Override
    public IRequest getFollowupRequestForCompletion(
                                                     @NotNull final IRequestManager manager, @NotNull final IRequest<ItemStack> completedRequest)
    {
        return manager.getRequestForToken(manager.createRequest(completedRequest.getRequester(),
          new Delivery(getLocation(), completedRequest.getRequester().getLocation(), completedRequest.getDelivery())));
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
}
