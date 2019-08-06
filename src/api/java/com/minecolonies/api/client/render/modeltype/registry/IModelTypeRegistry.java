package com.minecolonies.api.client.render.modeltype.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.client.render.modeltype.IModelType;
import net.minecraft.client.renderer.entity.model.BipedModel;

import java.util.Map;

public interface IModelTypeRegistry
{

    static IModelTypeRegistry getInstance()
    {
        return IMinecoloniesAPI.getInstance().getModelTypeRegistry();
    }

    IModelTypeRegistry register(IModelType type, BipedModel maleModel, BipedModel femaleModel);

    Map<IModelType, BipedModel> getMaleMap();

    Map<IModelType, BipedModel> getFemaleMap();
}
