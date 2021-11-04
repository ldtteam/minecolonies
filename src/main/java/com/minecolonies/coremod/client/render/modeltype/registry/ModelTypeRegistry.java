package com.minecolonies.coremod.client.render.modeltype.registry;

import com.google.common.collect.Maps;
import com.minecolonies.api.client.render.modeltype.IModelType;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import net.minecraft.util.ResourceLocation;

import java.util.concurrent.ConcurrentMap;

public class ModelTypeRegistry implements IModelTypeRegistry
{
    private final ConcurrentMap<ResourceLocation, IModelType> modelMap = Maps.newConcurrentMap();

    public ModelTypeRegistry()
    {

    }

    @Override
    public void register(final IModelType type)
    {
        modelMap.put(type.getName(), type);
    }

    @Override
    public IModelType getModelType(final ResourceLocation name)
    {
        return modelMap.get(name);
    }
}
