package com.minecolonies.core.client.render.modeltype.registry;

import com.minecolonies.api.client.render.modeltype.IModelType;
import net.minecraft.resources.Identifier;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

public class ModelTypeRegistry implements IModelTypeRegistry
{
    private final ConcurrentHashMap<Identifier, IModelType> modelMap = new ConcurrentHashMap<>();

    public ModelTypeRegistry()
    {

    }

    @Override
    public void register(final IModelType type)
    {
        modelMap.put(type.getName(), type);
    }

    @Override
    public @Nullable IModelType getModelType(final Identifier name)
    {
        return modelMap.get(name);
    }
}
