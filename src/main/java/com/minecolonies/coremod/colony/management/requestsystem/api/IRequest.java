package com.minecolonies.coremod.colony.management.requestsystem.api;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Used to represent requests, of type T, made to the internal market of the colony.
 * @param <T> The type of request, eg ItemStack, FluidStack etc.
 */
public interface IRequest<T> extends INBTSerializable<NBTTagCompound> {

    /**
     * Used to determine which type of request this is.
     * Only RequestResolvers for this Type are then used to resolve the this.
     * @return The class that represents this Type of Request.
     */
    @NotNull
    Class<? extends T> getRequestType();

    /**
     * Returns the current state of the request.
     * @return The current state.
     */
    @NotNull
    RequestState getState();

    /**
     * Setter for the current state of this request.
     * @param state The new state of this request.
     */
    void setState(@NotNull RequestState state);

    /**
     * Return the object that is actually requested.
     * A RequestResolver can compare this object however way it sees fit.
     *
     * During the resolving process this object is called multiple times. But at least twice.
     * A cached implementation is preferred.
     * @return The object that is actually requested.
     */
    @NotNull
    T getRequest();

    /**
     * Returns the result of this request.
     * @return The result of this request, or null if it is not available.
     */
    @Nullable
    IRequestResult<T> getResult();

    /**
     * Setter for the result of the request.
     * @param result The new result of this request.
     */
    void setResult(@NotNull IRequestResult<ItemStack> result);

}
