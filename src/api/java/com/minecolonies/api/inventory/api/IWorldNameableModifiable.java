package com.minecolonies.api.inventory.api;

import net.minecraft.util.INameable;
import org.jetbrains.annotations.Nullable;

/**
 * Created by marcf on 3/25/2017.
 */
public interface IWorldNameableModifiable extends INameable
{
    /**
     * Method to set the name of this {@link INameable}.
     *
     * @param name The new name of this {@link INameable}, or null to reset it to its default.
     */
    void setName(@Nullable String name);
}
