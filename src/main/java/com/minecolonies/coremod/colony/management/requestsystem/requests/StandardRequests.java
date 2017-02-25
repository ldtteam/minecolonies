package com.minecolonies.coremod.colony.management.requestsystem.requests;

import com.minecolonies.coremod.colony.IColony;
import com.minecolonies.coremod.colony.management.requestsystem.api.IRequestToken;
import org.jetbrains.annotations.NotNull;

/**
 * Created by marcf on 2/25/2017.
 */
public abstract class StandardRequests {

    public static class ItemStack extends AbstractRequest<ItemStack> {

        public ItemStack(@NotNull IColony colony, @NotNull IRequestToken token, @NotNull ItemStack requested) {
            super(colony, token, requested);
        }
    }
}
