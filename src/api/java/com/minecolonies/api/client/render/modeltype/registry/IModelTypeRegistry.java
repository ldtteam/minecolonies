package com.minecolonies.api.client.render.modeltype.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.client.render.modeltype.IModelType;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.entity.model.BipedModel;

import java.util.Map;

public interface IModelTypeRegistry
{

    static IModelTypeRegistry getInstance()
    {
        return IMinecoloniesAPI.getInstance().getModelTypeRegistry();
    }

    IModelTypeRegistry register(IModelType type, BipedModel<? extends AbstractEntityCitizen> maleModel, BipedModel<? extends AbstractEntityCitizen> femaleModel);

    Map<IModelType, ? extends AbstractEntityCitizen> getMaleMap();

    Map<IModelType, ? extends AbstractEntityCitizen> getFemaleMap();
}
