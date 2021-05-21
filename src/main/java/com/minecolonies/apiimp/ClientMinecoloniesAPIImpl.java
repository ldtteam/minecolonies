package com.minecolonies.apiimp;

import com.minecolonies.api.client.render.modeltype.registry.ICitizenResourceRegistry;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.coremod.client.render.modeltype.registry.ModelTypeRegistry;
import com.minecolonies.coremod.client.render.modularcitizen.CitizenResourceRegistry;

public class ClientMinecoloniesAPIImpl extends CommonMinecoloniesAPIImpl
{
    private final IModelTypeRegistry       modelTypeRegistry       = new ModelTypeRegistry();
    private final ICitizenResourceRegistry citizenResourceRegistry = new CitizenResourceRegistry();

    @Override
    public IModelTypeRegistry getModelTypeRegistry()
    {
        return modelTypeRegistry;
    }

    @Override
    public ICitizenResourceRegistry getCitizenResourceRegistry()
    {
        return citizenResourceRegistry;
    }
}
