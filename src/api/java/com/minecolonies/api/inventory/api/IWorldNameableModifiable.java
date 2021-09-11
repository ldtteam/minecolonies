package com.minecolonies.api.inventory.api;

import net.minecraft.world.Nameable;
import org.jetbrains.annotations.Nullable;

/**
 * Created by marcf on 3/25/2017.
 */
public interface IWorldNameableModifiable extends Nameable
{
    /**
     * Method to set the name of this {@link Nameable}.
     *
     * @param name The new name of this {@link Nameable}, or null to reset it to its default.
     */
    void setName(@Nullable String name);
}
