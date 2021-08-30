package com.minecolonies.coremod.client.render.modeltype.registry;

import com.google.common.collect.Maps;
import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.client.render.modeltype.IModelType;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.client.model.*;

import java.util.Collections;
import java.util.Map;

public class ModelTypeRegistry implements IModelTypeRegistry
{
    private final Map<IModelType, CitizenModel<AbstractEntityCitizen>> maleMap   = Maps.newHashMap();
    private final Map<IModelType, CitizenModel<AbstractEntityCitizen>> femaleMap = Maps.newHashMap();

    public ModelTypeRegistry()
    {

    }

    @Override
    public IModelTypeRegistry register(final IModelType type, final CitizenModel<AbstractEntityCitizen> maleModel, final CitizenModel<AbstractEntityCitizen> femaleModel)
    {
        this.maleMap.put(type, maleModel);
        this.femaleMap.put(type, femaleModel);

        return this;
    }

    @Override
    public IModelTypeRegistry register(final IModelType type, final boolean female, final CitizenModel<AbstractEntityCitizen> model)
    {
        if (female)
        {
            this.femaleMap.put(type, model);
        }
        else
        {
            this.maleMap.put(type, model);
        }

        return this;
    }

    @Override
    public Map<IModelType, CitizenModel<AbstractEntityCitizen>> getMaleMap()
    {
        return Collections.unmodifiableMap(maleMap);
    }

    @Override
    public Map<IModelType, CitizenModel<AbstractEntityCitizen>> getFemaleMap()
    {
        return Collections.unmodifiableMap(femaleMap);
    }
}
