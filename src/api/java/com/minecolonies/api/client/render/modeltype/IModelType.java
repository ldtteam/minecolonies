package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.constant.Constants;

import net.minecraft.util.ResourceLocation;

/**
 * Defines a model type and its textures.
 * Use the {@link com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry} to register it, together with a Citizen model for both male and female.
 */
public interface IModelType
{
    /**
     * The name of the model type.
     * @return The name.
     */
    String getName();

    /**
     * Method used to get the path to the texture every time it is updated on the entity.
     *
     * @param entityCitizen The citizen in question to get the path.
     * @return The path to the citizen.
     */
    ResourceLocation getTexture(AbstractEntityCitizen entityCitizen);
}
