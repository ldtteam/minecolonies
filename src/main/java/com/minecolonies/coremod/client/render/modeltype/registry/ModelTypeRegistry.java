package com.minecolonies.coremod.client.render.modeltype.registry;

import com.minecolonies.api.client.render.modeltype.IModelType;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

public class ModelTypeRegistry implements IModelTypeRegistry
{
    private final ConcurrentHashMap<ResourceLocation, IModelType> modelMap = new ConcurrentHashMap<>();

    public ModelTypeRegistry()
    {

    }

    @Override
    public void register(final IModelType type)
    {
        modelMap.put(type.getName(), type);
    }

    @Override
    public @Nullable IModelType getModelType(final ResourceLocation name)
    {
        return modelMap.get(name);
    }
}
