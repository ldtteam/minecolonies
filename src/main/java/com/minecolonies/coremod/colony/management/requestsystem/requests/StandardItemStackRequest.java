package com.minecolonies.coremod.colony.management.requestsystem.requests;

import com.minecolonies.coremod.colony.IColony;
import com.minecolonies.coremod.colony.management.requestsystem.api.IRequestToken;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Created by marcf on 2/22/2017.
 */
public class StandardItemStackRequest extends AbstractRequest<ItemStack> {

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_STATE = "State";
    private static final String NBT_REQUESTEDSTACK = "Stack";
    private static final String NBT_RESULTSTACK = "Result";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    private ItemStack requestedStack;

    public StandardItemStackRequest(@NotNull IRequestToken token, @NotNull IColony colony, ItemStack requestedStack) {
        super(colony, token);
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

    /*@Override
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
    }*/
}
