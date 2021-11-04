package com.minecolonies.api.client.render.modeltype.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.client.render.modeltype.IModelType;
import net.minecraft.util.ResourceLocation;

/**
 *
 */
public interface IModelTypeRegistry
{
    /**
     * @return
     */
    static IModelTypeRegistry getInstance()
    {
        return IMinecoloniesAPI.getInstance().getModelTypeRegistry();
    }

    /**
     * @param name
     * @param modelType
     * @return
     */
    void register(IModelType modelType);

    /**
     * @param name
     * @return
     */
    IModelType getModelType(ResourceLocation name);
}
