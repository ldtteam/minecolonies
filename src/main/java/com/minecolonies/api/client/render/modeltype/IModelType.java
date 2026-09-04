package com.minecolonies.api.client.render.modeltype;

import net.minecraft.resources.Identifier;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;

/**
 * Defines a model type and its textures. Use the {@link com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry} to register it, together with a Citizen model for
 * both male and female.
 */
public interface IModelType
{
    /**
     * The name of the model type.
     *
     * @return The name.
     */
    Identifier getName();

    /**
     * Method used to get the path to the texture every time it is updated on the entity.
     *
     * @param entityCitizen The citizen in question to get the path.
     * @return The path to the citizen.
     */
    Identifier getTexture(CitizenRenderState renderState);

    default Identifier getTexture(final AbstractEntityCitizen citizen)
    {
        final CitizenRenderState renderState = new CitizenRenderState();
        renderState.setCitizen(citizen);
        return getTexture(renderState);
    }

    /**
     * Get the male model for this model type
     *
     * @return The male model
     */
    CitizenModel<CitizenRenderState> getMaleModel();

    /**
     * Get the female model for this model type
     *
     * @return The female model
     */
    CitizenModel<CitizenRenderState> getFemaleModel();
}
