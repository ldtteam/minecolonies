package com.minecolonies.coremod.colony.management.requestsystem.requestable;

import com.minecolonies.coremod.colony.management.requestsystem.api.location.ILocation;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Class used to represent deliveries inside the request system.
 * This class can be used to request a delivery of a given ItemStack from a source to a target.
 */
public class Delivery {

    @NotNull
    private final ILocation start;
    @NotNull
    private final ILocation target;
    @NotNull
    private final ItemStack stack;

    public Delivery(@NotNull ILocation start, @NotNull ILocation target, @NotNull ItemStack stack) {
        this.start = start;
        this.target = target;
        this.stack = stack;
    }

    @NotNull
    public ILocation getStart() {
        return start;
    }

    @NotNull
    public ILocation getTarget() {
        return target;
    }

    @NotNull
    public ItemStack getStack() {
        return stack;
    }
}
