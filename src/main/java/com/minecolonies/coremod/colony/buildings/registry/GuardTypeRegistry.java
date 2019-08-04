package com.minecolonies.coremod.colony.buildings.registry;

import com.google.common.collect.Maps;
import com.minecolonies.api.colony.buildings.IGuardType;
import com.minecolonies.api.colony.buildings.registry.IGuardTypeRegistry;
import com.minecolonies.coremod.colony.buildings.EnumGuardType;
import net.minecraft.util.ResourceLocation;

import java.util.LinkedHashMap;

public class GuardTypeRegistry implements IGuardTypeRegistry
{
    private final LinkedHashMap<ResourceLocation, IGuardType> registryMap = Maps.newLinkedHashMap();

    public GuardTypeRegistry()
    {
        //For now we initialize like this.
        registryMap.put(EnumGuardType.RANGER.getRegistryName(), EnumGuardType.RANGER);
        registryMap.put(EnumGuardType.KNIGHT.getRegistryName(), EnumGuardType.KNIGHT);

        //We also register this to preserve backwards compatibility since these are the ordinals that are used in the previous mappings from the enum.
        registryMap.put(new ResourceLocation(String.valueOf(EnumGuardType.RANGER.ordinal())), EnumGuardType.RANGER);
        registryMap.put(new ResourceLocation(String.valueOf(EnumGuardType.KNIGHT.ordinal())), EnumGuardType.KNIGHT);
    }

    @Override
    public IGuardTypeRegistry registerGuardType(final IGuardType type)
    {
        registryMap.put(type.getRegistryName(), type);
        return this;
    }

    @Override
    public LinkedHashMap<ResourceLocation, IGuardType> getRegisteredTypes()
    {
        return registryMap;
    }
}
