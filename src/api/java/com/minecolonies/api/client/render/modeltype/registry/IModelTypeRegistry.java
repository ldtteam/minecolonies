package com.minecolonies.api.client.render.modeltype.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.client.render.modeltype.IModelType;

import java.util.Map;

public interface IModelTypeRegistry
{

    static IModelTypeRegistry getInstance()
    {
        return IMinecoloniesAPI.getInstance().getModelTypeRegistry();
    }

    IModelTypeRegistry register(IModelType type, CitizenModel maleModel, CitizenModel femaleModel);

    Map<IModelType, CitizenModel> getMaleMap();

    Map<IModelType, CitizenModel> getFemaleMap();
}
