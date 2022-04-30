package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractBuildingWithLootTableModule extends AbstractBuildingModule
{
    @NotNull
    public abstract ResourceLocation getDefaultLootTable();
}
