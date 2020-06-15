package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.constant.Constants;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.entity.citizen.AbstractEntityCitizen.DATA_STYLE;
import static com.minecolonies.api.entity.citizen.AbstractEntityCitizen.DATA_TEXTURE_SUFFIX;

public interface ISimpleModelType extends IModelType {

    /**
     * Base folder for textures.
     */
    String BASE_FOLDER = "textures/entity/citizen/";

    /**
     * Default folder.
     */
    String DEFAULT_FOLDER = "default/";
    
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
    default ResourceLocation getTexture(@NotNull final AbstractEntityCitizen entityCitizen)
    {
        String folder = DEFAULT_FOLDER;
        final int moddedTextureId = (entityCitizen.getTextureId() % getNumTextures()) + 1;
        final String textureIdentifier = getTextureBase() + (entityCitizen.isFemale() ? "female" : "male") + moddedTextureId + entityCitizen.getRenderMetadata() + entityCitizen.getDataManager().get(DATA_TEXTURE_SUFFIX);

        //TODO: We have to add style tags to the townhalls that will be used for this in the future.
        // - This will then become a switch case statement for this sake
        if (entityCitizen.getDataManager().get(DATA_STYLE).contains("medieval"))
        {
            folder = "medieval/";
        }

        ResourceLocation modified = new ResourceLocation(Constants.MOD_ID, BASE_FOLDER + folder + textureIdentifier + ".png");
        if (Minecraft.getInstance().getResourceManager().hasResource(modified))
        {
            return modified;
        }

        return new ResourceLocation(Constants.MOD_ID, BASE_FOLDER + DEFAULT_FOLDER + textureIdentifier + ".png");
    }
}
