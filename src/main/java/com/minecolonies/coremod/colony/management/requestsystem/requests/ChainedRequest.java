package com.minecolonies.coremod.colony.management.requestsystem.requests;

import com.minecolonies.coremod.colony.management.requestsystem.api.IRequest;
import com.minecolonies.coremod.colony.management.requestsystem.api.IRequestResult;
import com.minecolonies.coremod.colony.management.requestsystem.api.RequestState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Created by marcf on 2/22/2017.
 */
public abstract class ChainedRequest<T> implements IRequest<T> {

    @NotNull
    private IRequest<T> coreRequest;

    @NotNull
    private final ArrayList<IRequest> openRequriedRequests = new ArrayList<>();

    /**
     * Used to determine which type of request this is.
     * Only RequestResolvers for this Type are then used to resolve the this.
     *
     * @return The class that represents this Type of Request.
     */
    @NotNull
    @Override
    public Class<? extends T> getRequestType() {
        return coreRequest.getRequestType();
    }

    /**
     * Returns the current state of the request.
     *
     * @return The current state.
     */
    @NotNull
    @Override
    public RequestState getState() {
        return null;
    }

    /**
     * Setter for the current state of this request.
     *
     * @param state The new state of this request.
     */
    @Override
    public void setState(@NotNull RequestState state) {

    }

    /**
     * Return the object that is actually requested.
     * A RequestResolver can compare this object however way it sees fit.
     * <p>
     * During the resolving process this object is called multiple times. But at least twice.
     * A cached implementation is preferred.
     *
     * @return The object that is actually requested.
     */
    @NotNull
    @Override
    public T getRequest() {
        return null;
    }

    /**
     * Returns the result of this request.
     *
     * @return The result of this request, or null if it is not available.
     */
    @Nullable
    @Override
    public IRequestResult<T> getResult() {
        return null;
    }

    /**
     * Setter for the result of the request.
     *
     * @param result The new result of this request.
     */
    @Override
    public void setResult(@NotNull IRequestResult<ItemStack> result) {

    }

    @Override
    public NBTTagCompound serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {

    }
}
