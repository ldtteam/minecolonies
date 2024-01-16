package com.minecolonies.core.colony.buildings.registry;

import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.colony.guardtype.registry.IGuardTypeDataManager;
import com.minecolonies.api.colony.guardtype.registry.IGuardTypeRegistry;
import net.minecraft.resources.ResourceLocation;

public final class GuardTypeDataManager implements IGuardTypeDataManager
{
    @Override
    public GuardType getFrom(final ResourceLocation jobName)
    {
        if (jobName == null)
        {
            return null;
        }

        return IGuardTypeRegistry.getInstance().getValue(jobName);
    }
}
