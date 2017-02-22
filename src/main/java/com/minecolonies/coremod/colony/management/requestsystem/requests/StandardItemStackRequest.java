package com.minecolonies.coremod.colony.management.requestsystem.requests;

import com.minecolonies.coremod.colony.management.requestsystem.api.IRequest;
import com.minecolonies.coremod.colony.management.requestsystem.api.IRequestResult;
import com.minecolonies.coremod.colony.management.requestsystem.api.RequestState;
import com.minecolonies.coremod.colony.management.requestsystem.results.StandardItemStackResult;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by marcf on 2/22/2017.
 */
public class StandardItemStackRequest implements IRequest<ItemStack> {

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_STATE = "State";
    private static final String NBT_REQUESTEDSTACK = "Stack";
    private static final String NBT_RESULTSTACK = "Result";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\


    @NotNull
    private RequestState state = RequestState.CREATED;
    @NotNull
    private ItemStack requestedStack;
    @Nullable
    private IRequestResult<ItemStack> result;

    public StandardItemStackRequest(ItemStack requestedStack) {
        this.requestedStack = requestedStack;
    }

    /**
     * Used to determine which type of request this is.
     * Only RequestResolvers for this Type are then used to resolve the this.
     *
     * @return The class that represents this Type of Request.
     */
    @NotNull
    @Override
    public Class<? extends ItemStack> getRequestType() {
        return ItemStack.class;
    }

    /**
     * Returns the current state of the request.
     *
     * @return The current state.
     */
    @NotNull
    @Override
    public RequestState getState() {
        return state;
    }

    /**
     * Setter for the current state of this request.
     *
     * @param state The new state of this request.
     */
    @Override
    public void setState(@NotNull RequestState state) {
        this.state = state;
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
    public ItemStack getRequest() {
        return requestedStack.copy();
    }

    /**
     * Returns the result of this request.
     *
     * @return The result of this request, or null if it is not available.
     */
    @Nullable
    @Override
    public IRequestResult<ItemStack> getResult() {
        return this.result;
    }

    /**
     * Setter for the result of the request.
     *
     * @param result The new result of this request.
     */
    @Override
    public void setResult(@NotNull IRequestResult<ItemStack> result) {
        this.result = result;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound data = new NBTTagCompound();

        data.setTag(NBT_STATE, state.serializeNBT());
        data.setTag(NBT_REQUESTEDSTACK, getRequest().serializeNBT());

        if (getResult() != null)
            data.setTag(NBT_RESULTSTACK, getResult().getResult().serializeNBT());

        return data;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        setState(RequestState.deserializeNBT((NBTTagInt) nbt.getTag(NBT_STATE)));
        this.requestedStack = new ItemStack(nbt.getCompoundTag(NBT_REQUESTEDSTACK));

        if (nbt.hasKey(NBT_RESULTSTACK))
            setResult(new StandardItemStackResult(this, new ItemStack(nbt.getCompoundTag(NBT_RESULTSTACK))));
    }
}
