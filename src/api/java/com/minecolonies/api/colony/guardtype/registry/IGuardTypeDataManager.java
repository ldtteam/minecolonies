package com.minecolonies.api.colony.guardtype.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.guardtype.GuardType;
import net.minecraft.util.ResourceLocation;

public interface IGuardTypeDataManager
{

    static IGuardTypeDataManager getInstance()
    {
        return IMinecoloniesAPI.getInstance().getGuardTypeDataManager();
    }

    GuardType getFrom(ResourceLocation jobName);
}
