package com.minecolonies.coremod.colony.management.requestsystem.requests;

import com.minecolonies.coremod.colony.management.requestsystem.api.IRequest;
import com.minecolonies.coremod.colony.management.requestsystem.api.IRequestResult;
import com.minecolonies.coremod.colony.management.requestsystem.api.RequestState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Created by marcf on 2/22/2017.
 */
public abstract class ChainedRequest<T> implements IRequest<T> {

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_CORE = "CoreRequests";
    private static final String NBT_OPEN = "OpenRequests";
    private static final String NBT_CLOSED = "ClosedRequests";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\


    @NotNull
    private final IRequest<T> coreRequest;

    @NotNull
    private final ArrayList<IRequest> openRequiriedRequests = new ArrayList<>();

    @NotNull
    private final ArrayList<IRequest> closedRequiriedRequests = new ArrayList<>();

    protected ChainedRequest(@NotNull IRequest<T> coreRequest) {
        this.coreRequest = coreRequest;
    }

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
        return coreRequest.getState();
    }

    /**
     * Setter for the current state of this request.
     *
     * @param state The new state of this request.
     */
    @Override
    public void setState(@NotNull RequestState state) {
        this.coreRequest.setState(state);
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
        return coreRequest.getRequest();
    }

    /**
     * Returns the result of this request.
     *
     * @return The result of this request, or null if it is not available.
     */
    @Nullable
    @Override
    public IRequestResult<T> getResult() {
        return coreRequest.getResult();
    }

    /**
     * Setter for the result of the request.
     *
     * @param result The new result of this request.
     */
    @Override
    public void setResult(@NotNull IRequestResult<ItemStack> result) {
        this.coreRequest.setResult(result);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setTag(NBT_CORE, coreRequest.serializeNBT());

        NBTTagList openList = new NBTTagList();
        openRequiriedRequests.forEach(iRequest -> {
            openList.appendTag(iRequest.serializeNBT());
        });
        compound.setTag(NBT_OPEN, openList);

        NBTTagList closedList = new NBTTagList();
        closedRequiriedRequests.forEach(iRequest -> {
            closedList.appendTag(iRequest.serializeNBT());
        });
        compound.setTag(NBT_CLOSED, closedList);

        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {

    }

    public void registerNewRequirement(@NotNull IRequest requirement) {
        openRequiriedRequests.add(requirement);
    }

    public void closeRequirement(@NotNull IRequestResult result) {
        openRequiriedRequests.remove(result.getRequest());
        closedRequiriedRequests.add(result.getRequest());
    }
}
