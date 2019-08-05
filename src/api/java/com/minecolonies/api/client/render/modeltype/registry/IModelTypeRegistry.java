package com.minecolonies.api.client.render.modeltype.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.client.render.modeltype.IModelType;
import net.minecraft.client.model.ModelBiped;

import java.util.Map;

public interface IModelTypeRegistry
{

    static IModelTypeRegistry getInstance()
    {
        return IMinecoloniesAPI.getInstance().getModelTypeRegistry();
    }

    IModelTypeRegistry register(IModelType type, ModelBiped maleModel, ModelBiped femaleModel);

    Map<IModelType, ModelBiped> getMaleMap();

    Map<IModelType, ModelBiped> getFemaleMap();
}
