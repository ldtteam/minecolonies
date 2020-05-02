package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.constant.Constants;

import net.minecraft.util.ResourceLocation;

public interface ISimpleModelType extends IModelType {

    /**
     * The base name of the texture.
     * Is by default appended by a random textureId as well as the render info.
     *
     * @return The base file name.
     */
    String getTextureBase();

    /**
     * The available amount of textures in this model type.
     *
     * @return The amount of textures available.
     */
    int getNumTextures();

    /**
     * Method used to get the path to the texture every time it is updated on the entity.
     * By default this uses the textureBase + sex marker + randomly assigned texture index + metadata as a format.
     *
     * @param entityCitizen The citizen in question to get the path.
     * @return The path to the citizen.
     */
    default ResourceLocation getTexture(AbstractEntityCitizen entityCitizen)
    {
        final String textureBase = "textures/entity/" + getTextureBase() + (entityCitizen.isFemale() ? "female" : "male");
        final int moddedTextureId = (entityCitizen.getTextureId() % getNumTextures()) + 1;
        return new ResourceLocation(Constants.MOD_ID, textureBase + moddedTextureId + entityCitizen.getRenderMetadata() + ".png");
    }
}
