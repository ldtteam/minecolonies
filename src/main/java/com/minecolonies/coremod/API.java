package com.minecolonies.coremod;

import com.minecolonies.api.IAPI;
import com.minecolonies.api.colony.management.IColonyManager;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.NotNull;

/**
 * The actual API implementation of the Minecolonies API
 */
public final class API implements IAPI
{
    public final static API INSTANCE = new API();

    private API()
    {
        if (INSTANCE != null)
        {
            throw new IllegalStateException("API Already created");
        }

        IAPI.Holder.setApi(this);
    }

    @NotNull
    @Override
    public IColonyManager getColonyManager()
    {
        return null;
    }

    @NotNull
    @Override
    public IColonyManager getManagerForSpecificSide(@NotNull final Side side)
    {
        return null;
    }
}
