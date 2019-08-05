package com.minecolonies.coremod.colony.buildings.registry;

import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.colony.guardtype.registry.IGuardTypeDataManager;
import net.minecraft.util.ResourceLocation;

public class GuardTypeDataManager implements IGuardTypeDataManager
{
    public GuardTypeDataManager()
    {
    }

    @Override
    public GuardType getFrom(final ResourceLocation jobName)
    {
        return null;
    }
}
