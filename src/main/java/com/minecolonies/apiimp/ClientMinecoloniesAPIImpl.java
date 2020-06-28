package com.minecolonies.apiimp;

import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.coremod.client.render.modeltype.registry.ModelTypeRegistry;

public class ClientMinecoloniesAPIImpl extends CommonMinecoloniesAPIImpl
{
    private final IModelTypeRegistry modelTypeRegistry = new ModelTypeRegistry();

    @Override
    public IModelTypeRegistry getModelTypeRegistry()
    {
        return modelTypeRegistry;
    }
}
