package com.minecolonies.coremod.colony.management.requestsystem.requests;

import com.minecolonies.coremod.colony.management.requestsystem.api.IRequestToken;
import com.minecolonies.coremod.colony.management.requestsystem.api.requests.IRequest;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Created by marcf on 2/22/2017.
 */
public abstract class ChainedRequest<T> extends AbstractRequest<T> {

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_CORE = "CoreRequests";
    private static final String NBT_OPEN = "OpenRequests";
    private static final String NBT_CLOSED = "ClosedRequests";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\


    @NotNull
    private final IRequest<T> coreRequest;

    @NotNull
    private final ArrayList<IRequestToken> openRequiredRequests = new ArrayList<>();

    @NotNull
    private final ArrayList<IRequestToken> closedRequiredRequests = new ArrayList<>();


    public ChainedRequest(@NotNull IRequestToken token, @NotNull IRequest<T> coreRequest) {
        super(coreRequest.getColony(), token);

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

    /*
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setTag(NBT_CORE, coreRequest.serializeNBT());

        NBTTagList openList = new NBTTagList();
        openRequiredRequests.forEach(iRequest -> {
            openList.appendTag(iRequest.serializeNBT());
        });
        compound.setTag(NBT_OPEN, openList);

        NBTTagList closedList = new NBTTagList();
        closedRequiredRequests.forEach(iRequest -> {
            closedList.appendTag(iRequest.serializeNBT());
        });
        compound.setTag(NBT_CLOSED, closedList);

        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {

    }

    public void registerNewRequirement(@NotNull IRequest requirement) {
        openRequiredRequests.add(requirement);
    }

    public void closeRequirement(@NotNull IRequestResult result) {
        openRequiredRequests.remove(result.getRequest());
        closedRequiredRequests.add(result.getRequest());
    }
    */
}
