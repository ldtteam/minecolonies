package com.minecolonies.coremod.colony.management.requestsystem.results;

import com.minecolonies.coremod.colony.management.requestsystem.api.requests.IRequest;
import com.minecolonies.coremod.colony.management.requestsystem.api.IRequestResult;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Created by marcf on 2/22/2017.
 */
public class StandardItemStackResult implements IRequestResult<ItemStack> {

    @NotNull
    private final IRequest<ItemStack> request;

    @NotNull
    private final ItemStack result;

    public StandardItemStackResult(@NotNull IRequest<ItemStack> request, @NotNull ItemStack result) {
        this.request = request;
        this.result = result;
    }

    /**
     * The request that created this result.
     *
     * @return The request that created this result.
     */
    @NotNull
    @Override
    public IRequest<ItemStack> getRequest() {
        return request;
    }

    /**
     * The result from the request
     *
     * @return The result from the request.
     */
    @NotNull
    @Override
    public ItemStack getResult() {
        return result.copy();
    }
}
